package uk.co.ogauthority.pwa.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

@RunWith(SpringJUnit4ClassRunner.class)
public class CoordinateUtilsTest {

  @Test
  public void coordinatePairFromForm() {

    var form = new CoordinateForm();
    form.setLatitudeDegrees(55);
    form.setLatitudeMinutes(12);
    form.setLatitudeSeconds(BigDecimal.valueOf(3.3));
    form.setLatitudeDirection(LatitudeDirection.NORTH);

    form.setLongitudeDegrees(25);
    form.setLongitudeMinutes(30);
    form.setLongitudeSeconds(BigDecimal.valueOf(45.56));
    form.setLongitudeDirection(LongitudeDirection.WEST);

    var coordinatePair = CoordinateUtils.coordinatePairFromForm(form);

    assertThat(coordinatePair.getLatitude().getDegrees()).isEqualTo(form.getLatitudeDegrees());
    assertThat(coordinatePair.getLatitude().getMinutes()).isEqualTo(form.getLatitudeMinutes());
    assertThat(coordinatePair.getLatitude().getSeconds()).isEqualTo(form.getLatitudeSeconds());
    assertThat(coordinatePair.getLatitude().getDirection()).isEqualTo(form.getLatitudeDirection());

    assertThat(coordinatePair.getLongitude().getDegrees()).isEqualTo(form.getLongitudeDegrees());
    assertThat(coordinatePair.getLongitude().getMinutes()).isEqualTo(form.getLongitudeMinutes());
    assertThat(coordinatePair.getLongitude().getSeconds()).isEqualTo(form.getLongitudeSeconds());
    assertThat(coordinatePair.getLongitude().getDirection()).isEqualTo(form.getLongitudeDirection());

  }

  @Test
  public void mapCoordinatePairToForm() {

    var coordinatePair = new CoordinatePair(
        new LatitudeCoordinate(48, 49, BigDecimal.valueOf(50), LatitudeDirection.NORTH),
        new LongitudeCoordinate(5, 4, BigDecimal.valueOf(6.66), LongitudeDirection.EAST)
    );

    var form = new CoordinateForm();

    CoordinateUtils.mapCoordinatePairToForm(coordinatePair, form);

    assertThat(form.getLatitudeDegrees()).isEqualTo(coordinatePair.getLatitude().getDegrees());
    assertThat(form.getLatitudeMinutes()).isEqualTo(coordinatePair.getLatitude().getMinutes());
    assertThat(form.getLatitudeSeconds()).isEqualTo(coordinatePair.getLatitude().getSeconds());
    assertThat(form.getLatitudeDirection()).isEqualTo(coordinatePair.getLatitude().getDirection());

    assertThat(form.getLongitudeDegrees()).isEqualTo(coordinatePair.getLongitude().getDegrees());
    assertThat(form.getLongitudeMinutes()).isEqualTo(coordinatePair.getLongitude().getMinutes());
    assertThat(form.getLongitudeSeconds()).isEqualTo(coordinatePair.getLongitude().getSeconds());
    assertThat(form.getLongitudeDirection()).isEqualTo(coordinatePair.getLongitude().getDirection());

  }

}
