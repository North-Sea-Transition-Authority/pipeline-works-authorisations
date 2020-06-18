package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public enum CrossedBlockOwner {
  HOLDER("PWA Holder(s) own 100% of block"),
  PORTAL_ORGANISATION("The block is not owned by the PWA Holder(s)"),
  UNLICENCED("The block is unlicenced");

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
