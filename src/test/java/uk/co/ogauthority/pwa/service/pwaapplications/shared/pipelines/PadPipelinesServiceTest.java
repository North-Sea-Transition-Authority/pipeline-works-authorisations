package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineHeaderForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;

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
    form.setFromLatDeg(55);
    form.setFromLatMin(30);
    form.setFromLatSec(BigDecimal.valueOf(22.22));
    form.setFromLongDeg(13);
    form.setFromLongMin(22);
    form.setFromLongSec(BigDecimal.valueOf(12.1));
    form.setFromLongDirection(LongitudeDirection.EAST);
    form.setToLocation("to");
    form.setToLatDeg(54);
    form.setToLatMin(22);
    form.setToLatSec(BigDecimal.valueOf(25));
    form.setToLongDeg(22);
    form.setToLongMin(21);
    form.setToLongSec(BigDecimal.valueOf(1));
    form.setToLongDirection(LongitudeDirection.WEST);

    form.setLength(BigDecimal.valueOf(65.66));
    form.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    form.setComponentPartsDescription("component parts");
    form.setProductsToBeConveyed("products");
    form.setTrenchedBuriedBackfilled(true);
    form.setTrenchingMethods("trench methods");

    pipelinesService.addPipeline(detail, form);

    verify(repository, times(1)).save(pipelineCaptor.capture());

    var newPipeline = pipelineCaptor.getValue();
    newPipeline.prePersist();

    assertThat(newPipeline.getFromLocation()).isEqualTo(form.getFromLocation());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLatitudeDegrees")).isEqualTo(form.getFromLatDeg());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLatitudeMinutes")).isEqualTo(form.getFromLatMin());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLatitudeSeconds")).isEqualTo(form.getFromLatSec());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLatitudeDirection")).isEqualTo(form.getFromLatDirection());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLongitudeDegrees")).isEqualTo(form.getFromLongDeg());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLongitudeMinutes")).isEqualTo(form.getFromLongMin());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLongitudeSeconds")).isEqualTo(form.getFromLongSec());
    assertThat(FieldUtils.getFieldValue(newPipeline, "fromLongitudeDirection")).isEqualTo(form.getFromLongDirection());

    assertThat(FieldUtils.getFieldValue(newPipeline, "toLatitudeDegrees")).isEqualTo(form.getToLatDeg());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLatitudeMinutes")).isEqualTo(form.getToLatMin());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLatitudeSeconds")).isEqualTo(form.getToLatSec());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLatitudeDirection")).isEqualTo(form.getToLatDirection());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLongitudeDegrees")).isEqualTo(form.getToLongDeg());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLongitudeMinutes")).isEqualTo(form.getToLongMin());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLongitudeSeconds")).isEqualTo(form.getToLongSec());
    assertThat(FieldUtils.getFieldValue(newPipeline, "toLongitudeDirection")).isEqualTo(form.getToLongDirection());

    assertThat(newPipeline.getLength()).isEqualTo(form.getLength());
    assertThat(newPipeline.getPipelineType()).isEqualTo(form.getPipelineType());
    assertThat(newPipeline.getProductsToBeConveyed()).isEqualTo(form.getProductsToBeConveyed());
    assertThat(newPipeline.getComponentPartsDescription()).isEqualTo(form.getComponentPartsDescription());
    assertThat(form.getTrenchedBuriedBackfilled()).isEqualTo(form.getTrenchedBuriedBackfilled());
    assertThat(form.getTrenchingMethods()).isEqualTo(form.getTrenchingMethods());

  }

}
