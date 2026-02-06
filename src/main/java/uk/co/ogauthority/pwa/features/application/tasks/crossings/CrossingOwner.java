package uk.co.ogauthority.pwa.features.application.tasks.crossings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.util.enumutils.Displayable;

public enum CrossingOwner implements Displayable {
  HOLDER("PWA holder(s) own 100% of the area"),
  PORTAL_ORGANISATION("The area is not owned by the PWA holder(s)"),
  UNLICENSED("The area is unlicensed");

  private final String displayName;

  CrossingOwner(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  public static Stream<CrossingOwner> stream() {
    return Arrays.stream(CrossingOwner.values());
  }

  public static List<CrossingOwner> asList() {
    return Arrays.asList(CrossingOwner.values());
  }
}
