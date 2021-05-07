package uk.co.ogauthority.pwa.model.location;

import java.math.BigDecimal;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Data class for handling co-ordinate data.
 */
public abstract class Coordinate {

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
}
