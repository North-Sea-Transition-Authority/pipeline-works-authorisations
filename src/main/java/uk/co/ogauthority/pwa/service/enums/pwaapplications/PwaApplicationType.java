package uk.co.ogauthority.pwa.service.enums.pwaapplications;

/**
 * Enumerates all types of application that can be submitted under the PWA process.
 */
public enum PwaApplicationType {

  INITIAL("New PWA"),

  DEPOSIT_CONSENT("Deposit Consent"),

  CAT_1_VARIATION("Cat. 1 Variation"),

  CAT_2_VARIATION("Cat. 2 Variation"),

  HUOO_VARIATION("HUOO Variation"),

  OPTIONS_VARIATION("Options Variation"),

  DECOMMISSIONING("Decommissioning");

  private String displayName;

  PwaApplicationType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
