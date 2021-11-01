package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.overview.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.overview.TechnicalDrawingSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.UmbilicalCrossSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.UmbilicalCrossSectionUrlFactory;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.generic.SummaryForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingUrlFactory;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/technical-drawings")
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.OPTIONS_VARIATION
})
public class TechnicalDrawingsController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final AdmiraltyChartFileService admiraltyChartFileService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final TechnicalDrawingSectionService technicalDrawingSectionService;
  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final PadFileService padFileService;
  private final UmbilicalCrossSectionService umbilicalCrossSectionService;

  @Autowired
  public TechnicalDrawingsController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      AdmiraltyChartFileService admiraltyChartFileService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      TechnicalDrawingSectionService technicalDrawingSectionService,
      PadTechnicalDrawingService padTechnicalDrawingService,
      PadFileService padFileService,
      UmbilicalCrossSectionService umbilicalCrossSectionService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.admiraltyChartFileService = admiraltyChartFileService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.technicalDrawingSectionService = technicalDrawingSectionService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.padFileService = padFileService;
    this.umbilicalCrossSectionService = umbilicalCrossSectionService;
  }

  private ModelAndView getOverviewModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/techdrawings/overview")
        .addObject("showAdmiraltyChart", admiraltyChartFileService.canUploadDocuments(detail))
        .addObject("admiraltyChartFileViews",
            padFileService.getUploadedFileViews(detail, ApplicationDetailFilePurpose.ADMIRALTY_CHART,
                ApplicationFileLinkStatus.FULL))
        .addObject("admiraltyOptional", !admiraltyChartFileService.isUploadRequired(detail))
        .addObject("admiraltyChartUrlFactory", new AdmiraltyChartUrlFactory(detail))
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(detail.getPwaApplication()))
        .addObject("pipelineDrawingUrlFactory", new PipelineDrawingUrlFactory(detail))
        .addObject("pipelineDrawingSummaryViews", padTechnicalDrawingService.getPipelineDrawingSummaryViewList(detail))
        .addObject("showUmbilicalCrossSection", umbilicalCrossSectionService.canUploadDocuments(detail))
        .addObject("umbilicalCrossSectionUrlFactory", new UmbilicalCrossSectionUrlFactory(detail))
        .addObject("umbilicalCrossSectionFileViews",
            padFileService.getUploadedFileViews(detail, ApplicationDetailFilePurpose.UMBILICAL_CROSS_SECTION,
                ApplicationFileLinkStatus.FULL));
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView,
        "Pipeline schematics and other diagrams");
    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderOverview(@PathVariable("applicationType")
                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                     @PathVariable("applicationId") Integer applicationId,
                                     PwaApplicationContext applicationContext,
                                     AuthenticatedUserAccount user) {
    return getOverviewModelAndView(applicationContext.getApplicationDetail());
  }


  @PostMapping
  public ModelAndView postOverview(@PathVariable("applicationType")
                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                   @PathVariable("applicationId") Integer applicationId,
                                   @Valid SummaryForm form,
                                   BindingResult bindingResult,
                                   PwaApplicationContext applicationContext,
                                   AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    bindingResult = technicalDrawingSectionService.validate(form, bindingResult, ValidationType.FULL, detail);
    var validationSummary = technicalDrawingSectionService.getValidationSummary(bindingResult);
    if (!validationSummary.isComplete()) {
      return getOverviewModelAndView(detail)
          .addObject("errorMessage", validationSummary.getErrorMessage())
          .addObject("validatorFactory", padTechnicalDrawingService.getValidationFactory(detail));
    }
    return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
  }

}
