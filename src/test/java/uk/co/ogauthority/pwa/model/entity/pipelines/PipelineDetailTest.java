package uk.co.ogauthority.pwa.model.entity.pipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.util.FieldUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePair;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LatitudeDirection;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeCoordinate;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.LongitudeDirection;

@RunWith(SpringJUnit4ClassRunner.class)
public class PipelineDetailTest {

  @Test
  public void updateCoordinateValues() throws IllegalAccessException {

    var pipelineDetail = new PipelineDetail();

    pipelineDetail.setFromCoordinates(new CoordinatePair(
        new LatitudeCoordinate(50, 30, BigDecimal.valueOf(20.02), LatitudeDirection.NORTH),
        new LongitudeCoordinate(20, 20, BigDecimal.valueOf(1), LongitudeDirection.EAST)
    ));

    pipelineDetail.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(47, 47, BigDecimal.valueOf(20), LatitudeDirection.SOUTH),
        new LongitudeCoordinate(12, 15, BigDecimal.valueOf(45.5), LongitudeDirection.WEST)
    ));

    assertThat(FieldUtils.getFieldValue(pipelineDetail, "fromLatitudeDegrees")).isEqualTo(50);
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "fromLatitudeMinutes")).isEqualTo(30);
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "fromLatitudeSeconds")).isEqualTo(BigDecimal.valueOf(20.02));
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "fromLatitudeDirection")).isEqualTo(LatitudeDirection.NORTH);

    assertThat(FieldUtils.getFieldValue(pipelineDetail, "fromLongitudeDegrees")).isEqualTo(20);
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "fromLongitudeMinutes")).isEqualTo(20);
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "fromLongitudeSeconds")).isEqualTo(BigDecimal.valueOf(1));
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "fromLongitudeDirection")).isEqualTo(LongitudeDirection.EAST);

    assertThat(FieldUtils.getFieldValue(pipelineDetail, "toLatitudeDegrees")).isEqualTo(47);
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "toLatitudeMinutes")).isEqualTo(47);
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "toLatitudeSeconds")).isEqualTo(BigDecimal.valueOf(20));
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "toLatitudeDirection")).isEqualTo(LatitudeDirection.SOUTH);

    assertThat(FieldUtils.getFieldValue(pipelineDetail, "toLongitudeDegrees")).isEqualTo(12);
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "toLongitudeMinutes")).isEqualTo(15);
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "toLongitudeSeconds")).isEqualTo(BigDecimal.valueOf(45.5));
    assertThat(FieldUtils.getFieldValue(pipelineDetail, "toLongitudeDirection")).isEqualTo(LongitudeDirection.WEST);

  }

  @Test
  public void postLoad() throws IllegalAccessException {

    var pipeline = new PipelineDetail();

    FieldUtils.setProtectedFieldValue("fromLatitudeDegrees", pipeline, 49);
    FieldUtils.setProtectedFieldValue("fromLatitudeMinutes", pipeline, 4);
    FieldUtils.setProtectedFieldValue("fromLatitudeSeconds", pipeline, new BigDecimal("2.00"));
    FieldUtils.setProtectedFieldValue("fromLatitudeDirection", pipeline, LatitudeDirection.NORTH);
    FieldUtils.setProtectedFieldValue("fromLongitudeDegrees", pipeline, 7);
    FieldUtils.setProtectedFieldValue("fromLongitudeMinutes", pipeline, 6);
    FieldUtils.setProtectedFieldValue("fromLongitudeSeconds", pipeline, new BigDecimal("5.50"));
    FieldUtils.setProtectedFieldValue("fromLongitudeDirection", pipeline, LongitudeDirection.EAST);

    FieldUtils.setProtectedFieldValue("toLatitudeDegrees", pipeline, 44);
    FieldUtils.setProtectedFieldValue("toLatitudeMinutes", pipeline, 8);
    FieldUtils.setProtectedFieldValue("toLatitudeSeconds", pipeline, new BigDecimal("9.00"));
    FieldUtils.setProtectedFieldValue("toLatitudeDirection", pipeline, LatitudeDirection.SOUTH);
    FieldUtils.setProtectedFieldValue("toLongitudeDegrees", pipeline, 19);
    FieldUtils.setProtectedFieldValue("toLongitudeMinutes", pipeline, 3);
    FieldUtils.setProtectedFieldValue("toLongitudeSeconds", pipeline, BigDecimal.valueOf(18.23));
    FieldUtils.setProtectedFieldValue("toLongitudeDirection", pipeline, LongitudeDirection.WEST);

    pipeline.postLoad();

    assertThat(pipeline.getFromCoordinates().getLatitude().getDegrees()).isEqualTo(FieldUtils.getFieldValue(pipeline, "fromLatitudeDegrees"));
    assertThat(pipeline.getFromCoordinates().getLatitude().getMinutes()).isEqualTo(FieldUtils.getFieldValue(pipeline, "fromLatitudeMinutes"));
    assertThat(pipeline.getFromCoordinates().getLatitude().getSeconds()).isEqualTo(FieldUtils.getFieldValue(pipeline, "fromLatitudeSeconds"));
    assertThat(pipeline.getFromCoordinates().getLatitude().getDirection()).isEqualTo(FieldUtils.getFieldValue(pipeline, "fromLatitudeDirection"));

    assertThat(pipeline.getFromCoordinates().getLongitude().getDegrees()).isEqualTo(FieldUtils.getFieldValue(pipeline, "fromLongitudeDegrees"));
    assertThat(pipeline.getFromCoordinates().getLongitude().getMinutes()).isEqualTo(FieldUtils.getFieldValue(pipeline, "fromLongitudeMinutes"));
    assertThat(pipeline.getFromCoordinates().getLongitude().getSeconds()).isEqualTo(FieldUtils.getFieldValue(pipeline, "fromLongitudeSeconds"));
    assertThat(pipeline.getFromCoordinates().getLongitude().getDirection()).isEqualTo(FieldUtils.getFieldValue(pipeline, "fromLongitudeDirection"));

    assertThat(pipeline.getToCoordinates().getLatitude().getDegrees()).isEqualTo(FieldUtils.getFieldValue(pipeline, "toLatitudeDegrees"));
    assertThat(pipeline.getToCoordinates().getLatitude().getMinutes()).isEqualTo(FieldUtils.getFieldValue(pipeline, "toLatitudeMinutes"));
    assertThat(pipeline.getToCoordinates().getLatitude().getSeconds()).isEqualTo(FieldUtils.getFieldValue(pipeline, "toLatitudeSeconds"));
    assertThat(pipeline.getToCoordinates().getLatitude().getDirection()).isEqualTo(FieldUtils.getFieldValue(pipeline, "toLatitudeDirection"));

    assertThat(pipeline.getToCoordinates().getLongitude().getDegrees()).isEqualTo(FieldUtils.getFieldValue(pipeline, "toLongitudeDegrees"));
    assertThat(pipeline.getToCoordinates().getLongitude().getMinutes()).isEqualTo(FieldUtils.getFieldValue(pipeline, "toLongitudeMinutes"));
    assertThat(pipeline.getToCoordinates().getLongitude().getSeconds()).isEqualTo(FieldUtils.getFieldValue(pipeline, "toLongitudeSeconds"));
    assertThat(pipeline.getToCoordinates().getLongitude().getDirection()).isEqualTo(FieldUtils.getFieldValue(pipeline, "toLongitudeDirection"));

  }

  @Test(expected = NullPointerException.class)
  public void getPipelineDetailId_whenIdIsNull() {
    var pd = new PipelineDetail();
    pd.getPipelineDetailId();
  }

  public void getPipelineDetailId_whenIdIsNotNull() {
    var pd = new PipelineDetail();
    pd.setId(1);
    assertThat(pd.getPipelineDetailId().asInt()).isEqualTo(1);
  }
}
