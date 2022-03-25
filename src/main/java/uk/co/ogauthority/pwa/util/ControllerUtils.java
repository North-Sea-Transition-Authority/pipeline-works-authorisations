package uk.co.ogauthority.pwa.util;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.controller.feedback.FeedbackController;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.webapp.footer.controller.AccessibilityStatementController;
import uk.co.ogauthority.pwa.features.webapp.footer.controller.ContactInformationController;
import uk.co.ogauthority.pwa.features.webapp.footer.controller.CookiesController;
import uk.co.ogauthority.pwa.model.Checkable;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

/**
 * Utility class to provide useful methods for controllers.
 */
public class ControllerUtils {

  private static final Set<PwaApplicationType> VALID_START_VARIATION_APP_TYPES = EnumSet.of(
      PwaApplicationType.CAT_1_VARIATION,
      PwaApplicationType.CAT_2_VARIATION,
      PwaApplicationType.HUOO_VARIATION,
      PwaApplicationType.DEPOSIT_CONSENT,
      PwaApplicationType.OPTIONS_VARIATION,
      PwaApplicationType.DECOMMISSIONING
  );

  private ControllerUtils() {
    throw new AssertionError();
  }

  public static Map<String, String> asCheckboxMap(List<? extends Checkable> items) {
    return items.stream()
        .collect(Collectors.toMap(Checkable::getIdentifier, Checkable::getDisplayName));
  }

  public static String getAccessibilityStatementUrl() {
    return ReverseRouter.route(on(AccessibilityStatementController.class).getAccessibilityStatement(null));
  }

  public static String getContactInformationUrl() {
    return ReverseRouter.route(on(ContactInformationController.class).getContactInformation(null));
  }

  public static String getCookiesUrl() {
    return ReverseRouter.route(on(CookiesController.class).getCookiePreferences());
  }

  public static void startVariationControllerCheckAppType(PwaApplicationType pwaApplicationType) {

    if (!VALID_START_VARIATION_APP_TYPES.contains(pwaApplicationType)) {
      throw new AccessDeniedException("Unsupported type for variation start controller: " + pwaApplicationType);
    }

  }

  public static String getFeedbackUrl(Integer pwaApplicationDetailId) {
    return ReverseRouter.route(on(FeedbackController.class).getFeedback(Optional.of(pwaApplicationDetailId), null, null));
  }

  public static String getFeedbackUrl() {
    return ReverseRouter.route(on(FeedbackController.class).getFeedback(Optional.empty(), null, null));
  }

}
