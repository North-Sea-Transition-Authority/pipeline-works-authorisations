package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartDocumentForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.admiralty.AdmiraltyChartFileService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.overview.controller.TechnicalDrawingsController;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/technical-drawings/admiralty-chart")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION
})
public class AdmiraltyChartDocumentsController {

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.ADMIRALTY_CHART;

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final AdmiraltyChartFileService admiraltyChartFileService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public AdmiraltyChartDocumentsController(
      AdmiraltyChartFileService admiraltyChartFileService,
      ApplicationBreadcrumbService applicationBreadcrumbService,
      ControllerHelperService controllerHelperService,
      PadFileManagementService padFileManagementService
  ) {
    this.admiraltyChartFileService = admiraltyChartFileService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.padFileManagementService = padFileManagementService;
  }

  private ModelAndView createAdmiraltyChartModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                        AdmiraltyChartDocumentForm form) {
    var fileUploadAttributes = padFileManagementService.getFileUploadComponentAttributes(
        form.getUploadedFiles(),
        pwaApplicationDetail,
        DOCUMENT_TYPE
    );

    var modelAndView = new ModelAndView("pwaApplication/form/uploadFiles")
        .addObject("pageTitle", "Admiralty chart")
        .addObject("backButtonText", "Back to technical drawings")
        .addObject("backUrl", ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)))
        .addObject("singleFileUpload", true)
        .addObject("fileUploadAttributes", fileUploadAttributes);

    applicationBreadcrumbService.fromTechnicalDrawings(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Admiralty chart");

    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderEditAdmiraltyChartDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AdmiraltyChartDocumentForm form,
      PwaApplicationContext applicationContext) {
    padFileManagementService.mapFilesToForm(form, applicationContext.getApplicationDetail(), DOCUMENT_TYPE);
    return createAdmiraltyChartModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postAdmiraltyChartDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") AdmiraltyChartDocumentForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    admiraltyChartFileService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );
    var modelAndView = createAdmiraltyChartModelAndView(applicationContext.getApplicationDetail(), form);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {
      padFileManagementService.saveFiles(form, applicationContext.getApplicationDetail(), DOCUMENT_TYPE);
      return ReverseRouter.redirect(on(TechnicalDrawingsController.class)
          .renderOverview(applicationType, detail.getMasterPwaApplicationId(), null, null));
    });
  }
}
