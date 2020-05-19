package uk.co.ogauthority.pwa.model.location;

import java.math.BigDecimal;
import java.util.Objects;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;

public class LatitudeCoordinate extends Coordinate {

  private LatitudeDirection direction;

  public LatitudeCoordinate(Integer degrees, Integer minutes, BigDecimal seconds, LatitudeDirection direction) {
    super(degrees, minutes, seconds);
    this.direction = direction;
  }

  public LatitudeDirection getDirection() {
    return direction;
  }

  public void setDirection(LatitudeDirection direction) {
    this.direction = direction;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LatitudeCoordinate that = (LatitudeCoordinate) o;
    return direction == that.direction;
  }

  @Override
  public int hashCode() {
    return Objects.hash(direction);
  }
}
