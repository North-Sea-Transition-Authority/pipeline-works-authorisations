package uk.co.ogauthority.pwa.model.location;


import java.math.BigDecimal;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

public class CoordinatePairTestUtil {


  public static CoordinatePair getDefaultCoordinate(){
    return new CoordinatePair(
        new LatitudeCoordinate(45, 46, BigDecimal.ZERO, LatitudeDirection.NORTH),
        new LongitudeCoordinate(0, 1, BigDecimal.TEN, LongitudeDirection.EAST)
    );

  }
}