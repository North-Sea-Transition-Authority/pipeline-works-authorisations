package uk.co.ogauthority.pwa.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
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
        new LatitudeCoordinate(48, 49, new BigDecimal("50.00"), LatitudeDirection.NORTH),
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

  @Test
  public void restoreScale_null() {

    BigDecimal decimal = CoordinateUtils.restoreScale(null);

    assertThat(decimal).isNull();

  }

  @Test
  public void restoreScale_0dp() {

    BigDecimal decimal = CoordinateUtils.restoreScale(new BigDecimal("7"));

    assertThat(decimal).isEqualTo(new BigDecimal("7.00"));

  }

  @Test
  public void restoreScale_1dp() {

    BigDecimal decimal = CoordinateUtils.restoreScale(new BigDecimal("7.5"));

    assertThat(decimal).isEqualTo(new BigDecimal("7.50"));

  }

  @Test
  public void restoreScale_2dp() {

    BigDecimal decimal = CoordinateUtils.restoreScale(new BigDecimal("7.66"));

    assertThat(decimal).isEqualTo(new BigDecimal("7.66"));

  }

  @Test
  public void restoreScale_moreThan_2dp() {

    BigDecimal decimal = CoordinateUtils.restoreScale(new BigDecimal("7.666"));

    assertThat(decimal).isEqualTo(new BigDecimal("7.666"));

  }

  @Test
  public void buildFromCoordinatePair_null() {

    var pipeline = new PadPipeline();

    var coordinatePair = CoordinateUtils.buildFromCoordinatePair(pipeline);

    assertThat(coordinatePair.getLatitude().getDegrees()).isNull();
    assertThat(coordinatePair.getLatitude().getMinutes()).isNull();
    assertThat(coordinatePair.getLatitude().getSeconds()).isNull();
    assertThat(coordinatePair.getLatitude().getDirection()).isNull();

    assertThat(coordinatePair.getLongitude().getDegrees()).isNull();
    assertThat(coordinatePair.getLongitude().getMinutes()).isNull();
    assertThat(coordinatePair.getLongitude().getSeconds()).isNull();
    assertThat(coordinatePair.getLongitude().getDirection()).isNull();

  }

  @Test
  public void buildFromCoordinatePair_values() {

    var pipeline = new PadPipeline();
    var coordinate = CoordinatePairTestUtil.getDefaultCoordinate();
    pipeline.setFromCoordinates(coordinate);

    var coordinatePair = CoordinateUtils.buildFromCoordinatePair(pipeline);

    assertThat(coordinatePair.getLatitude().getDegrees()).isEqualTo(coordinate.getLatitude().getDegrees());
    assertThat(coordinatePair.getLatitude().getMinutes()).isEqualTo(coordinate.getLatitude().getMinutes());
    assertThat(coordinatePair.getLatitude().getSeconds()).isEqualTo(CoordinateUtils.restoreScale(coordinate.getLatitude().getSeconds()));
    assertThat(coordinatePair.getLatitude().getDirection()).isEqualTo(coordinate.getLatitude().getDirection());

    assertThat(coordinatePair.getLongitude().getDegrees()).isEqualTo(coordinate.getLongitude().getDegrees());
    assertThat(coordinatePair.getLongitude().getMinutes()).isEqualTo(coordinate.getLongitude().getMinutes());
    assertThat(coordinatePair.getLongitude().getSeconds()).isEqualTo(CoordinateUtils.restoreScale(coordinate.getLongitude().getSeconds()));
    assertThat(coordinatePair.getLongitude().getDirection()).isEqualTo(coordinate.getLongitude().getDirection());

  }

  @Test
  public void buildToCoordinatePair_null() {

    var pipeline = new PadPipeline();

    var coordinatePair = CoordinateUtils.buildToCoordinatePair(pipeline);

    assertThat(coordinatePair.getLatitude().getDegrees()).isNull();
    assertThat(coordinatePair.getLatitude().getMinutes()).isNull();
    assertThat(coordinatePair.getLatitude().getSeconds()).isNull();
    assertThat(coordinatePair.getLatitude().getDirection()).isNull();

    assertThat(coordinatePair.getLongitude().getDegrees()).isNull();
    assertThat(coordinatePair.getLongitude().getMinutes()).isNull();
    assertThat(coordinatePair.getLongitude().getSeconds()).isNull();
    assertThat(coordinatePair.getLongitude().getDirection()).isNull();

  }

  @Test
  public void buildToCoordinatePair_values() {

    var pipeline = new PadPipeline();
    var coordinate = CoordinatePairTestUtil.getDefaultCoordinate();
    pipeline.setToCoordinates(coordinate);

    var coordinatePair = CoordinateUtils.buildToCoordinatePair(pipeline);

    assertThat(coordinatePair.getLatitude().getDegrees()).isEqualTo(coordinate.getLatitude().getDegrees());
    assertThat(coordinatePair.getLatitude().getMinutes()).isEqualTo(coordinate.getLatitude().getMinutes());
    assertThat(coordinatePair.getLatitude().getSeconds()).isEqualTo(CoordinateUtils.restoreScale(coordinate.getLatitude().getSeconds()));
    assertThat(coordinatePair.getLatitude().getDirection()).isEqualTo(coordinate.getLatitude().getDirection());

    assertThat(coordinatePair.getLongitude().getDegrees()).isEqualTo(coordinate.getLongitude().getDegrees());
    assertThat(coordinatePair.getLongitude().getMinutes()).isEqualTo(coordinate.getLongitude().getMinutes());
    assertThat(coordinatePair.getLongitude().getSeconds()).isEqualTo(CoordinateUtils.restoreScale(coordinate.getLongitude().getSeconds()));
    assertThat(coordinatePair.getLongitude().getDirection()).isEqualTo(coordinate.getLongitude().getDirection());

  }

}
