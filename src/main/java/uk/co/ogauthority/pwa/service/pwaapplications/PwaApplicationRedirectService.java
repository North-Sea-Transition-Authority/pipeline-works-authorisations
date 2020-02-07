package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.controller.pwaapplications.start.StartInitialPwaController;
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
      case DECOMMISSIONING:
      case DEPOSIT_CONSENT:
      case HUOO_VARIATION:
      case OPTIONS_VARIATION:
      default:
        return null;
    }

  }

  /**
   * Return a redirect to the right task list for the passed-in application type.
   */
  public ModelAndView getTaskListRedirect(PwaApplicationType applicationType) {

    switch (applicationType) {
      case INITIAL:
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
