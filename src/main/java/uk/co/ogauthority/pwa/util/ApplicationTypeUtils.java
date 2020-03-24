package uk.co.ogauthority.pwa.util;

import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class ApplicationTypeUtils {

  public ApplicationTypeUtils() {
    throw new AssertionError();
  }

  public static String getFormattedDuration(PwaApplicationType pwaApplicationType) {
    String durationMeasurement = "";
    int minPeriod = 0;
    int maxPeriod = 0;
    if (pwaApplicationType.getMinPeriod().getDays() > 0) {
      durationMeasurement = "weeks";
      minPeriod = pwaApplicationType.getMinPeriod().getDays() / 7;
      maxPeriod = pwaApplicationType.getMaxPeriod().getDays() / 7;
    } else if (pwaApplicationType.getMinPeriod().getMonths() > 0) {
      durationMeasurement = "months";
      minPeriod = pwaApplicationType.getMinPeriod().getMonths();
      maxPeriod = pwaApplicationType.getMaxPeriod().getMonths();
    }
    if (minPeriod >= maxPeriod) {
      return String.format("%d+ %s", minPeriod, durationMeasurement);
    } else {
      return String.format("%d-%d %s", minPeriod, maxPeriod, durationMeasurement);
    }
  }

  public static String getFormattedImplicationDuration(PwaApplicationType pwaApplicationType) {
    String durationMeasurement = "";
    int maxPeriod = 0;
    if (pwaApplicationType.getMinPeriod().getDays() > 0) {
      durationMeasurement = "weeks";
      maxPeriod = Math.max(
          pwaApplicationType.getMinPeriod().getDays(),
          pwaApplicationType.getMaxPeriod().getDays()
      ) / 7;
    } else if (pwaApplicationType.getMinPeriod().getMonths() > 0) {
      durationMeasurement = "months";
      maxPeriod = Math.max(
          pwaApplicationType.getMinPeriod().getMonths(),
          pwaApplicationType.getMaxPeriod().getMonths()
      );
    }
    return String.format("%d+ %s", maxPeriod, durationMeasurement);
  }

}
