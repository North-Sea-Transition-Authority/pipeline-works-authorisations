package uk.co.ogauthority.pwa.controller.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeDocumentUpdateService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/public-notice-document-update")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.UPDATE_PUBLIC_NOTICE_DOC})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class PublicNoticeDocumentUpdateController {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService;
  private final ControllerHelperService controllerHelperService;
  private final AppFileManagementService appFileManagementService;

  @Autowired
  public PublicNoticeDocumentUpdateController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      PublicNoticeService publicNoticeService,
      ControllerHelperService controllerHelperService,
      PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService,
      AppFileManagementService appFileManagementService) {
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.publicNoticeService = publicNoticeService;
    this.controllerHelperService = controllerHelperService;
    this.publicNoticeDocumentUpdateService = publicNoticeDocumentUpdateService;
    this.appFileManagementService = appFileManagementService;
  }

  private ModelAndView publicNoticeInValidState(PwaAppProcessingContext processingContext,
                                                Supplier<ModelAndView> modelAndViewSupplier) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> {
          if (publicNoticeDocumentUpdateService.publicNoticeDocumentCanBeUpdated(processingContext.getPwaApplication())) {
            return modelAndViewSupplier.get();
          }
          throw new AccessDeniedException(
              "Access denied as there is not a public notice that requires the document to be updated for application with id: " +
                  processingContext.getMasterPwaApplicationId());
        });
  }

  @GetMapping
  public ModelAndView renderUpdatePublicNoticeDocument(@PathVariable("applicationId") Integer applicationId,
                                                @PathVariable("applicationType")
                                                @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                PwaAppProcessingContext processingContext,
                                                AuthenticatedUserAccount authenticatedUserAccount,
                                                @ModelAttribute("form") UpdatePublicNoticeDocumentForm form) {

    return publicNoticeInValidState(processingContext, () ->
        getUpdatePublicNoticeDocumentModelAndView(processingContext, form));
  }

  @PostMapping
  public ModelAndView postUpdatePublicNoticeDocument(@PathVariable("applicationId") Integer applicationId,
                                                     @PathVariable("applicationType")
                                                     @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                                     PwaAppProcessingContext processingContext,
                                                     AuthenticatedUserAccount authenticatedUserAccount,
                                                     @ModelAttribute("form") UpdatePublicNoticeDocumentForm form,
                                                     BindingResult bindingResult,
                                                     RedirectAttributes redirectAttributes) {

    return publicNoticeInValidState(processingContext, () -> {
      var validatedBindingResult = publicNoticeDocumentUpdateService.validate(form, bindingResult);

      return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
          getUpdatePublicNoticeDocumentModelAndView(processingContext, form), () -> {

            publicNoticeDocumentUpdateService.updatePublicNoticeDocumentAndTransitionWorkflow(
                processingContext.getPwaApplication(), form);
            FlashUtils.success(redirectAttributes, "Public notice document updated");
            return  ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));
          });
    });

  }

  private ModelAndView getUpdatePublicNoticeDocumentModelAndView(PwaAppProcessingContext processingContext,
                                                                 UpdatePublicNoticeDocumentForm form) {

    var pwaApplication = processingContext.getPwaApplication();
    var publicNotice = publicNoticeService.getLatestPublicNotice(pwaApplication);
    var publicNoticeRequest = publicNoticeService.getLatestPublicNoticeRequest(publicNotice);

    var publicNoticeDocumentFileView = publicNoticeService.getLatestPublicNoticeDocumentFileView(pwaApplication);

    var fileUploadAttributes = publicNoticeService.getFileUploadComponentAttributes(
        form.getUploadedFiles(),
        pwaApplication
    );

    var modelAndView = new ModelAndView("publicNotice/updatePublicNoticeDocument")
        .addObject("appRef", pwaApplication.getAppReference())
        .addObject("coverLetter", publicNoticeRequest.getCoverLetterText())
        .addObject("publicNoticeDocumentComments", publicNoticeService.getLatestPublicNoticeDocument(publicNotice).getComments())
        .addObject("publicNoticeDocumentFileView", publicNoticeDocumentFileView)
        .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(processingContext))
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("fileUploadAttributes", fileUploadAttributes);

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Update public notice document");
    return modelAndView;
  }
}