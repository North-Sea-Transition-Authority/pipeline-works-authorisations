package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.masterpwas.contacts.PwaContactController;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.fields.InitialFieldsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.FastTrackController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.HuooController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.LocationDetailsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.campaignworks.CampaignWorksController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.crossings.CrossingAgreementsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositDrawingsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PipelinesHuooController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelines.PipelinesController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinetechinfo.FluidCompositionInfoController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinetechinfo.PipelineTechInfoController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.submission.ReviewAndSubmitController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.techdrawings.TechnicalDrawingsController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaViewService;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;

@Service
public class TaskListService {

  private final ApplicationContext applicationContext;
  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ApplicationBreadcrumbService breadcrumbService;
  private final TaskCompletionService taskCompletionService;
  private final PwaContactService pwaContactService;
  private final MasterPwaViewService masterPwaViewService;

  @Autowired
  public TaskListService(ApplicationContext applicationContext,
                         PwaApplicationRedirectService pwaApplicationRedirectService,
                         ApplicationBreadcrumbService breadcrumbService,
                         TaskCompletionService taskCompletionService,
                         PwaContactService pwaContactService,
                         MasterPwaViewService masterPwaViewService) {
    this.applicationContext = applicationContext;
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.breadcrumbService = breadcrumbService;
    this.taskCompletionService = taskCompletionService;
    this.pwaContactService = pwaContactService;
    this.masterPwaViewService = masterPwaViewService;
  }

  @VisibleForTesting
  public LinkedHashMap<String, String> getPwaInfoTasks(PwaApplication application) {

    var tasks = new LinkedHashMap<String, String>();

    if (application.getApplicationType().equals(PwaApplicationType.INITIAL)) {

      tasks.put("Field information",
          ReverseRouter.route(on(InitialFieldsController.class)
              .renderFields(application.getApplicationType(), application.getId(), null, null, null)));

    } else {
      tasks.put("No tasks", pwaApplicationRedirectService.getTaskListRoute(application));
    }

    return tasks;

  }

  @VisibleForTesting
  public List<TaskListEntry> getAppInfoTasks(PwaApplication application) {
    return List.of(
        new TaskListEntry(
            "Application contacts",
            ReverseRouter.route(on(PwaContactController.class)
                .renderContactsScreen(application.getApplicationType(), application.getId(), null)),
            false,
            List.of(new TaskInfo("CONTACT", pwaContactService.countContactsByPwaApplication(application))))
    );
  }

  @VisibleForTesting
  public List<TaskListEntry> getPrepareAppTasks(PwaApplicationDetail detail) {

    List<TaskListEntry> tasks = new ArrayList<>();

    ApplicationTask.stream()
        .sorted(Comparator.comparing(ApplicationTask::getDisplayOrder))
        .forEachOrdered(task -> checkTaskAndAddToList(tasks, task, detail));

    if (tasks.isEmpty()) {
      tasks.add(
          new TaskListEntry("No tasks", pwaApplicationRedirectService.getTaskListRoute(detail.getPwaApplication()),
              false));
    }

    return tasks;

  }

  private void checkTaskAndAddToList(List<TaskListEntry> tasks, ApplicationTask task, PwaApplicationDetail detail) {

    Set<PwaApplicationType> validApplicationTypes = Optional.ofNullable(task.getControllerClass())
        // this allows us to test method logic by returning an arbitrary class from the applicationContext in tests
        .map(controllerClass -> applicationContext.getBean(controllerClass).getClass())
        .map(clazz -> clazz.getAnnotation(PwaApplicationTypeCheck.class))
        .map(typeCheck -> Set.of(typeCheck.types()))
        // task has valid app type if controller has no type restriction
        .orElse(EnumSet.allOf(PwaApplicationType.class));

    var taskIsShown = Optional.ofNullable(task.getServiceClass())
        .map(service -> applicationContext.getBean(service))
        .map(service -> ((ApplicationFormSectionService) service).canShowInTaskList(detail))
        // always show task if no service is provided
        .orElse(true);

    if (validApplicationTypes.contains(detail.getPwaApplicationType()) && taskIsShown) {
      tasks.add(createTaskListEntry(task, detail));
    }
  }

  private TaskListEntry createTaskListEntry(ApplicationTask task, PwaApplicationDetail detail) {

    var applicationId = detail.getPwaApplication().getId();
    var applicationType = detail.getPwaApplicationType();

    return new TaskListEntry(
        task.getDisplayName(),
        getRouteForTask(task, applicationType, applicationId),
        taskCompletionService.isTaskComplete(detail, task));
  }

  @VisibleForTesting
  public String getRouteForTask(ApplicationTask task, PwaApplicationType applicationType, int applicationId) {
    Map<String, Object> uriVariables = new HashMap<>();
    uriVariables.put("applicationId", applicationId);
    switch (task) {
      case PROJECT_INFORMATION:
        return ReverseRouter.route(on(ProjectInformationController.class)
            .renderProjectInformation(applicationType, applicationId, null, null));
      case FAST_TRACK:
        return ReverseRouter.route(on(FastTrackController.class)
            .renderFastTrack(applicationType, applicationId, null, null, null));
      case ENVIRONMENTAL_DECOMMISSIONING:
        return ReverseRouter.route(on(EnvironmentalDecomController.class)
            .renderEnvDecom(applicationType, null, null), uriVariables);
      case CROSSING_AGREEMENTS:
        return ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(applicationType, applicationId, null, null));
      case LOCATION_DETAILS:
        return ReverseRouter.route(on(LocationDetailsController.class)
            .renderLocationDetails(applicationType, null, null, null), Map.of("applicationId", applicationId));
      case HUOO:
        return ReverseRouter.route(on(HuooController.class)
            .renderHuooSummary(applicationType, applicationId, null, null));
      case PIPELINES:
        return ReverseRouter.route(on(PipelinesController.class)
            .renderPipelinesOverview(applicationId, applicationType, null));
      case PIPELINES_HUOO:
        return ReverseRouter.route(on(PipelinesHuooController.class)
            .renderSummary(applicationType, applicationId, null));
      case CAMPAIGN_WORKS:
        return ReverseRouter.route(on(CampaignWorksController.class)
            .renderSummary(applicationType, applicationId, null));
      case TECHNICAL_DRAWINGS:
        return ReverseRouter.route(on(TechnicalDrawingsController.class)
            .renderOverview(applicationType, applicationId, null, null));
      case PERMANENT_DEPOSITS:
        return ReverseRouter.route(on(PermanentDepositController.class)
            .renderPermanentDepositsOverview(applicationType, applicationId, null, null));
      case PERMANENT_DEPOSIT_DRAWINGS:
        return ReverseRouter.route(on(PermanentDepositDrawingsController.class)
            .renderDepositDrawingsOverview(applicationType, applicationId, null, null));
      case GENERAL_TECH_DETAILS:
        return ReverseRouter.route(on(PipelineTechInfoController.class)
            .renderAddPipelineTechInfo(applicationType, applicationId, null, null));
      case FLUID_COMPOSITION:
        return ReverseRouter.route(on(FluidCompositionInfoController.class)
            .renderAddFluidCompositionInfo(applicationType, applicationId, null, null));
      case DESIGN_OP_CONDITIONS:
        return ReverseRouter.route(on(DesignOpConditionsController.class)
            .renderAddDesignOpConditions(applicationType, applicationId, null, null));
      default:
        return "";
    }
  }

  @VisibleForTesting
  public String getTaskListTemplatePath(PwaApplicationType applicationType) {
    switch (applicationType) {
      case INITIAL:
        return "pwaApplication/initial/initialTaskList";
      case CAT_1_VARIATION:
        return "pwaApplication/category1/cat1TaskList";
      case CAT_2_VARIATION:
        return "pwaApplication/category2/cat2TaskList";
      case HUOO_VARIATION:
        return "pwaApplication/huooVariation/huooTaskList";
      case DEPOSIT_CONSENT:
        return "pwaApplication/depositConsent/depositConsentTaskList";
      case DECOMMISSIONING:
        return "pwaApplication/decommissioning/decommissioningTaskList";
      case OPTIONS_VARIATION:
        return "pwaApplication/optionsVariation/optionsVariationTaskList";
      default:
        return "";
    }
  }

  private TaskListEntry getSubmissionTask(PwaApplicationDetail detail) {
    return new TaskListEntry(
        "Review and submit application",
        ReverseRouter.route(on(ReviewAndSubmitController.class)
            .review(detail.getPwaApplicationType(), detail.getMasterPwaApplicationId(), null)),
        false
    );
  }

  public ModelAndView getTaskListModelAndView(PwaApplicationDetail pwaApplicationDetail) {

    var modelAndView = new ModelAndView(getTaskListTemplatePath(pwaApplicationDetail.getPwaApplicationType()))
        .addObject("pwaInfoTasks", getPwaInfoTasks(pwaApplicationDetail.getPwaApplication()))
        .addObject("appInfoTasks", getAppInfoTasks(pwaApplicationDetail.getPwaApplication()))
        .addObject("prepareAppTasks", getPrepareAppTasks(pwaApplicationDetail))
        .addObject("submissionTask", getSubmissionTask(pwaApplicationDetail));

    if (pwaApplicationDetail.getPwaApplicationType() != PwaApplicationType.INITIAL) {
      modelAndView.addObject("masterPwaReference",
          masterPwaViewService.getCurrentMasterPwaView(pwaApplicationDetail.getPwaApplication()).getReference());
    }

    breadcrumbService.fromWorkArea(modelAndView, "Task list");

    return modelAndView;

  }
}
