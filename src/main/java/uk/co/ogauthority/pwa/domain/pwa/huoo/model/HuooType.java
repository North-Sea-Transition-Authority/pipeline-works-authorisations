package uk.co.ogauthority.pwa.domain.pwa.huoo.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum HuooType {
  // if these change check the huoo data migration script for hardcoded enum values
  PORTAL_ORG(10, "Legal entity", true),
  TREATY_AGREEMENT(20, "Treaty agreement", true),
  UNASSIGNED_PIPELINE_SPLIT(30, "Split pipeline role link", false);

  private final int displayOrder;
  private final String displayText;
  private final boolean selectable;

  HuooType(int displayOrder, String displayText, boolean selectable) {
    this.displayOrder = displayOrder;
    this.displayText = displayText;
    this.selectable = selectable;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getDisplayText() {
    return displayText;
  }

  public boolean isSelectable() {
    return selectable;
  }

  public static Set<HuooType> getSelectable() {
    return streamSelectable().collect(Collectors.toSet());
  }

  public static Stream<HuooType> streamSelectable() {
    return Arrays.stream(HuooType.values())
        .filter(HuooType::isSelectable);
  }
}
