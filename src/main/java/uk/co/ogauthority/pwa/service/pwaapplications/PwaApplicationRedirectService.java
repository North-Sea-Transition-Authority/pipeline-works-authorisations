package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.pwaapplications.start.StartInitialPwaController;
import uk.co.ogauthority.pwa.model.entity.pwa.PwaApplication;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.temp.controller.PwaApplicationController;

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
      case DECOMMISSIONING:
      case DEPOSIT_CONSENT:
      case HUOO_VARIATION:
      case OPTIONS_VARIATION:
      default:
        return null;
    }

  }

  /**
   * Return a redirect to the right task list for the passed-in application.
   */
  public ModelAndView getTaskListRedirect(PwaApplication pwaApplication) {

    switch (pwaApplication.getApplicationType()) {
      case INITIAL:
        // temporary task list
        return ReverseRouter.redirect(on(PwaApplicationController.class).viewTaskList(pwaApplication.getId()));
      case CAT_1_VARIATION:
      case CAT_2_VARIATION:
      case DECOMMISSIONING:
      case DEPOSIT_CONSENT:
      case HUOO_VARIATION:
      case OPTIONS_VARIATION:
      default:
        return ReverseRouter.redirect(on(WorkAreaController.class).renderWorkArea());
    }

  }

}
