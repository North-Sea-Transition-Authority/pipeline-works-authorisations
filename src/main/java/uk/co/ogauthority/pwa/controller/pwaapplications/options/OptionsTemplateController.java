package uk.co.ogauthority.pwa.controller.pwaapplications.options;

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
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDetailDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.form.pwaapplications.options.OptionsTemplateForm;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.options.OptionsTemplateService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/options-template")
@PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = { PwaApplicationType.OPTIONS_VARIATION })
public class OptionsTemplateController extends PwaApplicationDetailDataFileUploadAndDownloadController {

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.OPTIONS_TEMPLATE;
  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final ControllerHelperService controllerHelperService;
  private final OptionsTemplateService optionsTemplateService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  @Autowired
  public OptionsTemplateController(PadFileService padFileService,
                                   ApplicationBreadcrumbService applicationBreadcrumbService,
                                   ControllerHelperService controllerHelperService,
                                   OptionsTemplateService optionsTemplateService,
                                   PwaApplicationRedirectService pwaApplicationRedirectService) {
    super(padFileService);
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.controllerHelperService = controllerHelperService;
    this.optionsTemplateService = optionsTemplateService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
  }

  private ModelAndView getModelAndView(PwaApplicationContext applicationContext,
                                       OptionsTemplateForm form) {

    var modelAndView = this.createModelAndView(
        "pwaApplication/options/optionsTemplate",
        applicationContext.getApplicationDetail(),
        FILE_PURPOSE,
        form
    );

    applicationBreadcrumbService.fromTaskList(applicationContext.getPwaApplication(), modelAndView, "Options template");

    return modelAndView;

  }

  @GetMapping
  public ModelAndView renderOptionsTemplate(@PathVariable("applicationId") Integer applicationId,
                                            @PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") OptionsTemplateForm form,
                                            AuthenticatedUserAccount user) {
    var detail = applicationContext.getApplicationDetail();
    padFileService.mapFilesToForm(form, detail, FILE_PURPOSE);
    return getModelAndView(applicationContext, form);
  }

  @PostMapping
  public ModelAndView postOptionsTemplate(@PathVariable("applicationId") Integer applicationId,
                                          @PathVariable("applicationType")
                                          @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                          PwaApplicationContext applicationContext,
                                          @ModelAttribute("form") OptionsTemplateForm form,
                                          BindingResult bindingResult,
                                          AuthenticatedUserAccount user,
                                          ValidationType validationType) {

    var detail = applicationContext.getApplicationDetail();
    bindingResult = optionsTemplateService.validate(form, bindingResult, validationType, detail);

    return controllerHelperService.checkErrorsAndRedirect(bindingResult, getModelAndView(applicationContext, form), () -> {

      padFileService.updateFiles(
          form,
          applicationContext.getApplicationDetail(),
          FILE_PURPOSE,
          FileUpdateMode.DELETE_UNLINKED_FILES,
          applicationContext.getUser());

      return pwaApplicationRedirectService.getTaskListRedirect(detail.getPwaApplication());

    });

  }

  @PostMapping("/files/upload")
  @ResponseBody
  public FileUploadResult handleUpload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @RequestParam("file") MultipartFile file,
      PwaApplicationContext applicationContext) {

    return padFileService.processInitialUpload(
        file,
        applicationContext.getApplicationDetail(),
        FILE_PURPOSE,
        applicationContext.getUser());

  }

  @GetMapping("/files/download/{fileId}")
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.VIEW})
  @ResponseBody
  public ResponseEntity<Resource> handleDownload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    return serveFile(applicationContext.getPadFile());
  }

  @PostMapping("/files/delete/{fileId}")
  @ResponseBody
  public FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    return padFileService.processFileDeletion(applicationContext.getPadFile(), applicationContext.getUser());
  }

}
