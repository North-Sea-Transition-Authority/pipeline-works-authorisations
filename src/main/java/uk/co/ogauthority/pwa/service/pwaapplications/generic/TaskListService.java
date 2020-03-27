package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.LinkedHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.masterpwas.contacts.PwaContactController;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.PwaHolderController;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.fields.InitialFieldsController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ApplicationTypeRestriction;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.EnvironmentalDecomController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.FastTrackController;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.ProjectInformationController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;

@Service
public class TaskListService {

  private final PwaApplicationRedirectService pwaApplicationRedirectService;
  private final ApplicationBreadcrumbService breadcrumbService;
  private final PwaApplicationDetailService pwaApplicationDetailService;
  private final PadFastTrackService padFastTrackService;

  @Autowired
  public TaskListService(PwaApplicationRedirectService pwaApplicationRedirectService,
                         ApplicationBreadcrumbService breadcrumbService,
                         PwaApplicationDetailService pwaApplicationDetailService,
                         PadFastTrackService padFastTrackService) {
    this.pwaApplicationRedirectService = pwaApplicationRedirectService;
    this.breadcrumbService = breadcrumbService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.padFastTrackService = padFastTrackService;
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
                .renderContactsScreen(application.getApplicationType(), application.getId(),  null)));
      }
    };
  }

  @VisibleForTesting
  public LinkedHashMap<String, String> getPrepareAppTasks(PwaApplication application) {

    var detail = pwaApplicationDetailService.getTipDetailWithStatus(application.getId(), PwaApplicationStatus.DRAFT);

    var restrictions = new LinkedHashMap<String, Class>() {
      {
        put("Project information", ProjectInformationController.class);
        put("Environmental and decommissioning", EnvironmentalDecomController.class);
        compute("Fast-track", (key, value) -> {
          if (padFastTrackService.isFastTrackRequired(detail)) {
            return FastTrackController.class;
          }
          return null;
        });
      }
    };

    var routes = new LinkedHashMap<String, String>() {
      {
        put("Project information",
            ReverseRouter.route(on(ProjectInformationController.class)
                .renderProjectInformation(application.getApplicationType(), application.getId(), null, null)));
        put("Environmental and decommissioning",
            ReverseRouter.route(on(EnvironmentalDecomController.class)
                .renderEnvDecom(application.getApplicationType(), application.getId(), null, null)));
        put("Fast-track",
            ReverseRouter.route(on(FastTrackController.class)
                .renderFastTrack(application.getApplicationType(), application.getId(), null, null)));
      }
    };

    var builder = new LinkedHashMap<String, String>();
    restrictions.forEach((key, value) -> {
      var annotation = (ApplicationTypeRestriction) value.getAnnotation(ApplicationTypeRestriction.class);
      if (annotation != null) {
        // Check if appType is within restriction
        var contained = Arrays.stream(annotation.value())
            .anyMatch(type -> type == application.getApplicationType());
        if (contained) {
          builder.put(key, routes.get(key));
        }
      } else {
        // No annotation, controller is not restricted
        builder.put(key, routes.get(key));
      }
    });
    if (builder.isEmpty()) {
      builder.put("No tasks", pwaApplicationRedirectService.getTaskListRoute(application));
    }
    return builder;
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
