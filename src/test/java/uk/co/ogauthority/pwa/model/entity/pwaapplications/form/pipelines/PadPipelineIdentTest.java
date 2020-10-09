package uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import org.springframework.security.util.FieldUtils;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

public class PadPipelineIdentTest {

  @Test
  public void updateCoordinateValues() throws IllegalAccessException {

    var ident = new PadPipelineIdent();

    ident.setFromCoordinates(new CoordinatePair(
        new LatitudeCoordinate(50, 30, BigDecimal.valueOf(20.02), LatitudeDirection.NORTH),
        new LongitudeCoordinate(20, 20, new BigDecimal("1.00"), LongitudeDirection.EAST)
    ));

    ident.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(47, 47, new BigDecimal("20.00"), LatitudeDirection.SOUTH),
        new LongitudeCoordinate(12, 15, new BigDecimal("45.50"), LongitudeDirection.WEST)
    ));

    assertThat(FieldUtils.getFieldValue(ident, "fromLatitudeDegrees")).isEqualTo(50);
    assertThat(FieldUtils.getFieldValue(ident, "fromLatitudeMinutes")).isEqualTo(30);
    assertThat(FieldUtils.getFieldValue(ident, "fromLatitudeSeconds")).isEqualTo(BigDecimal.valueOf(20.02));
    assertThat(FieldUtils.getFieldValue(ident, "fromLatitudeDirection")).isEqualTo(LatitudeDirection.NORTH);

    assertThat(FieldUtils.getFieldValue(ident, "fromLongitudeDegrees")).isEqualTo(20);
    assertThat(FieldUtils.getFieldValue(ident, "fromLongitudeMinutes")).isEqualTo(20);
    assertThat(FieldUtils.getFieldValue(ident, "fromLongitudeSeconds")).isEqualTo(new BigDecimal("1.00"));
    assertThat(FieldUtils.getFieldValue(ident, "fromLongitudeDirection")).isEqualTo(LongitudeDirection.EAST);

    assertThat(FieldUtils.getFieldValue(ident, "toLatitudeDegrees")).isEqualTo(47);
    assertThat(FieldUtils.getFieldValue(ident, "toLatitudeMinutes")).isEqualTo(47);
    assertThat(FieldUtils.getFieldValue(ident, "toLatitudeSeconds")).isEqualTo(new BigDecimal("20.00"));
    assertThat(FieldUtils.getFieldValue(ident, "toLatitudeDirection")).isEqualTo(LatitudeDirection.SOUTH);

    assertThat(FieldUtils.getFieldValue(ident, "toLongitudeDegrees")).isEqualTo(12);
    assertThat(FieldUtils.getFieldValue(ident, "toLongitudeMinutes")).isEqualTo(15);
    assertThat(FieldUtils.getFieldValue(ident, "toLongitudeSeconds")).isEqualTo(new BigDecimal("45.50"));
    assertThat(FieldUtils.getFieldValue(ident, "toLongitudeDirection")).isEqualTo(LongitudeDirection.WEST);

  }

  @Test
  public void postLoad() throws IllegalAccessException {

    var ident = new PadPipelineIdent();

    FieldUtils.setProtectedFieldValue("fromLatitudeDegrees", ident, 49);
    FieldUtils.setProtectedFieldValue("fromLatitudeMinutes", ident, 4);
    FieldUtils.setProtectedFieldValue("fromLatitudeSeconds", ident, new BigDecimal("2.00"));
    FieldUtils.setProtectedFieldValue("fromLatitudeDirection", ident, LatitudeDirection.NORTH);
    FieldUtils.setProtectedFieldValue("fromLongitudeDegrees", ident, 7);
    FieldUtils.setProtectedFieldValue("fromLongitudeMinutes", ident, 6);
    FieldUtils.setProtectedFieldValue("fromLongitudeSeconds", ident, new BigDecimal("5.50"));
    FieldUtils.setProtectedFieldValue("fromLongitudeDirection", ident, LongitudeDirection.EAST);

    FieldUtils.setProtectedFieldValue("toLatitudeDegrees", ident, 44);
    FieldUtils.setProtectedFieldValue("toLatitudeMinutes", ident, 8);
    FieldUtils.setProtectedFieldValue("toLatitudeSeconds", ident, new BigDecimal("9.00"));
    FieldUtils.setProtectedFieldValue("toLatitudeDirection", ident, LatitudeDirection.SOUTH);
    FieldUtils.setProtectedFieldValue("toLongitudeDegrees", ident, 19);
    FieldUtils.setProtectedFieldValue("toLongitudeMinutes", ident, 3);
    FieldUtils.setProtectedFieldValue("toLongitudeSeconds", ident, BigDecimal.valueOf(18.23));
    FieldUtils.setProtectedFieldValue("toLongitudeDirection", ident, LongitudeDirection.WEST);

    ident.postLoad();

    assertThat(ident.getFromCoordinates().getLatitude().getDegrees()).isEqualTo(FieldUtils.getFieldValue(ident, "fromLatitudeDegrees"));
    assertThat(ident.getFromCoordinates().getLatitude().getMinutes()).isEqualTo(FieldUtils.getFieldValue(ident, "fromLatitudeMinutes"));
    assertThat(ident.getFromCoordinates().getLatitude().getSeconds()).isEqualTo(FieldUtils.getFieldValue(ident, "fromLatitudeSeconds"));
    assertThat(ident.getFromCoordinates().getLatitude().getDirection()).isEqualTo(FieldUtils.getFieldValue(ident, "fromLatitudeDirection"));

    assertThat(ident.getFromCoordinates().getLongitude().getDegrees()).isEqualTo(FieldUtils.getFieldValue(ident, "fromLongitudeDegrees"));
    assertThat(ident.getFromCoordinates().getLongitude().getMinutes()).isEqualTo(FieldUtils.getFieldValue(ident, "fromLongitudeMinutes"));
    assertThat(ident.getFromCoordinates().getLongitude().getSeconds()).isEqualTo(FieldUtils.getFieldValue(ident, "fromLongitudeSeconds"));
    assertThat(ident.getFromCoordinates().getLongitude().getDirection()).isEqualTo(FieldUtils.getFieldValue(ident, "fromLongitudeDirection"));

    assertThat(ident.getToCoordinates().getLatitude().getDegrees()).isEqualTo(FieldUtils.getFieldValue(ident, "toLatitudeDegrees"));
    assertThat(ident.getToCoordinates().getLatitude().getMinutes()).isEqualTo(FieldUtils.getFieldValue(ident, "toLatitudeMinutes"));
    assertThat(ident.getToCoordinates().getLatitude().getSeconds()).isEqualTo(FieldUtils.getFieldValue(ident, "toLatitudeSeconds"));
    assertThat(ident.getToCoordinates().getLatitude().getDirection()).isEqualTo(FieldUtils.getFieldValue(ident, "toLatitudeDirection"));

    assertThat(ident.getToCoordinates().getLongitude().getDegrees()).isEqualTo(FieldUtils.getFieldValue(ident, "toLongitudeDegrees"));
    assertThat(ident.getToCoordinates().getLongitude().getMinutes()).isEqualTo(FieldUtils.getFieldValue(ident, "toLongitudeMinutes"));
    assertThat(ident.getToCoordinates().getLongitude().getSeconds()).isEqualTo(FieldUtils.getFieldValue(ident, "toLongitudeSeconds"));
    assertThat(ident.getToCoordinates().getLongitude().getDirection()).isEqualTo(FieldUtils.getFieldValue(ident, "toLongitudeDirection"));

  }

}
