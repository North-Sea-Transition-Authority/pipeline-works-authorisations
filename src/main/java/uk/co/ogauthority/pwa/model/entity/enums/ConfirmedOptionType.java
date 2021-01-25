package uk.co.ogauthority.pwa.model.entity.enums;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;


public enum ConfirmedOptionType {
  WORK_COMPLETE_AS_PER_OPTIONS("Work completed as per option", 10),
  WORK_DONE_BUT_NOT_PRESENTED_AS_OPTION("Work done but not covered by option", 20),
  NO_WORK_DONE("No work done", 20);

  private final String displayName;
  private final int displayOrder;

  ConfirmedOptionType(String displayName, int displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<ConfirmedOptionType> orderedStream() {
    return Arrays.stream(ConfirmedOptionType.values())
        .sorted(Comparator.comparing(ConfirmedOptionType::getDisplayOrder));
  }
}
