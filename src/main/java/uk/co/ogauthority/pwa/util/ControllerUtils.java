package uk.co.ogauthority.pwa.util;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.controller.footer.AccessibilityStatementController;
import uk.co.ogauthority.pwa.controller.footer.ContactInformationController;
import uk.co.ogauthority.pwa.model.Checkable;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

/**
 * Utility class to provide useful methods for controllers.
 */
public class ControllerUtils {

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

}
