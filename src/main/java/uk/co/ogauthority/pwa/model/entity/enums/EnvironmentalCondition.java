package uk.co.ogauthority.pwa.model.entity.enums;

import java.util.Arrays;
import java.util.stream.Stream;

public enum EnvironmentalCondition {

  DISCHARGE_FUNDS_AVAILABLE(10,
      "I hereby confirm that the holder has funds available to discharge any liability for damage " +
          "attributable to the release or escape of anything from the pipeline."),
  OPOL_LIABILITY_STATEMENT(20,
      "I acknowledge liability insurance in respect of North Sea operations is arranged under the " +
          "General Third Party Liability Risk Insurance and the complementary arrangements effected under the " +
          "Offshore Pollution Liability Agreement (OPOL) of the holder.");

  private int displayOrder;
  private String conditionText;

  EnvironmentalCondition(int displayOrder, String conditionText) {
    this.displayOrder = displayOrder;
    this.conditionText = conditionText;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getConditionText() {
    return conditionText;
  }

  public static Stream<EnvironmentalCondition> stream() {
    return Arrays.stream(EnvironmentalCondition.values());
  }
}
