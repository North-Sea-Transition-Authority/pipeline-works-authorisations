package uk.co.ogauthority.pwa.temp.controller;

public enum PrototypeApplicationType {
  INITIAL("New PWA"),

  DEPOSIT_CONSENT("Deposit Consent"),

  CAT_1_VARIATION("Cat. 1 Variation"),

  CAT_2_VARIATION("Cat. 2 Variation"),

  HUOO_VARIATION("HUOO Variation"),

  OPTIONS_VARIATION("Options Variation"),

  DECOMMISSIONING("Decommissioning");

  private String displayName;

  PrototypeApplicationType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

}
