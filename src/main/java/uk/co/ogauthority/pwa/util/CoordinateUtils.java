package uk.co.ogauthority.pwa.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Optional;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.CoordinatePairEntity;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;

/**
 * Utility class for common interactions with coordinates and associated objects.
 */
public class CoordinateUtils {

  public static final String FORMAT_STRING =  "%s ° %s ' %s \" %s";
  public static final DecimalFormat DECIMAL_SECONDS_FORMAT = new DecimalFormat("00.00");
  public static final int DECIMAL_SECONDS_DP = 2;

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
   * @return if the value's scale is less than required, set the scale to the required format, otherwise the unchanged value
   */
  static BigDecimal restoreScale(BigDecimal seconds) {

    return Optional.ofNullable(seconds)
        .map(value -> {

          if (value.scale() < DECIMAL_SECONDS_DP) {
            return value.setScale(DECIMAL_SECONDS_DP, RoundingMode.UNNECESSARY);
          }

          return value;

        })
        .orElse(null);

  }

  /**
   * Used to build coordinate objects for easier interaction with individual coordinate fields
   * when dealing with coordinate entities. Also handles scaling retrieved data accordingly as trailing
   * zeroes are removed on retrieval of individual field values.
   * @param coordinatePairEntity building coordinate object for
   * @return coordinate pair containing the individual coordinate values defined on the object
   */
  public static CoordinatePair buildFromCoordinatePair(CoordinatePairEntity coordinatePairEntity) {

    return new CoordinatePair(
        new LatitudeCoordinate(
            coordinatePairEntity.getFromLatDeg(),
            coordinatePairEntity.getFromLatMin(),
            restoreScale(coordinatePairEntity.getFromLatSec()),
            coordinatePairEntity.getFromLatDir()),
        new LongitudeCoordinate(
            coordinatePairEntity.getFromLongDeg(),
            coordinatePairEntity.getFromLongMin(),
            restoreScale(coordinatePairEntity.getFromLongSec()),
            coordinatePairEntity.getFromLongDir())
    );

  }

  /**
   * Used to build coordinate objects for easier interaction with individual coordinate fields
   * when dealing with coordinate entities. Also handles scaling retrieved data accordingly as trailing
   * zeroes are removed on retrieval of individual field values.
   * @param coordinatePairEntity building coordinate object for
   * @return coordinate pair containing the individual coordinate values defined on the object
   */
  public static CoordinatePair buildToCoordinatePair(CoordinatePairEntity coordinatePairEntity) {

    return new CoordinatePair(
        new LatitudeCoordinate(
            coordinatePairEntity.getToLatDeg(),
            coordinatePairEntity.getToLatMin(),
            restoreScale(coordinatePairEntity.getToLatSec()),
            coordinatePairEntity.getToLatDir()),
        new LongitudeCoordinate(coordinatePairEntity.getToLongDeg(),
            coordinatePairEntity.getToLongMin(),
            restoreScale(coordinatePairEntity.getToLongSec()),
            coordinatePairEntity.getToLongDir())
    );

  }

  public static String formatSeconds(BigDecimal seconds) {
    return seconds != null ? CoordinateUtils.DECIMAL_SECONDS_FORMAT.format(seconds) : null;
  }

}
