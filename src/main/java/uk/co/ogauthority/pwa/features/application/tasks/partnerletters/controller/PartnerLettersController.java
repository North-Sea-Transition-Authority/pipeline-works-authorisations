package uk.co.ogauthority.pwa.features.application.tasks.partnerletters.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDetailDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PadPartnerLettersService;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/partner-letters")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
public class PartnerLettersController extends PwaApplicationDetailDataFileUploadAndDownloadController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadPartnerLettersService padPartnerLettersService;
  private final ControllerHelperService controllerHelperService;
  private final String partnerLettersTemplateLink;

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.PARTNER_LETTERS;

  @Autowired
  public PartnerLettersController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                  PwaApplicationRedirectService pwaApplicationRedirectService,
                                  PadPartnerLettersService padPartnerLettersService,
                                  PadFileService padFileService,
                                  ControllerHelperService controllerHelperService,
                                  @Value("${oga.partnerletters.template.link}") String partnerLettersTemplateLink) {
    super(padFileService);
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padPartnerLettersService = padPartnerLettersService;
    this.controllerHelperService = controllerHelperService;
    this.partnerLettersTemplateLink = partnerLettersTemplateLink;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderAddPartnerLetters(@PathVariable("applicationType")
                                              @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                              @PathVariable("applicationId") Integer applicationId,
                                              PwaApplicationContext applicationContext,
                                              @ModelAttribute("form") PartnerLettersForm form) {
    padPartnerLettersService.mapEntityToForm(applicationContext.getApplicationDetail(), form);
    return getAddPartnerLettersModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postAddPartnerLetters(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") PartnerLettersForm form,
                                            BindingResult bindingResult,
                                            ValidationType validationType) {

    bindingResult = padPartnerLettersService.validate(form, bindingResult, validationType, applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        getAddPartnerLettersModelAndView(applicationContext.getApplicationDetail(), form), () -> {
          padPartnerLettersService.saveEntityUsingForm(applicationContext.getApplicationDetail(), form, applicationContext.getUser());
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        });

  }

  private ModelAndView getAddPartnerLettersModelAndView(PwaApplicationDetail pwaApplicationDetail, PartnerLettersForm form) {

    var modelAndView = this.createModelAndView(
        "pwaApplication/shared/partnerletters/partnerLetters", pwaApplicationDetail, FILE_PURPOSE, form)
        .addObject("partnerLettersTemplateLink", partnerLettersTemplateLink);

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView, "Partner approval letters");

    return modelAndView;

  }

  //File handle Endpoints
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

  @PostMapping("/files/upload")
  @ResponseBody
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
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

  @PostMapping("/files/delete/{fileId}")
  @ResponseBody
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    return padFileService.processFileDeletion(applicationContext.getPadFile(), applicationContext.getUser());
  }

}