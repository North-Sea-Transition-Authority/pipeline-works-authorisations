package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.time.Period;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Enumerates all types of application that can be submitted under the PWA process.
 */
public enum PwaApplicationType {

  INITIAL("New PWA", "initial",
      Period.ofMonths(4), Period.ofMonths(6)),

  DEPOSIT_CONSENT("Deposit Consent", "dep",
      Period.ofWeeks(6), Period.ofWeeks(8)),

  CAT_1_VARIATION(
      "Cat. 1 Variation", "cat-1",
      Period.ofMonths(4), Period.ofMonths(6)),

  CAT_2_VARIATION("Cat. 2 Variation", "cat-2",
      Period.ofWeeks(6), Period.ofWeeks(8)),

  HUOO_VARIATION("HUOO Variation", "huoo",
      Period.ofWeeks(6), Period.ofWeeks(8)),

  OPTIONS_VARIATION("Options Variation", "options",
      Period.ofWeeks(6), Period.ofWeeks(8)),

  DECOMMISSIONING("Decommissioning", "decom",
      Period.ofMonths(6), Period.ofMonths(6));

  private final String displayName;
  private final String urlPathString;

  private final Period minPeriod;
  private final Period maxPeriod;

  PwaApplicationType(String displayName, String urlPathString, Period minPeriod, Period maxPeriod) {
    this.displayName = displayName;
    this.urlPathString = urlPathString;
    this.minPeriod = minPeriod;
    this.maxPeriod = maxPeriod;
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

  public Period getMinPeriod() {
    return minPeriod;
  }

  public Period getMaxPeriod() {
    return maxPeriod;
  }

  public static Stream<PwaApplicationType> stream() {
    return Stream.of(PwaApplicationType.values());
  }
}
