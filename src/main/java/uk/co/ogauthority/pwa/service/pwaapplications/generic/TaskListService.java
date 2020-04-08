package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.masterpwas.contacts.PwaContactController;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.PwaHolderController;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.fields.InitialFieldsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.CrossingAgreementsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.FastTrackController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.LocationDetailsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;

@Service
public class TaskListService {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ApplicationBreadcrumbService breadcrumbService;
  private final PadFastTrackService padFastTrackService;
  private final TaskCompletionService taskCompletionService;

  @Autowired
  public TaskListService(PwaApplicationRedirectService pwaApplicationRedirectService,
                         ApplicationBreadcrumbService breadcrumbService,
                         PadFastTrackService padFastTrackService,
                         TaskCompletionService taskCompletionService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.breadcrumbService = breadcrumbService;
    this.padFastTrackService = padFastTrackService;
    this.taskCompletionService = taskCompletionService;
  }

  @VisibleForTesting
  public LinkedHashMap<String, String> getPwaInfoTasks(PwaApplication application) {

    var tasks = new LinkedHashMap<String, String>();

    if (application.getApplicationType().equals(PwaApplicationType.INITIAL)) {

      tasks.put("Consent holder",
          ReverseRouter.route(on(PwaHolderController.class)
              .renderHolderScreen(application.getApplicationType(), application.getId(), null, null)));

      tasks.put("Field information",
          ReverseRouter.route(on(InitialFieldsController.class)
              .renderFields(application.getApplicationType(), application.getId(), null, null)));

    } else {
      tasks.put("No tasks", pwaApplicationRedirectService.getTaskListRoute(application));
    }

    return tasks;

  }

  @VisibleForTesting
  public LinkedHashMap<String, String> getAppInfoTasks(PwaApplication application) {
    return new LinkedHashMap<>() {
      {
        put("Application contacts",
            ReverseRouter.route(on(PwaContactController.class)
                .renderContactsScreen(application.getApplicationType(), application.getId(), null)));
      }
    };
  }

  @VisibleForTesting
  public List<TaskListEntry> getPrepareAppTasks(PwaApplicationDetail detail) {

    List<TaskListEntry> tasks = new ArrayList<>();

    ApplicationTask.stream()
        .sorted(Comparator.comparing(ApplicationTask::getDisplayOrder))
        .forEachOrdered(task -> checkTaskAndAddToList(tasks, task, detail));

    if (tasks.isEmpty()) {
      tasks.add(new TaskListEntry("No tasks", pwaApplicationRedirectService.getTaskListRoute(detail.getPwaApplication()), false));
    }

    return tasks;

  }

  private void checkTaskAndAddToList(List<TaskListEntry> tasks, ApplicationTask task, PwaApplicationDetail detail) {

    Optional.ofNullable(task.getControllerClass().getAnnotation(PwaApplicationTypeCheck.class)).ifPresentOrElse(
        typeCheck -> {
          if (Arrays.asList(typeCheck.types()).contains(detail.getPwaApplicationType())) {
            addTask(tasks, task, detail);
          }
        },
        () -> addTask(tasks, task, detail)
    );

  }

  private void addTask(List<TaskListEntry> tasks, ApplicationTask task, PwaApplicationDetail detail) {

    var applicationId = detail.getPwaApplication().getId();
    var applicationType = detail.getPwaApplicationType();

    if (task != ApplicationTask.FAST_TRACK || padFastTrackService.isFastTrackRequired(detail)) {
      tasks.add(new TaskListEntry(
          task.getDisplayName(),
          getRouteForTask(task, applicationType, applicationId),
          taskCompletionService.isTaskComplete(detail, task)));
    }

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
            .renderEnvDecom(applicationType, null, null, null), uriVariables);
      case CROSSING_AGREEMENTS:
        return ReverseRouter.route(on(CrossingAgreementsController.class)
            .renderCrossingAgreementsOverview(applicationType, null, null), Map.of("applicationId", applicationId));
      case LOCATION_DETAILS:
        return ReverseRouter.route(on(LocationDetailsController.class)
            .renderLocationDetails(applicationType, null, null, null), Map.of("applicationId", applicationId));
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

  public ModelAndView getTaskListModelAndView(PwaApplicationDetail pwaApplicationDetail) {

    var modelAndView = new ModelAndView(getTaskListTemplatePath(pwaApplicationDetail.getPwaApplicationType()))
        .addObject("pwaInfoTasks", getPwaInfoTasks(pwaApplicationDetail.getPwaApplication()))
        .addObject("appInfoTasks", getAppInfoTasks(pwaApplicationDetail.getPwaApplication()))
        .addObject("prepareAppTasks", getPrepareAppTasks(pwaApplicationDetail));

    // TODO: PWA-361 - Remove hard-coded "PWA-Example-BP-2".
    if (pwaApplicationDetail.getPwaApplicationType() != PwaApplicationType.INITIAL) {
      modelAndView.addObject("masterPwaReference", "PWA-Example-BP-2");
    }

    breadcrumbService.fromWorkArea(modelAndView, "Task list");

    return modelAndView;

  }
}
