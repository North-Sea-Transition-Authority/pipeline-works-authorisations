package uk.co.ogauthority.pwa.features.application.tasks.campaignworks.controller;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
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
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.CampaignWorksService;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.CampaignWorksUrlFactory;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.WorkScheduleForm;
import uk.co.ogauthority.pwa.features.application.tasks.campaignworks.WorkScheduleView;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.projectextension.PadProjectExtensionService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.controller.ProjectInformationController;
import uk.co.ogauthority.pwa.model.form.enums.ScreenActionType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.validation.SummaryScreenValidationResult;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;

@Controller
@RequestMapping("/pwa-application/{applicationType}/{applicationId}/campaign-works")
@PwaApplicationTypeCheck(types = {
    PwaApplicationType.INITIAL,
    PwaApplicationType.CAT_1_VARIATION,
    PwaApplicationType.CAT_2_VARIATION,
    PwaApplicationType.DECOMMISSIONING
})
@PwaApplicationStatusCheck(statuses = {PwaApplicationStatus.DRAFT, PwaApplicationStatus.UPDATE_REQUESTED})
@PwaApplicationPermissionCheck(permissions = {PwaApplicationPermission.EDIT})
public class CampaignWorksController {

  private final ApplicationBreadcrumbService applicationBreadcrumbService;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final PadPipelineService padPipelineService;
  private final CampaignWorksService campaignWorksService;
  private final ControllerHelperService controllerHelperService;

  private final PadProjectExtensionService projectExtensionService;

  @Autowired
  public CampaignWorksController(
      ApplicationBreadcrumbService applicationBreadcrumbService,
      PwaApplicationRedirectService pwaApplicationRedirectService,
      PadPipelineService padPipelineService,
      CampaignWorksService campaignWorksService,
      ControllerHelperService controllerHelperService, PadProjectExtensionService projectExtensionService) {
    this.applicationBreadcrumbService = applicationBreadcrumbService;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.padPipelineService = padPipelineService;
    this.campaignWorksService = campaignWorksService;
    this.controllerHelperService = controllerHelperService;
    this.projectExtensionService = projectExtensionService;
  }

  private ModelAndView createWorkScheduleFormModelAndView(PwaApplicationContext applicationContext,
                                                          ScreenActionType screenActionType) {
    var modelAndView = new ModelAndView("pwaApplication/shared/campaignworks/workScheduleForm")
        .addObject("cancelUrl", ReverseRouter.route(on(CampaignWorksController.class)
            .renderSummary(applicationContext.getApplicationType(), applicationContext.getMasterPwaApplicationId(),
                null)))
        .addObject("pipelineViews", padPipelineService.getApplicationPipelineOverviews(applicationContext.getApplicationDetail()))
        .addObject("screenActionType", screenActionType)
        .addObject("timelineGuidance", projectExtensionService.getProjectTimelineGuidance(
            applicationContext.getApplicationDetail()));

    applicationBreadcrumbService.fromCampaignWorksOverview(applicationContext.getPwaApplication(), modelAndView,
        screenActionType.getActionText() + " work schedule");

    return modelAndView;
  }

  @GetMapping
  public ModelAndView renderSummary(@PathVariable("applicationType")
                                    @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                    @PathVariable("applicationId") int applicationId,
                                    PwaApplicationContext applicationContext) {
    return createSummaryModelAndView(applicationContext, null);
  }

  @PostMapping
  public ModelAndView postSummary(@PathVariable("applicationType")
                                  @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                  @PathVariable("applicationId") int applicationId,
                                  PwaApplicationContext applicationContext) {
    // Seems to be the pattern to accept a post on summary pages.
    // when section is "complete" redirect to task list, else simply reload page.
    var summaryValidationResult = campaignWorksService.getCampaignWorksValidationResult(
        applicationContext.getApplicationDetail());
    if (summaryValidationResult.isSectionComplete()) {
      return pwaApplicationRedirectService.getTaskListRedirect(applicationContext.getPwaApplication());
    } else {
      return createSummaryModelAndView(applicationContext, summaryValidationResult);
    }
  }

  private ModelAndView createSummaryModelAndView(PwaApplicationContext applicationContext,
                                                 SummaryScreenValidationResult summaryValidationResult) {
    var modelAndView = new ModelAndView("pwaApplication/shared/campaignworks/campaignWorks")
        .addObject("dependencySectionName", ApplicationTask.PROJECT_INFORMATION.getDisplayName())
        .addObject("dependencySectionUrl", ReverseRouter.route(on(ProjectInformationController.class)
            .renderProjectInformation(
                applicationContext.getApplicationType(),
                applicationContext.getMasterPwaApplicationId(),
                null,
                null)))
        .addObject("backUrl", pwaApplicationRedirectService.getTaskListRoute(applicationContext.getPwaApplication()))
        .addObject("urlFactory", new CampaignWorksUrlFactory(applicationContext.getApplicationDetail()))
        .addObject("summaryValidationResult", summaryValidationResult)
        .addObject("workScheduleViewList",
            campaignWorksService.getWorkScheduleViews(applicationContext.getApplicationDetail())
                .stream()
                .sorted(Comparator.comparing(WorkScheduleView::getWorkStartDate)
                    .thenComparing(WorkScheduleView::getWorkEndDate))
                .collect(Collectors.toList())
        );
    applicationBreadcrumbService.fromTaskList(applicationContext.getPwaApplication(), modelAndView, "Campaign Works");

    return modelAndView;
  }

  @GetMapping("/add")
  public ModelAndView renderAddWorkSchedule(@PathVariable("applicationType")
                                            @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                            @PathVariable("applicationId") int applicationId,
                                            PwaApplicationContext applicationContext,
                                            @ModelAttribute("form") WorkScheduleForm form) {

    return createWorkScheduleFormModelAndView(applicationContext, ScreenActionType.ADD);
  }

  @PostMapping("/add")
  public ModelAndView addWorkSchedule(@PathVariable("applicationType")
                                      @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                      @PathVariable("applicationId") int applicationId,
                                      PwaApplicationContext applicationContext,
                                      @ModelAttribute("form") WorkScheduleForm form,
                                      BindingResult bindingResult) {

    bindingResult = campaignWorksService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        createWorkScheduleFormModelAndView(applicationContext, ScreenActionType.ADD), () -> {
          campaignWorksService.addCampaignWorkScheduleFromForm(form, applicationContext.getApplicationDetail());
          return ReverseRouter.redirect(
              on(CampaignWorksController.class).renderSummary(pwaApplicationType, applicationId, null));

        });
  }

  @GetMapping("/{campaignWorkScheduleId}/edit")
  public ModelAndView renderEditWorkSchedule(@PathVariable("applicationType")
                                             @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                             @PathVariable("applicationId") int applicationId,
                                             @PathVariable("campaignWorkScheduleId") int campaignWorkScheduleId,
                                             PwaApplicationContext applicationContext,
                                             @ModelAttribute("form") WorkScheduleForm form) {
    var editWorkSchedule = campaignWorksService.getWorkScheduleOrError(
        applicationContext.getApplicationDetail(),
        campaignWorkScheduleId);
    campaignWorksService.mapWorkScheduleToForm(form, editWorkSchedule);
    return createWorkScheduleFormModelAndView(applicationContext, ScreenActionType.EDIT);
  }

  @PostMapping("/{campaignWorkScheduleId}/edit")
  public ModelAndView editWorkSchedule(@PathVariable("applicationType")
                                       @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                       @PathVariable("applicationId") int applicationId,
                                       @PathVariable("campaignWorkScheduleId") int campaignWorkScheduleId,
                                       PwaApplicationContext applicationContext,
                                       @ModelAttribute("form") WorkScheduleForm form,
                                       BindingResult bindingResult) {
    var editWorkSchedule = campaignWorksService.getWorkScheduleOrError(
        applicationContext.getApplicationDetail(),
        campaignWorkScheduleId);

    bindingResult = campaignWorksService.validate(
        form,
        bindingResult,
        ValidationType.FULL,
        applicationContext.getApplicationDetail()
    );

    return controllerHelperService.checkErrorsAndRedirect(bindingResult,
        createWorkScheduleFormModelAndView(applicationContext, ScreenActionType.EDIT), () -> {
          campaignWorksService.updateCampaignWorksScheduleFromForm(form, editWorkSchedule);
          return ReverseRouter.redirect(
              on(CampaignWorksController.class).renderSummary(pwaApplicationType, applicationId, null));
        });
  }


  @GetMapping("/{campaignWorkScheduleId}/remove")
  public ModelAndView renderRemoveWorkSchedule(@PathVariable("applicationType")
                                               @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                               @PathVariable("applicationId") int applicationId,
                                               @PathVariable("campaignWorkScheduleId") int campaignWorkScheduleId,
                                               PwaApplicationContext applicationContext) {
    var campaignWorkSchedule = campaignWorksService.getWorkScheduleOrError(
        applicationContext.getApplicationDetail(),
        campaignWorkScheduleId);

    var workScheduleView = campaignWorksService.createWorkScheduleView(campaignWorkSchedule);

    var modelAndView = new ModelAndView("pwaApplication/shared/campaignworks/removeWorkSchedule")
        .addObject("workSchedule", workScheduleView)
        .addObject("overviewUrl", ReverseRouter.route(on(CampaignWorksController.class)
            .renderSummary(pwaApplicationType, applicationId, null)));
    applicationBreadcrumbService.fromCampaignWorksOverview(applicationContext.getPwaApplication(), modelAndView,
        "Remove work schedule");
    return modelAndView;
  }

  @PostMapping("/{campaignWorkScheduleId}/remove")
  public ModelAndView removeWorkSchedule(@PathVariable("applicationType")
                                         @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
                                         @PathVariable("applicationId") int applicationId,
                                         @PathVariable("campaignWorkScheduleId") int campaignWorkScheduleId,
                                         PwaApplicationContext applicationContext) {
    var campaignWorkSchedule = campaignWorksService.getWorkScheduleOrError(
        applicationContext.getApplicationDetail(),
        campaignWorkScheduleId);

    campaignWorksService.removeCampaignWorksSchedule(campaignWorkSchedule);

    return ReverseRouter.redirect(
        on(CampaignWorksController.class).renderSummary(pwaApplicationType, applicationId, null));
  }

}
