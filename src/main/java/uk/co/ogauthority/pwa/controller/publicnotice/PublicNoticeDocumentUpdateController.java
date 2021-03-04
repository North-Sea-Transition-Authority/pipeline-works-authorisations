package uk.co.ogauthority.pwa.controller.publicnotice;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.form.publicnotice.UpdatePublicNoticeDocumentForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeDocumentUpdateService;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/public-notice-document-update")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.UPDATE_PUBLIC_NOTICE_DOC})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class PublicNoticeDocumentUpdateController extends PwaApplicationDataFileUploadAndDownloadController {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final PublicNoticeService publicNoticeService;
  private final PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService;
  private final ControllerHelperService controllerHelperService;

  private static final String FILE_HANDLE_UNSUPPORTED_OPERATION_EXCEPTION_MSG =
      "File handling is not directly supported within PublicNoticeDocumentUpdateController. " +
          "File handling should be handled in PublicNoticeDraftController";

  @Autowired
  public PublicNoticeDocumentUpdateController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      PublicNoticeService publicNoticeService,
      ControllerHelperService controllerHelperService,
      AppFileService appFileService,
      PublicNoticeDocumentUpdateService publicNoticeDocumentUpdateService) {
    super(appFileService);
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.publicNoticeService = publicNoticeService;
    this.controllerHelperService = controllerHelperService;
    this.publicNoticeDocumentUpdateService = publicNoticeDocumentUpdateService;
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
              "Access denied as there is not an public notice that requires the document to be updated for application with id: " +
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
                                            BindingResult bindingResult) {

    return publicNoticeInValidState(processingContext, () -> {
      var validatedBindingResult = publicNoticeDocumentUpdateService.validate(form, bindingResult);

      return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
          getUpdatePublicNoticeDocumentModelAndView(processingContext, form), () -> {

            publicNoticeDocumentUpdateService.updatePublicNoticeDocumentAndTransitionWorkflow(
                processingContext.getPwaApplication(), form, authenticatedUserAccount);
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

    var modelAndView = createModelAndView("publicNotice/updatePublicNoticeDocument",
        pwaApplication,
        AppFilePurpose.PUBLIC_NOTICE,
        form);

    modelAndView.addObject("appRef", pwaApplication.getAppReference())
        .addObject("coverLetter", publicNoticeRequest.getCoverLetterText())
        .addObject("publicNoticeDocumentComments", publicNoticeService.getLatestPublicNoticeDocument(publicNotice).getComments())
        .addObject("publicNoticeDocumentFileView", publicNoticeDocumentFileView)
        .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(processingContext))
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Update public notice document");
    return modelAndView;
  }




  //These file handle methods are not actually used, as the file purpose used in this controller is associated with the
  // public notice draft controller which supports the permission/s associated with this controller.
  // The file methods below are still required to be implemented here as we're extending the abstract class
  // PwaApplicationDataFileUploadAndDownloadController therefore throwing UnsupportedOperationException.
  @PostMapping("/file/upload")
  @ResponseBody
  public FileUploadResult handleUpload(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("applicationId") Integer applicationId,
                                       @RequestParam("file") MultipartFile file,
                                       PwaAppProcessingContext processingContext) {
    throw new UnsupportedOperationException(FILE_HANDLE_UNSUPPORTED_OPERATION_EXCEPTION_MSG);
  }

  @GetMapping("/files/download/{fileId}")
  @ResponseBody
  public ResponseEntity<Resource> handleDownload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaAppProcessingContext processingContext) {
    throw new UnsupportedOperationException(FILE_HANDLE_UNSUPPORTED_OPERATION_EXCEPTION_MSG);
  }

  @PostMapping("/file/delete/{fileId}")
  @ResponseBody
  public FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaAppProcessingContext processingContext) {
    throw new UnsupportedOperationException(FILE_HANDLE_UNSUPPORTED_OPERATION_EXCEPTION_MSG);
  }




}