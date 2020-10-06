package uk.co.ogauthority.pwa.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;

/**
 * Utility class for common interactions with coordinates and associated objects.
 */
public class CoordinateUtils {
  public static final String FORMAT_STRING =  "%s ° %s ' %s \" %s";

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
    if (pair != null) {
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

  /**
   * After a BigDecimal has been saved as "X.00", the scale disappears once it's retrieved and set on the form, becoming "X".
   * This method will restore the scale if it has been lost, adding the ".00" to the "X".
   * @param seconds value being updated
   * @return if the value's scale is 0, the same value with scale set to 2, otherwise the unchanged value
   */
  public static BigDecimal restoreScale(BigDecimal seconds) {
    return seconds.scale() == 0 ? seconds.setScale(2, RoundingMode.UNNECESSARY) : seconds;
  }

}
