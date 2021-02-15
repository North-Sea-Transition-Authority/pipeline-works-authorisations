package uk.co.ogauthority.pwa.controller.publicnotice;

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
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.form.publicnotice.PublicNoticeDraftForm;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
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
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/public-notice")
@PwaAppProcessingPermissionCheck(permissions = {PwaAppProcessingPermission.PUBLIC_NOTICE})
@PwaApplicationStatusCheck(statuses = PwaApplicationStatus.CASE_OFFICER_REVIEW)
public class PublicNoticeController extends PwaApplicationDataFileUploadAndDownloadController {

  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final PublicNoticeService publicNoticeService;
  private final ControllerHelperService controllerHelperService;

  @Autowired
  public PublicNoticeController(
      AppProcessingBreadcrumbService appProcessingBreadcrumbService,
      PublicNoticeService publicNoticeService,
      ControllerHelperService controllerHelperService,
      AppFileService appFileService) {
    super(appFileService);
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.publicNoticeService = publicNoticeService;
    this.controllerHelperService = controllerHelperService;
  }


  @GetMapping
  public ModelAndView renderDraftPublicNotice(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaAppProcessingContext processingContext,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              @ModelAttribute("form") PublicNoticeDraftForm form) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(
        processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,
        () -> {
          var pwaApplication = processingContext.getPwaApplication();
          publicNoticeService.mapPublicNoticeDraftToForm(pwaApplication, form);
          return getDraftPublicNoticeModelAndView(processingContext, form);
        });
  }

  @PostMapping
  public ModelAndView postDraftPublicNotice(@PathVariable("applicationId") Integer applicationId,
                                              @PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              PwaAppProcessingContext processingContext,
                                              AuthenticatedUserAccount authenticatedUserAccount,
                                              @ModelAttribute("form") PublicNoticeDraftForm form,
                                              BindingResult bindingResult) {

    return CaseManagementUtils.withAtLeastOneSatisfactoryVersion(processingContext,
        PwaAppProcessingTask.PUBLIC_NOTICE,  () -> {
          var validatedBindingResult = publicNoticeService.validate(form, bindingResult);

          return controllerHelperService.checkErrorsAndRedirect(validatedBindingResult,
              getDraftPublicNoticeModelAndView(processingContext, form), () -> {
                publicNoticeService.createPublicNoticeAndStartWorkflow(
                    form, processingContext.getPwaApplication(), authenticatedUserAccount);
                return  CaseManagementUtils.redirectCaseManagement(processingContext);
              });

        });

  }



  private ModelAndView getDraftPublicNoticeModelAndView(PwaAppProcessingContext processingContext, PublicNoticeDraftForm form) {

    var pwaApplication = processingContext.getPwaApplication();

    var modelAndView = createModelAndView("publicNotice/draftPublicNotice",
        pwaApplication,
        AppFilePurpose.PUBLIC_NOTICE,
        form);

    modelAndView.addObject("appRef", pwaApplication.getAppReference())
        .addObject("publicNoticeRequestReasons", PublicNoticeRequestReason.asList())
        .addObject("cancelUrl", CaseManagementUtils.routeCaseManagement(processingContext))
        .addObject("caseSummaryView", processingContext.getCaseSummaryView());

    appProcessingBreadcrumbService.fromCaseManagement(pwaApplication, modelAndView, "Draft a public notice");
    return modelAndView;
  }





  @PostMapping("/file/upload")
  @ResponseBody
  public FileUploadResult handleUpload(@PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("applicationId") Integer applicationId,
                                       @RequestParam("file") MultipartFile file,
                                       PwaAppProcessingContext processingContext) {
    return appFileService.processInitialUpload(
        file,
        processingContext.getPwaApplication(),
        AppFilePurpose.PUBLIC_NOTICE,
        processingContext.getUser());
  }

  @GetMapping("/files/download/{fileId}")
  @ResponseBody
  public ResponseEntity<Resource> handleDownload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaAppProcessingContext processingContext) {
    return serveFile(processingContext.getAppFile());
  }

  @PostMapping("/file/delete/{fileId}")
  @ResponseBody
  public FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaAppProcessingContext processingContext) {
    return appFileService.processFileDeletionWithPreDeleteAction(
        processingContext.getAppFile(),
        processingContext.getUser(),
        appFile -> publicNoticeService.getPublicNoticeDocumentLink(processingContext.getAppFile())
            .ifPresent(publicNoticeService::deleteFileLinkAndPublicNoticeDocument));
  }


}