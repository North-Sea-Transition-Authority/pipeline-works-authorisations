package uk.co.ogauthority.pwa.features.application.tasks.projectinfo.controller;

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
import uk.co.ogauthority.pwa.controller.files.PwaApplicationDetailDataFileUploadAndDownloadController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.MaxCompletionPeriod;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.PadProjectExtensionService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PermanentDepositMade;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationForm;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationQuestion;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/project-information")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.DEPOSIT_CONSENT,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.OPTIONS_VARIATION,
    PwaApplicationType.DECOMMISSIONING,
    PwaApplicationType.HUOO_VARIATION
})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class ProjectInformationController extends PwaApplicationDetailDataFileUploadAndDownloadController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadProjectInformationService padProjectInformationService;
  private final ControllerHelperService controllerHelperService;

  private final PadProjectExtensionService projectExtensionService;

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.PROJECT_INFORMATION;

  @Autowired
  public ProjectInformationController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                      PwaApplicationRedirectService pwaApplicationRedirectService,
                                      PadProjectInformationService padProjectInformationService,
                                      PadFileService padFileService,
                                      ControllerHelperService controllerHelperService,
                                      PadProjectExtensionService projectExtensionService) {
    super(padFileService);
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padProjectInformationService = padProjectInformationService;
    this.controllerHelperService = controllerHelperService;
    this.projectExtensionService = projectExtensionService;
  }

  @GetMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView renderProjectInformation(@PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @PathVariable("applicationId") Integer applicationId,
                                               PwaApplicationContext applicationContext,
                                               @ModelAttribute("form") ProjectInformationForm form) {
    var entity = padProjectInformationService.getPadProjectInformationData(applicationContext.getApplicationDetail());
    padProjectInformationService.mapEntityToForm(entity, form);
    return getProjectInformationModelAndView(applicationContext.getApplicationDetail(), form);
  }

  @PostMapping
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public ModelAndView postProjectInformation(@PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @PathVariable("applicationId") Integer applicationId,
                                             PwaApplicationContext applicationContext,
                                             @ModelAttribute("form") ProjectInformationForm form,
                                             BindingResult bindingResult,
                                             ValidationType validationType) {

    bindingResult = padProjectInformationService.validate(form,
        bindingResult,
        validationType,
        applicationContext.getApplicationDetail());

    //Remove extension permission uploads for any project that now no longer needs it.
    //Move to submission clean up - requires refactor to allow cleaning of hidden task list items.
    if (!projectExtensionService.canShowInTaskList(applicationContext.getApplicationDetail())) {
      var extensionFiles = padFileService.getAllByPwaApplicationDetailAndPurpose(
          applicationContext.getApplicationDetail(),
          ApplicationDetailFilePurpose.PROJECT_EXTENSION);
      extensionFiles.forEach(file -> padFileService.processFileDeletion(file, applicationContext.getUser()));
    }

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        // if invalid form, get all files, including not yet saved ones as they may have errored.
        getProjectInformationModelAndView(applicationContext.getApplicationDetail(), form), () -> {

          var entity = padProjectInformationService.getPadProjectInformationData(applicationContext.getApplicationDetail());
          padProjectInformationService.saveEntityUsingForm(entity, form, applicationContext.getUser());
          return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());

        });

  }

  private ModelAndView getProjectInformationModelAndView(PwaApplicationDetail pwaApplicationDetail,
                                                         ProjectInformationForm form) {

    var modelAndView = this.createModelAndView(
        "pwaApplication/shared/projectInformation",
        pwaApplicationDetail,
        FILE_PURPOSE,
        form
    );

    modelAndView.addObject("permanentDepositsMadeOptions", PermanentDepositMade.asList(pwaApplicationDetail.getPwaApplicationType()))
        .addObject("isFdpQuestionRequiredBasedOnField", padProjectInformationService.isFdpQuestionRequired(pwaApplicationDetail))
        .addObject("requiredQuestions", padProjectInformationService.getRequiredQuestions(
            pwaApplicationDetail.getPwaApplicationType()))
        .addObject("isPipelineDeploymentQuestionOptional",
            ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT.isOptionalForType(pwaApplicationDetail.getPwaApplicationType()))
        .addObject("timelineGuidance", getProjectTimelineGuidance(pwaApplicationDetail));

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Project information");
    return modelAndView;
  }

  private String getProjectTimelineGuidance(PwaApplicationDetail pwaApplicationDetail) {
    var projectType = MaxCompletionPeriod.valueOf(pwaApplicationDetail.getPwaApplicationType().name());
    var guidance = "For example, 31 3 2023 \n";
    guidance += String.format("This must be within %s months of the proposed start of works date. ",
        projectType.getMaxMonthsCompletion());

    if (projectType.isExtendable()) {
      guidance += "\n Unless prior approval has been received from the Consents and Authorisations Manager.";
    }
    return guidance;
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

  @PostMapping("/files/upload")
  @ResponseBody
  @PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
  public FileUploadResult handleUpload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @RequestParam("file") MultipartFile file,
      PwaApplicationContext applicationContext) {

    return padFileService.processImageUpload(
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
