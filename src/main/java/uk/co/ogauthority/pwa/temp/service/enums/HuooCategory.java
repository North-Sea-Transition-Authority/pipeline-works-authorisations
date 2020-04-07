package uk.co.ogauthority.pwa.temp.service.enums;

import java.util.stream.Stream;

public enum HuooCategory {

  HOLDER("Holders"),
  USER("Users"),
  OPERATOR("Operators"),
  OWNER("Owners");

  private String displayName;

  HuooCategory(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static Stream<HuooCategory> stream() {
    return Stream.of(HuooCategory.values());
  }

}
