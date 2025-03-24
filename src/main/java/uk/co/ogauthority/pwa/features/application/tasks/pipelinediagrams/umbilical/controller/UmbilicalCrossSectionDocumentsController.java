package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.controller;

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
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.overview.controller.TechnicalDrawingsController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.UmbilicalCrossSectionForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.umbilical.UmbilicalCrossSectionService;
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
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/technical-drawings/umbilical-cross-section")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION
})
public class UmbilicalCrossSectionDocumentsController {

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.UMBILICAL_CROSS_SECTION;

  private final UmbilicalCrossSectionService umbilicalCrossSectionService;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public UmbilicalCrossSectionDocumentsController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      UmbilicalCrossSectionService umbilicalCrossSectionService,
      ControllerHelperService controllerHelperService,
      PadFileManagementService padFileManagementService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.umbilicalCrossSectionService = umbilicalCrossSectionService;
    this.controllerHelperService = controllerHelperService;
    this.padFileManagementService = padFileManagementService;
  }

  private ModelAndView createFileUploadModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                    UmbilicalCrossSectionForm form) {
    var fileUploadAttributes = padFileManagementService.getFileUploadComponentAttributes(
        form.getUploadedFiles(),
        pwaApplicationDetail,
        DOCUMENT_TYPE
    );

    var modelAndView = new ModelAndView("pwaApplication/form/uploadFiles")
        .addObject("pageTitle", "Umbilical cross-section diagram")
        .addObject("backButtonText", "Back to technical drawings")
        .addObject("backUrl", ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(pwaApplicationDetail.getPwaApplicationType(),
                pwaApplicationDetail.getMasterPwaApplicationId(), null, null)))
        .addObject("singleFileUpload", true)
        .addObject("restrictToImageFileTypes", true)
        .addObject("fileUploadAttributes", fileUploadAttributes);

    applicationBreadcrumbService.fromTechnicalDrawings(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Umbilical cross-section diagram");

    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderAddDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") UmbilicalCrossSectionForm form,
      PwaApplicationContext applicationContext) {
    padFileManagementService.mapFilesToForm(form, applicationContext.getApplicationDetail(), DOCUMENT_TYPE);
    return createFileUploadModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postAddDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") UmbilicalCrossSectionForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext) {

    var detail = applicationContext.getApplicationDetail();
    umbilicalCrossSectionService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );
    var modelAndView = createFileUploadModelAndView(applicationContext.getApplicationDetail(), form);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {

      padFileManagementService.saveFiles(form, detail, DOCUMENT_TYPE);

      return ReverseRouter.redirect(on(TechnicalDrawingsController.class)
          .renderOverview(applicationType, detail.getMasterPwaApplicationId(), null, null));
    });
  }
}
