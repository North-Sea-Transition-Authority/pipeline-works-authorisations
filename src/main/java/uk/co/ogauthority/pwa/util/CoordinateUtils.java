package uk.co.ogauthority.pwa.util;

import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;

/**
 * Utility class for common interactions with coordinates and associated objects.
 */
public class CoordinateUtils {

  private CoordinateUtils() {
    throw new AssertionError();
  }

  public static CoordinatePair coordinatePairFromForm(CoordinateForm form) {
    return new CoordinatePair(
        new LatitudeCoordinate(
            form.getLatitudeDegrees(), form.getLatitudeMinutes(), form.getLatitudeSeconds(), form.getLatitudeDirection()),
        new LongitudeCoordinate(
            form.getLongitudeDegrees(), form.getLongitudeMinutes(), form.getLongitudeSeconds(), form.getLongitudeDirection())
    );
  }

  public static void mapCoordinatePairToForm(CoordinatePair pair, CoordinateForm form) {
    form.setLatitudeDegrees(pair.getLatitude().getDegrees());
    form.setLatitudeMinutes(pair.getLatitude().getMinutes());
    form.setLatitudeSeconds(pair.getLatitude().getSeconds());
    form.setLatitudeDirection(pair.getLatitude().getDirection());
    form.setLongitudeDegrees(pair.getLongitude().getDegrees());
    form.setLongitudeMinutes(pair.getLongitude().getMinutes());
    form.setLongitudeSeconds(pair.getLongitude().getSeconds());
    form.setLongitudeDirection(pair.getLongitude().getDirection());
  }

}
