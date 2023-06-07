package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum HasMoreBlocks {
  YES_NOW("Yes, I want to add one now", 10),
  YES_LATER("Yes, but I will add it later before I submit my application", 20),
  NO("No, I have added all the associated blocks I need to", 30);

  private final String displayName;
  private final Integer displayOrder;

  HasMoreBlocks(String displayName, Integer displayOrder) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
  }

  public static Map<String, String> getRadioItems() {
    return Arrays.stream(HasMoreBlocks.values())
        .sorted(Comparator.comparingInt(HasMoreBlocks::getDisplayOrder))
        .collect(Collectors.toMap(
            HasMoreBlocks::getEnumName,
            HasMoreBlocks::getDisplayName,
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
