package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.pwaapplications.category1.Category1TaskListController;
import uk.co.ogauthority.pwa.controller.pwaapplications.category2.Category2TaskListController;
import uk.co.ogauthority.pwa.controller.pwaapplications.decommissioning.DecommissioningTaskListController;
import uk.co.ogauthority.pwa.controller.pwaapplications.deposit.DepositConsentTaskListController;
import uk.co.ogauthority.pwa.controller.pwaapplications.huoo.HuooVariationTaskListController;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.InitialTaskListController;
import uk.co.ogauthority.pwa.controller.pwaapplications.options.OptionsVariationTaskListController;
import uk.co.ogauthority.pwa.controller.pwaapplications.start.StartInitialPwaController;
import uk.co.ogauthority.pwa.controller.pwaapplications.start.StartPwaApplicationController;
import uk.co.ogauthority.pwa.controller.pwaapplications.start.StartVariationController;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Service
public class PwaApplicationRedirectService {

  /**
   * Return a redirect to the right start page for the passed-in application type.
   */
  public ModelAndView getStartApplicationRedirect(PwaApplicationType applicationType) {

    switch (applicationType) {
      case INITIAL:
        return ReverseRouter.redirect(on(StartInitialPwaController.class).renderStartPage());
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
