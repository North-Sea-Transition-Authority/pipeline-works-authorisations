package uk.co.ogauthority.pwa.model.location;

import java.math.BigDecimal;
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
}
