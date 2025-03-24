package uk.co.ogauthority.pwa.features.application.tasks.projectinfo.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationStatusCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.PadProjectExtensionService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PermanentDepositMade;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationForm;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.ProjectInformationQuestion;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceApplicationsRestController;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceTransaction;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicenceapplications.PearsLicenceTransactionService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
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
public class ProjectInformationController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadProjectInformationService padProjectInformationService;
  private final ControllerHelperService controllerHelperService;
  private final PadProjectExtensionService projectExtensionService;

  private final PearsLicenceTransactionService pearsLicenceTransactionService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public ProjectInformationController(ApplicationBreadcrumbService applicationBreadcrumbService,
                                      PwaApplicationRedirectService pwaApplicationRedirectService,
                                      PadProjectInformationService padProjectInformationService,
                                      ControllerHelperService controllerHelperService,
                                      PadProjectExtensionService projectExtensionService,
                                      PearsLicenceTransactionService pearsLicenceTransactionService,
                                      PadFileManagementService padFileManagementService
  ) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padProjectInformationService = padProjectInformationService;
    this.controllerHelperService = controllerHelperService;
    this.projectExtensionService = projectExtensionService;
    this.pearsLicenceTransactionService = pearsLicenceTransactionService;
    this.padFileManagementService = padFileManagementService;
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
    //TODO: Move to submission clean up - requires refactor to allow cleaning of hidden task list items.
    if (!projectExtensionService.canShowInTaskList(applicationContext.getApplicationDetail())) {
      projectExtensionService.removeExtensionsForProject(applicationContext);
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

    List<PearsLicenceTransaction> licenceApplications = new ArrayList<>();
    if (form.getPearsApplicationList() != null) {
      licenceApplications = pearsLicenceTransactionService.getApplicationsByIds(
          Arrays.stream(form.getPearsApplicationList())
              .map(Integer::valueOf)
              .collect(Collectors.toList()));
    }

    var fileUploadAttributes = padFileManagementService.getFileUploadComponentAttributes(
        form.getUploadedFiles(),
        pwaApplicationDetail,
        FileDocumentType.PROJECT_INFORMATION
    );

    var modelAndView = new ModelAndView("pwaApplication/shared/projectInformation")
        .addObject("permanentDepositsMadeOptions", PermanentDepositMade.asList(pwaApplicationDetail.getPwaApplicationType()))
        .addObject("isFdpQuestionRequiredBasedOnField", padProjectInformationService.isFdpQuestionRequired(pwaApplicationDetail))
        .addObject("requiredQuestions", padProjectInformationService.getRequiredQuestions(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getResourceType()))
        .addObject("isPipelineDeploymentQuestionOptional",
            ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT.isOptionalForType(pwaApplicationDetail.getPwaApplicationType()))
        .addObject("timelineGuidance", projectExtensionService.getProjectTimelineGuidance(pwaApplicationDetail))
        .addObject("selectedLicenceApplications", licenceApplications)
        .addObject("licenceApplicationListUrl",
            ReverseRouter.route(on(PearsLicenceApplicationsRestController.class).getApplications(null)))
        .addObject("fileUploadAttributes", fileUploadAttributes);

    applicationBreadcrumbService.fromTaskList(pwaApplicationDetail.getPwaApplication(), modelAndView,
        "Project information");
    return modelAndView;
  }
}
