package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public enum CrossedBlockOwner {
  HOLDER("PWA holder(s) own 100% of block"),
  PORTAL_ORGANISATION("The block is not owned by the PWA holder(s)"),
  UNLICENSED("The block is unlicensed");

  private final String displayName;

  CrossedBlockOwner(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static Stream<CrossedBlockOwner> stream() {
    return Arrays.stream(CrossedBlockOwner.values());
  }

  public static List<CrossedBlockOwner> asList() {
    return Arrays.asList(CrossedBlockOwner.values());
  }
}
