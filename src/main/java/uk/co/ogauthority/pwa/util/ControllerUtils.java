package uk.co.ogauthority.pwa.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.Checkable;

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

}
