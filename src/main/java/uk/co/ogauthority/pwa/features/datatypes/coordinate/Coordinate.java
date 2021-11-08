package uk.co.ogauthority.pwa.features.datatypes.coordinate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data class for handling co-ordinate data.
 */
public abstract class Coordinate {

  private static final Logger LOGGER = LoggerFactory.getLogger(Coordinate.class);

  private static final int CALCULATION_SCALE = 10; // maximise precision in calculations
  private static final int OUTPUT_SCALE = 4; // we only collect 2 dp on "seconds", use sensible scale to avoid overly precise output.

  private Integer degrees;

  private Integer minutes;

  private BigDecimal seconds;

  public Coordinate(Integer degrees, Integer minutes, BigDecimal seconds) {
    this.degrees = degrees;
    this.minutes = minutes;
    this.seconds = seconds;
  }

  public Integer getDegrees() {
    return degrees;
  }

  public void setDegrees(Integer degrees) {
    this.degrees = degrees;
  }

  public Integer getMinutes() {
    return minutes;
  }

  public void setMinutes(Integer minutes) {
    this.minutes = minutes;
  }

  public BigDecimal getSeconds() {
    return seconds;
  }

  public void setSeconds(BigDecimal seconds) {
    this.seconds = seconds;
  }

  public abstract String getDisplayString();

  public boolean degMinSecPresent() {
    return ObjectUtils.allNotNull(degrees, minutes, seconds);
  }

  public double convertToDecimalDegrees() {
    //  Decimal Degrees = degrees + (minutes/60) + (seconds/3600)
    // need to convert to deg/min/dec values to BigDecimal and set scale to be large enough
    // so that division operation produces accurate result after division without truncating the value to the original scale.
    LOGGER.debug("convertToDecimalDegrees() {}", this);

    var deg = BigDecimal.valueOf(this.degrees).setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);

    var min = BigDecimal.valueOf(this.minutes).setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);
    var minDiv = min.divide(BigDecimal.valueOf(60), RoundingMode.HALF_UP);

    var sec =  seconds.setScale(CALCULATION_SCALE, RoundingMode.HALF_UP);
    var secDiv =  sec.divide(BigDecimal.valueOf(3600), RoundingMode.HALF_UP);

    var fullScaleDecimalDegrees =  deg.add(minDiv).add(secDiv);
    LOGGER.debug("fullScaleDecimalDegrees {}", fullScaleDecimalDegrees);

    var outputDecimalDegrees = fullScaleDecimalDegrees.setScale(OUTPUT_SCALE, RoundingMode.HALF_UP);
    LOGGER.debug("outputDecimalDegrees {}", outputDecimalDegrees);

    return outputDecimalDegrees.doubleValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Coordinate that = (Coordinate) o;
    return Objects.equals(degrees, that.degrees)
        && Objects.equals(minutes, that.minutes)
        && Objects.equals(seconds, that.seconds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(degrees, minutes, seconds);
  }

  @Override
  public String toString() {
    return "Coordinate{" +
        "degrees=" + degrees +
        ", minutes=" + minutes +
        ", seconds=" + seconds +
        '}';
  }
}
