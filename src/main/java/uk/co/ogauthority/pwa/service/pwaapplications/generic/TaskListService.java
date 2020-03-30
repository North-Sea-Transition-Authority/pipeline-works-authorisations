package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.masterpwas.contacts.PwaContactController;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.PwaHolderController;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.fields.InitialFieldsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PwaApplicationTypeCheck;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;

@Service
public class TaskListService {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ApplicationBreadcrumbService breadcrumbService;

  @Autowired
  public TaskListService(PwaApplicationRedirectService pwaApplicationRedirectService,
                         ApplicationBreadcrumbService breadcrumbService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.breadcrumbService = breadcrumbService;
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
  public LinkedHashMap<String, String> getPrepareAppTasks(PwaApplication application) {

    var tasks = new LinkedHashMap<String, String>();

    ApplicationTask.stream()
        .sorted(Comparator.comparing(ApplicationTask::getDisplayOrder))
        .forEachOrdered(task -> addTaskToList(tasks, task, application));

    if (tasks.isEmpty()) {
      tasks.put("No tasks", pwaApplicationRedirectService.getTaskListRoute(application));
    }

    return tasks;

  }

  private void addTaskToList(LinkedHashMap<String, String> tasks, ApplicationTask task, PwaApplication application) {

    var applicationId = application.getId();
    var applicationType = application.getApplicationType();

    Optional.ofNullable(task.getControllerClass().getAnnotation(PwaApplicationTypeCheck.class)).ifPresentOrElse(
        typeCheck -> {
          if (Arrays.asList(typeCheck.types()).contains(applicationType)) {
            tasks.put(task.getDisplayName(), getRouteForTask(task, applicationType, applicationId));
          }
        },
        () -> tasks.put(task.getDisplayName(), getRouteForTask(task, applicationType, applicationId))
    );

  }

  @VisibleForTesting
  public String getRouteForTask(ApplicationTask task, PwaApplicationType applicationType, int applicationId) {
    Map<String, Object> uriVariables = new HashMap<>();
    uriVariables.put("applicationId", applicationId);
    switch (task) {
      case PROJECT_INFORMATION:
        return ReverseRouter.route(on(ProjectInformationController.class)
            .renderProjectInformation(applicationType, null, null, null), uriVariables);
      case ENVIRONMENTAL_DECOMMISSIONING:
        return ReverseRouter.route(on(EnvironmentalDecomController.class)
            .renderEnvDecom(applicationType, null, null, null), uriVariables);
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

  public ModelAndView getTaskListModelAndView(PwaApplication pwaApplication) {

    var modelAndView = new ModelAndView(getTaskListTemplatePath(pwaApplication.getApplicationType()))
        .addObject("pwaInfoTasks", getPwaInfoTasks(pwaApplication))
        .addObject("appInfoTasks", getAppInfoTasks(pwaApplication))
        .addObject("prepareAppTasks", getPrepareAppTasks(pwaApplication));

    // TODO: PWA-361 - Remove hard-coded "PWA-Example-BP-2".
    if (pwaApplication.getApplicationType() != PwaApplicationType.INITIAL) {
      modelAndView.addObject("masterPwaReference", "PWA-Example-BP-2");
    }

    breadcrumbService.fromWorkArea(modelAndView, "Task list");

    return modelAndView;

  }
}
