package uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings;

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
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.generic.SummaryForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.AdmiraltyChartUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.PipelineDrawingUrlFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings.TechnicalDrawingSectionService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/technical-drawings")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION
})
public class TechnicalDrawingsController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final AdmiraltyChartFileService admiraltyChartFileService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final TechnicalDrawingSectionService technicalDrawingSectionService;
  private final PadTechnicalDrawingService padTechnicalDrawingService;

  @Autowired
  public TechnicalDrawingsController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      AdmiraltyChartFileService admiraltyChartFileService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      TechnicalDrawingSectionService technicalDrawingSectionService,
      PadTechnicalDrawingService padTechnicalDrawingService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.admiraltyChartFileService = admiraltyChartFileService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.technicalDrawingSectionService = technicalDrawingSectionService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
  }

  private ModelAndView getOverviewModelAndView(PwaApplicationDetail detail) {
    var modelAndView = new ModelAndView("pwaApplication/shared/techdrawings/overview")
        .addObject("admiraltyChartFileViews",
            admiraltyChartFileService.getAdmiraltyChartFileViews(detail, ApplicationFileLinkStatus.FULL))
        .addObject("admiraltyOptional", !admiraltyChartFileService.isUploadRequired(detail))
        .addObject("admiraltyChartUrlFactory", new AdmiraltyChartUrlFactory(detail))
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(detail.getPwaApplication()))
        .addObject("pipelineDrawingUrlFactory",
            new PipelineDrawingUrlFactory(detail))
        .addObject("pipelineDrawingSummaryViews", padTechnicalDrawingService.getPipelineDrawingSummaryViewList(detail));
    applicationBreadcrumbService.fromTaskList(detail.getPwaApplication(), modelAndView,
        "Admiralty chart and pipeline drawings");
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
    if (bindingResult.hasErrors()) {
      return getOverviewModelAndView(detail)
          .addObject("errorMessage", "The admiralty chart section is invalid");
    }
    return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
  }

}
