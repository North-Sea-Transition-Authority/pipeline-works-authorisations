package uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs.controller;

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
import uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs.SupplementaryDocumentsForm;
import uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs.SupplementaryDocumentsService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/supplementary-documents")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = { PwaApplicationType.OPTIONS_VARIATION })
public class SupplementaryDocumentsController {

  private static final FileDocumentType DOCUMENT_TYPE = FileDocumentType.SUPPLEMENTARY_DOCUMENTS;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final SupplementaryDocumentsService supplementaryDocumentsService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public SupplementaryDocumentsController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                          ControllerHelperService controllerHelperService,
                                          SupplementaryDocumentsService supplementaryDocumentsService,
                                          PwaApplicationRedirectService pwaApplicationRedirectService,
                                          PadFileManagementService padFileManagementService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.supplementaryDocumentsService = supplementaryDocumentsService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padFileManagementService = padFileManagementService;
  }

  private ModelAndView getModelAndView(PwaApplicationContext applicationContext,
                                       SupplementaryDocumentsForm form) {
    var fileUploadAttributes = padFileManagementService.getFileUploadComponentAttributes(
        form.getUploadedFiles(),
        applicationContext.getApplicationDetail(),
        DOCUMENT_TYPE
    );

    var modelAndView = new ModelAndView("pwaApplication/shared/supplementaryDocs/supplementaryDocs")
        .addObject("fileUploadAttributes", fileUploadAttributes);

    applicationBreadcrumbService.fromTaskList(applicationContext.getPwaApplication(), modelAndView, "Supplementary documents");

    return modelAndView;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderSupplementaryDocuments(@PathVariable("applicationId") Integer applicationId,
                                                   @PathVariable("applicationType")
                                                   @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                   PwaApplicationContext applicationContext,
                                                   @ModelAttribute("form") SupplementaryDocumentsForm form,
                                                   AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    supplementaryDocumentsService.mapSavedDataToForm(detail, form);
    return getModelAndView(applicationContext, form);
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postSupplementaryDocuments(@PathVariable("applicationId") Integer applicationId,
                                                 @PathVariable("applicationType")
                                                 @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                 PwaApplicationContext applicationContext,
                                                 @ModelAttribute("form") SupplementaryDocumentsForm form,
                                                 BindingResult bindingResult,
                                                 AuthenticatedUserAccount user,
                                                 ValidationType validationType) {

    var detail = applicationContext.getApplicationDetail();
    bindingResult = supplementaryDocumentsService.validate(form, bindingResult, validationType, detail);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getModelAndView(applicationContext, form), () -> {

      supplementaryDocumentsService.updateDocumentFlag(detail, form);

      padFileManagementService.saveFiles(form, detail, DOCUMENT_TYPE);

      return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());
    });
  }
}
