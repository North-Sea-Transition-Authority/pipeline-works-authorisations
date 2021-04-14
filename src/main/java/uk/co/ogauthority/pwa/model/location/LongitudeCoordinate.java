package uk.co.ogauthority.pwa.model.location;

import java.math.BigDecimal;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.util.CoordinateUtils;
import uk.co.ogauthority.pwa.util.StringDisplayUtils;

public class LongitudeCoordinate extends Coordinate {

  private LongitudeDirection direction;

  public LongitudeCoordinate(Integer degrees, Integer minutes, BigDecimal seconds, LongitudeDirection direction) {
    super(degrees, minutes, seconds);
    this.direction = direction;
  }

  public LongitudeDirection getDirection() {
    return direction;
  }

  public void setDirection(LongitudeDirection direction) {
    this.direction = direction;
  }

  @Override
  public String getDisplayString() {
    return ObjectUtils.allNotNull(this.direction, this.getDegrees(), this.getMinutes(), this.getSeconds())
        ? String.format(
        CoordinateUtils.FORMAT_STRING,
        StringDisplayUtils.formatInteger2DigitZeroPaddingOrNull(this.getDegrees()),
        StringDisplayUtils.formatInteger2DigitZeroPaddingOrNull(this.getMinutes()),
        CoordinateUtils.formatSeconds(this.getSeconds()),
        this.getDirection().getDisplayTextShort()
    ) : null;
  }

  public boolean hasValue() {
    return this.degMinSecPresent() && direction != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LongitudeCoordinate that = (LongitudeCoordinate) o;
    return direction == that.direction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(direction);
  }
}
