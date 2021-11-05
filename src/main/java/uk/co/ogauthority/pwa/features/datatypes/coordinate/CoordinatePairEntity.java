package uk.co.ogauthority.pwa.features.datatypes.coordinate;

import java.math.BigDecimal;

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
