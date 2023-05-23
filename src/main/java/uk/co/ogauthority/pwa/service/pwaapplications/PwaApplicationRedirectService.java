package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.creation.controller.PwaResourceTypeController;
import uk.co.ogauthority.pwa.features.application.creation.controller.StartPwaApplicationController;
import uk.co.ogauthority.pwa.features.application.creation.controller.StartVariationController;
import uk.co.ogauthority.pwa.features.application.tasklist.controllers.Category1TaskListController;
import uk.co.ogauthority.pwa.features.application.tasklist.controllers.Category2TaskListController;
import uk.co.ogauthority.pwa.features.application.tasklist.controllers.DecommissioningTaskListController;
import uk.co.ogauthority.pwa.features.application.tasklist.controllers.DepositConsentTaskListController;
import uk.co.ogauthority.pwa.features.application.tasklist.controllers.HuooVariationTaskListController;
import uk.co.ogauthority.pwa.features.application.tasklist.controllers.InitialTaskListController;
import uk.co.ogauthority.pwa.features.application.tasklist.controllers.OptionsVariationTaskListController;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class PwaApplicationRedirectService {

  /**
   * Return a redirect to the right start page for the passed-in application type.
   */
  public ModelAndView getStartApplicationRedirect(PwaApplicationType applicationType) {

    switch (applicationType) {
      case INITIAL:
        return ReverseRouter.redirect(on(PwaResourceTypeController.class).renderResourceTypeForm(null));
      case HUOO_VARIATION:
      case CAT_1_VARIATION:
      case CAT_2_VARIATION:
      case DEPOSIT_CONSENT:
      case OPTIONS_VARIATION:
      case DECOMMISSIONING:
        return ReverseRouter.redirect(on(StartVariationController.class).renderVariationTypeStartPage(applicationType));
      default:
        return ReverseRouter.redirect(on(StartPwaApplicationController.class).renderStartApplication(null));
    }

  }

  /**
   * Return a redirect to the right task list for the passed-in application.
   */
  public ModelAndView getTaskListRedirect(PwaApplication pwaApplication) {

    switch (pwaApplication.getApplicationType()) {
      case INITIAL:
        return ReverseRouter.redirect(on(InitialTaskListController.class).viewTaskList(pwaApplication.getId(), null));
      case CAT_1_VARIATION:
        return ReverseRouter.redirect(on(Category1TaskListController.class).viewTaskList(pwaApplication.getId(), null));
      case CAT_2_VARIATION:
        return ReverseRouter.redirect(on(Category2TaskListController.class).viewTaskList(pwaApplication.getId(), null));
      case DECOMMISSIONING:
        return ReverseRouter.redirect(on(DecommissioningTaskListController.class).viewTaskList(pwaApplication.getId(), null));
      case DEPOSIT_CONSENT:
        return ReverseRouter.redirect(on(DepositConsentTaskListController.class).viewTaskList(pwaApplication.getId(), null));
      case HUOO_VARIATION:
        return ReverseRouter.redirect(on(HuooVariationTaskListController.class).viewTaskList(pwaApplication.getId(), null));
      case OPTIONS_VARIATION:
        return ReverseRouter.redirect(on(OptionsVariationTaskListController.class).viewTaskList(pwaApplication.getId(), null));
      default:
        return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea(null, null, null));
    }
  }

  /**
   * Return a route to the right task list for the passed-in application.
   */
  public String getTaskListRoute(PwaApplication pwaApplication) {

    return getTaskListRoute(pwaApplication.getId(), pwaApplication.getApplicationType());
  }

  /**
   * Return a route to the right task list for the passed-in application.
   */
  public String getTaskListRoute(int pwaApplicationId, PwaApplicationType appType) {

    switch (appType) {
      case INITIAL:
        return ReverseRouter.route(on(InitialTaskListController.class).viewTaskList(pwaApplicationId, null));
      case CAT_1_VARIATION:
        return ReverseRouter.route(on(Category1TaskListController.class).viewTaskList(pwaApplicationId, null));
      case CAT_2_VARIATION:
        return ReverseRouter.route(on(Category2TaskListController.class).viewTaskList(pwaApplicationId, null));
      case DECOMMISSIONING:
        return ReverseRouter.route(on(DecommissioningTaskListController.class).viewTaskList(pwaApplicationId, null));
      case DEPOSIT_CONSENT:
        return ReverseRouter.route(on(DepositConsentTaskListController.class).viewTaskList(pwaApplicationId, null));
      case HUOO_VARIATION:
        return ReverseRouter.route(on(HuooVariationTaskListController.class).viewTaskList(pwaApplicationId, null));
      case OPTIONS_VARIATION:
        return ReverseRouter.route(on(OptionsVariationTaskListController.class).viewTaskList(pwaApplicationId, null));
      default:
        return ReverseRouter.route(on(WorkAreaController.class).renderWorkArea(null, null, null));
    }
  }

}
