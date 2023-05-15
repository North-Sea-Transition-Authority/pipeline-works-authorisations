package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum AddBlockOptions {
  YES_NOW("Yes, I want to add one now", 10),
  YES_LATER("Yes, but I will add it later before I submit my application", 20),
  NO("No, I have added all the blocks I need to", 30);

  private final String displayName;
  private final Integer displayOrder;

  AddBlockOptions(String displayName, Integer displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  public static Map<String, String> getAddBlockOptions() {
    return Arrays.stream(AddBlockOptions.values())
        .sorted(Comparator.comparingInt(AddBlockOptions::getDisplayOrder))
        .collect(Collectors.toMap(
            AddBlockOptions::getEnumName,
            AddBlockOptions::getDisplayName,
            (x, y) -> y,
            LinkedHashMap::new
        ));
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getDisplayOrder() {
    return this.displayOrder;
  }

  public String getEnumName() {
    return this.name();
  }
}
