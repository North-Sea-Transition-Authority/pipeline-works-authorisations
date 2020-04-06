package uk.co.ogauthority.pwa.controller.pwaapplications.shared;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

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
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PwaApplicationFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.util.ControllerUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/project-information")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
public class ProjectInformationController extends PwaApplicationDataFileUploadAndDownloadController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadProjectInformationService padProjectInformationService;
  private final PwaApplicationFileService applicationFileService;

  @Autowired
  public ProjectInformationController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                      PwaApplicationRedirectService pwaApplicationRedirectService,
                                      PadProjectInformationService padProjectInformationService,
                                      PwaApplicationFileService applicationFileService) {
    this.applicationFileService = applicationFileService;
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padProjectInformationService = padProjectInformationService;
  }

  @GetMapping("/files/download/{fileId}")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT, PwaApplicationPermission.VIEW})
  @ResponseBody
  public ResponseEntity<Resource> handleDownload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    var projectInfoFile = padProjectInformationService.getProjectInformationFile(fileId,
        applicationContext.getApplicationDetail());
    return serveFile(applicationFileService.getUploadedFile(projectInfoFile));
  }

  @PostMapping("/files/upload")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  @ResponseBody
  public FileUploadResult handleUpload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @RequestParam("file") MultipartFile file,
      PwaApplicationContext applicationContext) {

    // not creating link project information link until Save is clicked.
    return applicationFileService.processApplicationFileUpload(
        file,
        applicationContext.getUser(),
        applicationContext.getApplicationDetail(),
        padProjectInformationService::createUploadedFileLink
    );
  }

  @PostMapping("/files/delete/{fileId}")
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  @ResponseBody
  public FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext) {
    return applicationFileService.processApplicationFileDelete(
        fileId,
        applicationContext.getApplicationDetail(),
        applicationContext.getUser(),
        padProjectInformationService::deleteUploadedFileLink
    );
  }

  @GetMapping
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView renderProjectInformation(@PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @PathVariable("applicationId") Integer applicationId,
                                               PwaApplicationContext applicationContext,
                                               @ModelAttribute("form") ProjectInformationForm form) {
    var entity = padProjectInformationService.getPadProjectInformationData(applicationContext.getApplicationDetail());
    padProjectInformationService.mapEntityToForm(entity, form, ApplicationFileLinkStatus.FULL);
    return getProjectInformationModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  @PwaApplicationStatusCheck(status = PwaApplicationStatus.DRAFT)
  @PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
  public ModelAndView postProjectInformation(@PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @PathVariable("applicationId") Integer applicationId,
                                             PwaApplicationContext applicationContext,
                                             @ModelAttribute("form") ProjectInformationForm form,
                                             BindingResult bindingResult,
                                             ValidationType validationType) {

    bindingResult = padProjectInformationService.validate(form, bindingResult, validationType);

    return ControllerUtils.checkErrorsAndRedirect(bindingResult,
        // if invalid form, get all files, including not yet saved ones as they may have errored.
        getProjectInformationModelAndView(applicationContext.getApplicationDetail(), form), () -> {
          var entity = padProjectInformationService.getPadProjectInformationData(
              applicationContext.getApplicationDetail()
          );
          padProjectInformationService.saveEntityUsingForm(entity, form, applicationContext.getUser());
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
        });

  }


  private ModelAndView getProjectInformationModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                         ProjectInformationForm form) {
    var modelAndView = this.createModelAndView(
        "pwaApplication/shared/projectInformation",
        ReverseRouter.route(on(ProjectInformationController.class).handleUpload(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null, null
        )),
        ReverseRouter.route(on(ProjectInformationController.class).handleDownload(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null, null
        )),
        ReverseRouter.route(on(ProjectInformationController.class).handleDelete(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getMasterPwaApplicationId(),
            null, null
        )),
        padProjectInformationService.getUpdatedProjectInformationFileViewsWhenFileOnForm(pwaApplicationDetail, form)
    );

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Project information");
    return modelAndView;
  }

}
