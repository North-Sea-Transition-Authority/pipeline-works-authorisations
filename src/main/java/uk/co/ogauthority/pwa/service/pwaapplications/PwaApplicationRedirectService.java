package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.pwaapplications.category1.Category1TaskListController;
import uk.co.ogauthority.pwa.controller.pwaapplications.category2.Category2TaskListController;
import uk.co.ogauthority.pwa.controller.pwaapplications.initial.InitialTaskList;
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
      case CAT_1_VARIATION:
      case CAT_2_VARIATION:
        return ReverseRouter.redirect(on(StartVariationController.class).renderVariationTypeStartPage(applicationType));
      case DECOMMISSIONING:
      case DEPOSIT_CONSENT:
      case HUOO_VARIATION:
      case OPTIONS_VARIATION:
      default:
        return ReverseRouter.redirect(on(StartPwaApplicationController.class).renderStartApplication(null));
    }

  }

  /**
   * Return a redirect to the right task list for the passed-in application.
   */
  public ModelAndView getTaskListRedirect(PwaApplication pwaApplication, AuthenticatedUserAccount user) {

    switch (pwaApplication.getApplicationType()) {
      case INITIAL:
        return ReverseRouter.redirect(on(InitialTaskList.class).viewTaskList(pwaApplication.getId()));
      case CAT_1_VARIATION:
        return ReverseRouter.redirect(on(Category1TaskListController.class).viewTaskList(pwaApplication.getId()));
      case CAT_2_VARIATION:
        return ReverseRouter.redirect(on(Category2TaskListController.class).viewTaskList(pwaApplication.getId(), user));
      case DECOMMISSIONING:
      case DEPOSIT_CONSENT:
      case HUOO_VARIATION:
      case OPTIONS_VARIATION:
      default:
        return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea());
    }
  }

  /**
   * Return a route to the right task list for the passed-in application.
   */
  public String getTaskListRoute(PwaApplication pwaApplication, AuthenticatedUserAccount user) {

    switch (pwaApplication.getApplicationType()) {
      case INITIAL:
        return ReverseRouter.route(on(InitialTaskList.class).viewTaskList(pwaApplication.getId()));
      case CAT_1_VARIATION:
        return ReverseRouter.route(on(Category1TaskListController.class).viewTaskList(pwaApplication.getId()));
      case CAT_2_VARIATION:
        return ReverseRouter.route(on(Category2TaskListController.class).viewTaskList(pwaApplication.getId(), user));
      case DECOMMISSIONING:
      case DEPOSIT_CONSENT:
      case HUOO_VARIATION:
      case OPTIONS_VARIATION:
      default:
        return ReverseRouter.route(on(WorkAreaController.class).renderWorkArea());
    }
  }

}
