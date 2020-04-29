package uk.co.ogauthority.pwa.model.location;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

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
}
