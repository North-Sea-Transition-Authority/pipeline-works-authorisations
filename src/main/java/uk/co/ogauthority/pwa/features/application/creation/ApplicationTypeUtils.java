package uk.co.ogauthority.pwa.features.application.creation;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;

public class ApplicationTypeUtils {

  private ApplicationTypeUtils() {
    throw new AssertionError();
  }

  public static String getFormattedDuration(PwaApplicationType pwaApplicationType) {
    String durationMeasurement = "";
    int minPeriod = 0;
    int maxPeriod = 0;
    // Period.ofMonths doesn't include days.
    if (pwaApplicationType.getMinProcessingPeriod().getDays() > 0) {
      if (pwaApplicationType.getMinProcessingPeriod().getDays() % 7 == 0
          && pwaApplicationType.getMaxProcessingPeriod().getDays() % 7 == 0) {
        durationMeasurement = "weeks";
        minPeriod = pwaApplicationType.getMinProcessingPeriod().getDays() / 7;
        maxPeriod = pwaApplicationType.getMaxProcessingPeriod().getDays() / 7;
      } else {
        durationMeasurement = "days";
        minPeriod = pwaApplicationType.getMinProcessingPeriod().getDays();
        maxPeriod = pwaApplicationType.getMaxProcessingPeriod().getDays();
      }
    } else if (pwaApplicationType.getMinProcessingPeriod().getMonths() > 0) {
      durationMeasurement = "months";
      minPeriod = pwaApplicationType.getMinProcessingPeriod().getMonths();
      maxPeriod = pwaApplicationType.getMaxProcessingPeriod().getMonths();
    }
    if (minPeriod == maxPeriod) {
      return String.format("%d %s", minPeriod, durationMeasurement);
    } else {
      return String.format("%d-%d %s", minPeriod, maxPeriod, durationMeasurement);
    }
  }

  public static String getFormattedMedianLineDuration(PwaApplicationType pwaApplicationType) {
    if (pwaApplicationType.getMedianLineImplication().equals(MedianLineImplication.FALSE)) {
      throw new ActionNotAllowedException(
          "Formatting a duration without an implication is not allowed. PwaApplicationType: " + pwaApplicationType.name()
      );
    }
    String durationMeasurement = "";
    int maxPeriod = 0;
    if (pwaApplicationType.getMinProcessingPeriod().getDays() > 0) {
      if (pwaApplicationType.getMinProcessingPeriod().getDays() % 7 == 0) {
        durationMeasurement = "weeks";
        maxPeriod = Math.max(
            pwaApplicationType.getMinProcessingPeriod().getDays(),
            pwaApplicationType.getMaxProcessingPeriod().getDays()
        ) / 7;
      } else {
        durationMeasurement = "days";
        maxPeriod = Math.max(
            pwaApplicationType.getMinProcessingPeriod().getDays(),
            pwaApplicationType.getMaxProcessingPeriod().getDays()
        );
      }
    } else if (pwaApplicationType.getMinProcessingPeriod().getMonths() > 0) {
      durationMeasurement = "months";
      maxPeriod = Math.max(
          pwaApplicationType.getMinProcessingPeriod().getMonths(),
          pwaApplicationType.getMaxProcessingPeriod().getMonths()
      );
    }
    return String.format("more than %d %s", maxPeriod, durationMeasurement);
  }

}
