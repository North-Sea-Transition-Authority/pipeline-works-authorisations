package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.util.Optional;

/**
 * Enumerates all types of application that can be submitted under the PWA process.
 */
public enum PwaApplicationType {

  INITIAL("New PWA", "initial"),

  DEPOSIT_CONSENT("Deposit Consent", "dep"),

  CAT_1_VARIATION("Cat. 1 Variation", "cat-1"),

  CAT_2_VARIATION("Cat. 2 Variation", "cat-2"),

  HUOO_VARIATION("HUOO Variation", "huoo"),

  OPTIONS_VARIATION("Options Variation", "options"),

  DECOMMISSIONING("Decommissioning", "decom");

  private final String displayName;
  private final String urlPathString;

  PwaApplicationType(String displayName, String urlPathString) {
    this.displayName = displayName;
    this.urlPathString = urlPathString;
  }

  public static Optional<PwaApplicationType> getFromUrlPathString(String urlPathValue) {
    for (PwaApplicationType type : PwaApplicationType.values()) {
      if (type.getUrlPathString().equals(urlPathValue)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUrlPathString() {
    return urlPathString;
  }
}
