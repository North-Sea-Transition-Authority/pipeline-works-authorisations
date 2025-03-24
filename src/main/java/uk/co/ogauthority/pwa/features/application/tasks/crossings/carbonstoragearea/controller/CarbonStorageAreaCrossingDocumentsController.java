package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageAreaCrossingFileService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.formhelpers.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.crossings.CrossingAgreementTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/crossings/carbon-storage-crossing-documents")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.DECOMMISSIONING
})
public class CarbonStorageAreaCrossingDocumentsController {

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.CARBON_STORAGE_CROSSINGS;

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final CarbonStorageAreaCrossingFileService carbonStorageAreaCrossingFileService;
  private final CrossingAgreementsTaskListService crossingAgreementsTaskListService;
  private final ControllerHelperService controllerHelperService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public CarbonStorageAreaCrossingDocumentsController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                                      CarbonStorageAreaCrossingFileService carbonStorageAreaCrossingFileService,
                                                      CrossingAgreementsTaskListService crossingAgreementsTaskListService,
                                                      ControllerHelperService controllerHelperService,
                                                      PadFileManagementService padFileManagementService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.carbonStorageAreaCrossingFileService = carbonStorageAreaCrossingFileService;
    this.crossingAgreementsTaskListService = crossingAgreementsTaskListService;
    this.controllerHelperService = controllerHelperService;
    this.padFileManagementService = padFileManagementService;
  }


  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderEditCrossingDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") CrossingDocumentsForm form,
      PwaApplicationContext applicationContext) {

    var pwaApplicationDetail = applicationContext.getApplicationDetail();
    padFileManagementService.mapFilesToForm(form, pwaApplicationDetail, DOCUMENT_TYPE);
    return createCrossingModelAndView(pwaApplicationDetail, form);
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postCrossingDocuments(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @ModelAttribute("form") CrossingDocumentsForm form,
      BindingResult bindingResult,
      PwaApplicationContext applicationContext,
      AuthenticatedUserAccount user) {

    var detail = applicationContext.getApplicationDetail();
    carbonStorageAreaCrossingFileService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );
    var modelAndView = createCrossingModelAndView(applicationContext.getApplicationDetail(), form);
    return controllerHelperService.checkErrorsAndRedirect(bindingResult, modelAndView, () -> {

      padFileManagementService.saveFiles(form, detail, DOCUMENT_TYPE);
      return crossingAgreementsTaskListService.getOverviewRedirect(detail,
          CrossingAgreementTask.CARBON_STORAGE_AREAS);
    });
  }

  private ModelAndView createCrossingModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                       CrossingDocumentsForm form) {
    var fileUploadAttributes = padFileManagementService.getFileUploadComponentAttributes(
        form.getUploadedFiles(),
        pwaApplicationDetail,
        FileDocumentType.CARBON_STORAGE_CROSSINGS
    );

    var modelAndView = new ModelAndView("pwaApplication/form/uploadFiles")
        .addObject("pageTitle", "Carbon storage area crossing agreement documents")
        .addObject("backButtonText", "Back to carbon storage areas")
        .addObject("backUrl",
            crossingAgreementsTaskListService.getRoute(pwaApplicationDetail,
                CrossingAgreementTask.CARBON_STORAGE_AREAS))
        .addObject("fileUploadAttributes", fileUploadAttributes);

    applicationBreadcrumbService.fromCrossingSection(pwaApplicationDetail, modelAndView,
        CrossingAgreementTask.CARBON_STORAGE_AREAS,
        "Carbon storage area crossing agreement documents");
    return modelAndView;
  }
}
