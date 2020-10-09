package uk.co.ogauthority.pwa.model.location;

import java.math.BigDecimal;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

public interface CoordinatePairEntity {

  Integer getFromLatDeg();

  Integer getFromLatMin();

  BigDecimal getFromLatSec();

  LatitudeDirection getFromLatDir();

  Integer getFromLongDeg();

  Integer getFromLongMin();

  BigDecimal getFromLongSec();

  LongitudeDirection getFromLongDir();

  Integer getToLatDeg();

  Integer getToLatMin();

  BigDecimal getToLatSec();

  LatitudeDirection getToLatDir();

  Integer getToLongDeg();

  Integer getToLongMin();

  BigDecimal getToLongSec();

  LongitudeDirection getToLongDir();

}
