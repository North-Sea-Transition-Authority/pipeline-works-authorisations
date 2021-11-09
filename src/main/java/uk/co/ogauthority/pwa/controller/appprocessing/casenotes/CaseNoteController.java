package uk.co.ogauthority.pwa.controller.appprocessing.casenotes;

import javax.validation.groups.Default;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.appprocessing.shared.PwaAppProcessingPermissionCheck;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.form.appprocessing.casenotes.AddCaseNoteForm;
import uk.co.ogauthority.pwa.service.appprocessing.AppProcessingBreadcrumbService;
import uk.co.ogauthority.pwa.service.appprocessing.casenotes.CaseNoteService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;
import uk.co.ogauthority.pwa.util.FlashUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/case-management/case-note")
@PwaAppProcessingPermissionCheck(permissions = PwaAppProcessingPermission.ADD_CASE_NOTE)
public class CaseNoteController extends PwaApplicationDataFileUploadAndDownloadController {

  private final CaseNoteService caseNoteService;
  private final AppProcessingBreadcrumbService appProcessingBreadcrumbService;
  private final ControllerHelperService controllerHelperService;

  private static final AppFilePurpose FILE_PURPOSE = AppFilePurpose.CASE_NOTES;

  @Autowired
  public CaseNoteController(CaseNoteService caseNoteService,
                            AppProcessingBreadcrumbService appProcessingBreadcrumbService,
                            ControllerHelperService controllerHelperService,
                            AppFileService appFileService) {
    super(appFileService);
    this.caseNoteService = caseNoteService;
    this.appProcessingBreadcrumbService = appProcessingBreadcrumbService;
    this.controllerHelperService = controllerHelperService;
  }

  @GetMapping
  public ModelAndView renderAddCaseNote(@PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType")
                                           @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                           PwaAppProcessingContext processingContext,
                                           @ModelAttribute("form") AddCaseNoteForm form,
                                           AuthenticatedUserAccount authenticatedUserAccount) {

    return getAddCaseNoteModelAndView(processingContext, form);

  }

  private ModelAndView getAddCaseNoteModelAndView(PwaAppProcessingContext processingContext,
                                                  AddCaseNoteForm form) {

    var app = processingContext.getPwaApplication();

    var modelAndView = createModelAndView(
        "pwaApplication/appProcessing/caseNotes/addCaseNote",
        app,
        FILE_PURPOSE,
        form
    );

    modelAndView
        .addObject("caseSummaryView", processingContext.getCaseSummaryView())
        .addObject("caseManagementUrl", CaseManagementUtils.routeCaseManagement(app));

    appProcessingBreadcrumbService.fromCaseManagement(processingContext.getPwaApplication(), modelAndView, "Add case note");

    return modelAndView;

  }

  @PostMapping
  public ModelAndView postAddCaseNote(@PathVariable("applicationId") Integer applicationId,
                                      @PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      PwaAppProcessingContext processingContext,
                                      @Validated(value = {Default.class, FullValidation.class})
                                      @ModelAttribute("form") AddCaseNoteForm form,
                                      BindingResult bindingResult,
                                      AuthenticatedUserAccount authenticatedUserAccount,
                                      RedirectAttributes redirectAttributes) {

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getAddCaseNoteModelAndView(processingContext, form), () -> {

      caseNoteService.createCaseNote(processingContext.getPwaApplication(), form, authenticatedUserAccount);

      FlashUtils.info(redirectAttributes, "Case note added");

      return CaseManagementUtils.redirectCaseManagement(processingContext.getPwaApplication());

    });

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
        FILE_PURPOSE,
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
        appFile -> caseNoteService.getCaseNoteDocumentLink(processingContext.getPwaApplication(), processingContext.getAppFile())
            .ifPresent(caseNoteService::deleteFileLink));
  }

}
