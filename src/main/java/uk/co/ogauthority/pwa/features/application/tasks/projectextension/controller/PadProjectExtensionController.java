package uk.co.ogauthority.pwa.features.application.tasks.projectextension.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.time.ZoneId;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDetailDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.PadProjectExtensionService;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.ProjectExtensionForm;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.controller.ProjectInformationController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/project-extension")
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class PadProjectExtensionController extends PwaApplicationDetailDataFileUploadAndDownloadController {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;

  private final PadProjectInformationService padProjectInformationService;

  private final PadProjectExtensionService  projectExtensionService;

  private final ControllerHelperService controllerHelperService;

  private final String ogaConsentsMailboxEmail;

  public PadProjectExtensionController(PadFileService padFileService,
                                       PwaApplicationRedirectService pwaApplicationRedirectService,
                                       PadProjectInformationService padProjectInformationService,
                                       PadProjectExtensionService projectExtensionService,
                                       ControllerHelperService controllerHelperService,
                                       @Value("${oga.consents.email}") String ogaConsentsMailboxEmail) {
    super(padFileService);
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padProjectInformationService = padProjectInformationService;
    this.projectExtensionService = projectExtensionService;
    this.controllerHelperService = controllerHelperService;
    this.ogaConsentsMailboxEmail = ogaConsentsMailboxEmail;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderProjectExtension(PwaApplicationContext applicationContext,
                                             @PathVariable("applicationId") Integer applicationId,
                                             @PathVariable("applicationType") @ApplicationTypeUrl
                                               PwaApplicationType pwaApplicationType,
                                             @ModelAttribute("form") ProjectExtensionForm form) {
    var applicationDetail = applicationContext.getApplicationDetail();
    padFileService.mapFilesToForm(form, applicationDetail, ApplicationDetailFilePurpose.PROJECT_EXTENSION);
    var projectInformation = padProjectInformationService.getPadProjectInformationData(applicationDetail);

    var startDate = LocalDate.ofInstant(projectInformation.getProposedStartTimestamp(), ZoneId.systemDefault());
    var endDate = LocalDate.ofInstant(projectInformation.getLatestCompletionTimestamp(), ZoneId.systemDefault());

    return this.createModelAndView(
        "pwaApplication/shared/projectExtension",
        applicationDetail,
        ApplicationDetailFilePurpose.PROJECT_EXTENSION,
        form)
        .addObject("ogaConsentsEmail", ogaConsentsMailboxEmail)
        .addObject("startDate", DateUtils.formatDate(startDate))
        .addObject("endDate", DateUtils.formatDate(endDate))
        .addObject("modifyUrl", ReverseRouter.route(on(ProjectInformationController.class)
            .renderProjectInformation(applicationDetail.getPwaApplicationType(),
                applicationDetail.getMasterPwaApplicationId(),
                null,
                null)));
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postProjectExtension(PwaApplicationContext applicationContext,
                                           @ModelAttribute("form") ProjectExtensionForm projectExtensionForm,
                                           BindingResult bindingResult,
                                           ValidationType validationType,
                                           @PathVariable("applicationId") Integer applicationId,
                                           @PathVariable("applicationType") @ApplicationTypeUrl
                                             PwaApplicationType pwaApplicationType) {

    padFileService.updateFiles(
        projectExtensionForm,
        applicationContext.getApplicationDetail(),
        ApplicationDetailFilePurpose.PROJECT_EXTENSION,
        FileUpdateMode.DELETE_UNLINKED_FILES,
        applicationContext.getUser());

    bindingResult = projectExtensionService.validate(projectExtensionForm,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        renderProjectExtension(applicationContext,
            applicationId,
            pwaApplicationType,
            projectExtensionForm),
        () -> pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication()));
  }

  @PostMapping("/files/upload")
  @ResponseBody
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  @Override
  public FileUploadResult handleUpload(PwaApplicationType pwaApplicationType,
                                       Integer applicationId,
                                       MultipartFile file,
                                       PwaApplicationContext applicationContext) {


    return padFileService.processInitialUpload(
        file,
        applicationContext.getApplicationDetail(),
        ApplicationDetailFilePurpose.PROJECT_EXTENSION,
        applicationContext.getUser());
  }

  @GetMapping("/files/download/{fileId}")
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.VIEW})
  @ResponseBody
  @Override
  public ResponseEntity<Resource> handleDownload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    return serveFile(applicationContext.getPadFile());
  }

  @PostMapping("/files/delete/{fileId}")
  @ResponseBody
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  @Override
  public FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    return padFileService.processFileDeletion(applicationContext.getPadFile(), applicationContext.getUser());
  }
}
