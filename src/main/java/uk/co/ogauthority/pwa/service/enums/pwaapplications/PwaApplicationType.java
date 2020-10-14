package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.time.Period;
import java.util.Optional;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;

/**
 * Enumerates all types of application that can be submitted under the PWA process.
 */
public enum PwaApplicationType {

  INITIAL("New PWA", "initial",
      Period.ofMonths(4), Period.ofMonths(6), MedianLineImplication.FALSE),

  DEPOSIT_CONSENT("Deposit Consent", "dep",
      Period.ofWeeks(6), Period.ofWeeks(8), MedianLineImplication.FALSE),

  CAT_1_VARIATION(
      "Cat. 1 Variation", "cat-1",
      Period.ofMonths(4), Period.ofMonths(6), MedianLineImplication.TRUE),

  CAT_2_VARIATION("Cat. 2 Variation", "cat-2",
      Period.ofWeeks(6), Period.ofWeeks(8), MedianLineImplication.TRUE),

  HUOO_VARIATION("HUOO Variation", "huoo",
      Period.ofWeeks(6), Period.ofWeeks(8), MedianLineImplication.FALSE),

  OPTIONS_VARIATION("Options Variation", "options",
      Period.ofWeeks(6), Period.ofWeeks(8), MedianLineImplication.FALSE),

  DECOMMISSIONING("Decommissioning", "decom",
      Period.ofMonths(6), Period.ofMonths(6), MedianLineImplication.TRUE);

  private final String displayName;
  private final String urlPathString;

  private final Period minProcessingPeriod;
  private final Period maxProcessingPeriod;
  private final MedianLineImplication medianLineImplication;

  PwaApplicationType(String displayName, String urlPathString, Period minProcessingPeriod, Period maxProcessingPeriod,
                     MedianLineImplication medianLineImplication) {
    this.displayName = displayName;
    this.urlPathString = urlPathString;
    this.minProcessingPeriod = minProcessingPeriod;
    this.maxProcessingPeriod = maxProcessingPeriod;
    this.medianLineImplication = medianLineImplication;
  }

  public static Optional<PwaApplicationType> getFromUrlPathString(String urlPathValue) {
    for (PwaApplicationType type : PwaApplicationType.values()) {
      if (type.getUrlPathString().equals(urlPathValue)) {
        return Optional.of(type);
      }
    }
    return Optional.empty();
  }

  public static PwaApplicationType resolveFromDisplayText(String applicationTypeDisplay) {
    return PwaApplicationType.stream()
        .filter(type -> type.getDisplayName().equals(applicationTypeDisplay))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(
            String.format("Couldn't find PwaApplicationType value for display string: %s", applicationTypeDisplay)));
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUrlPathString() {
    return urlPathString;
  }

  public Period getMinProcessingPeriod() {
    return minProcessingPeriod;
  }

  public Period getMaxProcessingPeriod() {
    return maxProcessingPeriod;
  }

  public MedianLineImplication getMedianLineImplication() {
    return medianLineImplication;
  }

  public static Stream<PwaApplicationType> stream() {
    return Stream.of(PwaApplicationType.values());
  }
}
