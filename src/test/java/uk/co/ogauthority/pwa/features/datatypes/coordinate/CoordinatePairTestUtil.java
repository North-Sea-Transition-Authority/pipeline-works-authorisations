package uk.co.ogauthority.pwa.features.datatypes.coordinate;


import java.math.BigDecimal;

public class CoordinatePairTestUtil {


  public static CoordinatePair getDefaultCoordinate(){
    return getDefaultCoordinate(46, 0);

  }

  public static CoordinatePair getDefaultCoordinate(int latDegrees, int longDegrees){
    return new CoordinatePair(
        new LatitudeCoordinate(latDegrees, 46, BigDecimal.ZERO, LatitudeDirection.NORTH),
        new LongitudeCoordinate(longDegrees, 1, BigDecimal.TEN, LongitudeDirection.EAST)
    );

  }

  public static CoordinatePair getNullCoordinate(){
    return new CoordinatePair(
        new LatitudeCoordinate(null, null, null, null),
        new LongitudeCoordinate(null, null, null, null)
    );

  }
}