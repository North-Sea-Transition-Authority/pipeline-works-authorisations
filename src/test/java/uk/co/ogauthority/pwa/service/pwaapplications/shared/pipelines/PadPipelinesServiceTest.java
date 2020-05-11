package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.util.FieldUtils;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelinesServiceTest {

  @Mock
  private PadPipelineRepository repository;

  private PadPipelinesService pipelinesService;

  @Captor
  private ArgumentCaptor<PadPipeline> pipelineCaptor;

  @Before
  public void setUp() {
    pipelinesService = new PadPipelinesService(repository);
  }

  @Test
  public void addPipeline() throws IllegalAccessException {

    var detail = new PwaApplicationDetail();
    var form = new PipelineHeaderForm();

    form.setFromLocation("from");
    var fromCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(55, 55, BigDecimal.valueOf(55.55), LatitudeDirection.NORTH),
            new LongitudeCoordinate(12, 12, BigDecimal.valueOf(12), LongitudeDirection.EAST)
        ), fromCoordinateForm
    );
    form.setFromCoordinateForm(fromCoordinateForm);

    form.setToLocation("to");
    var toCoordinateForm = new CoordinateForm();
    CoordinateUtils.mapCoordinatePairToForm(
        new CoordinatePair(
            new LatitudeCoordinate(46, 46, BigDecimal.valueOf(46), LatitudeDirection.SOUTH),
            new LongitudeCoordinate(6, 6, BigDecimal.valueOf(6.66), LongitudeDirection.WEST)
        ), toCoordinateForm
    );
    form.setToCoordinateForm(toCoordinateForm);

    form.setLength(BigDecimal.valueOf(65.66));
    form.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    form.setComponentPartsDescription("component parts");
    form.setProductsToBeConveyed("products");
    form.setTrenchedBuriedBackfilled(true);
    form.setTrenchingMethods("trench methods");

    pipelinesService.addPipeline(detail, form);

    verify(repository, times(1)).save(pipelineCaptor.capture());

    var newPipeline = pipelineCaptor.getValue();
    newPipeline.prePersistUpdate();

    assertThat(newPipeline.getFromLocation()).isEqualTo(form.getFromLocation());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLatitudeDegrees")).isEqualTo(form.getFromCoordinateForm().getLatitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLatitudeMinutes")).isEqualTo(form.getFromCoordinateForm().getLatitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLatitudeSeconds")).isEqualTo(form.getFromCoordinateForm().getLatitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLatitudeDirection")).isEqualTo(form.getFromCoordinateForm().getLatitudeDirection());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLongitudeDegrees")).isEqualTo(form.getFromCoordinateForm().getLongitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLongitudeMinutes")).isEqualTo(form.getFromCoordinateForm().getLongitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLongitudeSeconds")).isEqualTo(form.getFromCoordinateForm().getLongitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLongitudeDirection")).isEqualTo(form.getFromCoordinateForm().getLongitudeDirection());

    assertThat(newPipeline.getToLocation()).isEqualTo(form.getToLocation());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLatitudeDegrees")).isEqualTo(form.getToCoordinateForm().getLatitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLatitudeMinutes")).isEqualTo(form.getToCoordinateForm().getLatitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLatitudeSeconds")).isEqualTo(form.getToCoordinateForm().getLatitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLatitudeDirection")).isEqualTo(form.getToCoordinateForm().getLatitudeDirection());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLongitudeDegrees")).isEqualTo(form.getToCoordinateForm().getLongitudeDegrees());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLongitudeMinutes")).isEqualTo(form.getToCoordinateForm().getLongitudeMinutes());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLongitudeSeconds")).isEqualTo(form.getToCoordinateForm().getLongitudeSeconds());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLongitudeDirection")).isEqualTo(form.getToCoordinateForm().getLongitudeDirection());

    assertThat(newPipeline.getLength()).isEqualTo(form.getLength());
    assertThat(newPipeline.getPipelineType()).isEqualTo(form.getPipelineType());
    assertThat(newPipeline.getProductsToBeConveyed()).isEqualTo(form.getProductsToBeConveyed());
    assertThat(newPipeline.getComponentPartsDescription()).isEqualTo(form.getComponentPartsDescription());
    assertThat(form.getTrenchedBuriedBackfilled()).isEqualTo(form.getTrenchedBuriedBackfilled());
    assertThat(form.getTrenchingMethods()).isEqualTo(form.getTrenchingMethods());

  }

  @Test
  public void isComplete_noPipes() {

    var detail = new PwaApplicationDetail();
    when(repository.countAllByPwaApplicationDetail(detail)).thenReturn(0L);

    assertThat(pipelinesService.isComplete(detail)).isFalse();

  }

  @Test
  public void isComplete_notAllPipesHaveIdents() {

    var detail = new PwaApplicationDetail();
    when(repository.countAllByPwaApplicationDetail(detail)).thenReturn(1L);
    when(repository.countAllWithNoIdentsByPwaApplicationDetail(detail)).thenReturn(1L);

    assertThat(pipelinesService.isComplete(detail)).isFalse();

  }

  @Test
  public void isComplete_allPipesHaveIdents() {

    var detail = new PwaApplicationDetail();
    when(repository.countAllByPwaApplicationDetail(detail)).thenReturn(1L);
    when(repository.countAllWithNoIdentsByPwaApplicationDetail(detail)).thenReturn(0L);

    assertThat(pipelinesService.isComplete(detail)).isTrue();

  }

}
