package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineMappingServiceTest {

  private PipelineMappingService pipelineMappingService;

  @Before
  public void setUp() throws Exception {

    pipelineMappingService = new PipelineMappingService();

  }

  @Test
  public void mapPadPipelineToPipelineDetail() throws IllegalAccessException {

    var appDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var padPipelineIdentData = PadPipelineTestUtil.createPadPipeline(appDetail, PipelineType.METHANOL_PIPELINE);
    var padPipeline = padPipelineIdentData.getPadPipelineIdent().getPadPipeline();

    var detail = new PipelineDetail();

    pipelineMappingService.mapPadPipelineToPipelineDetail(detail, padPipeline);

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(detail,
        List.of("id", "pipeline", "startTimestamp", "endTimestamp", "tipFlag", "pwaConsent"));

    assertThat(detail.getPipelineType()).isEqualTo(padPipeline.getPipelineType());
    assertThat(detail.getFromLocation()).isEqualTo(padPipeline.getFromLocation());
    assertThat(detail.getFromLatitudeDegrees()).isEqualTo(padPipeline.getFromLatDeg());
    assertThat(detail.getFromLatitudeMinutes()).isEqualTo(padPipeline.getFromLatMin());
    assertThat(detail.getFromLatitudeSeconds()).isEqualTo(padPipeline.getFromLatSec());
    assertThat(detail.getFromLatitudeDirection()).isEqualTo(padPipeline.getFromLatDir());
    assertThat(detail.getFromLongitudeDegrees()).isEqualTo(padPipeline.getFromLongDeg());
    assertThat(detail.getFromLongitudeMinutes()).isEqualTo(padPipeline.getFromLongMin());
    assertThat(detail.getFromLongitudeSeconds()).isEqualTo(padPipeline.getFromLongSec());
    assertThat(detail.getFromLongitudeDirection()).isEqualTo(padPipeline.getFromLongDir());
    assertThat(detail.getToLocation()).isEqualTo(padPipeline.getToLocation());
    assertThat(detail.getToLatitudeDegrees()).isEqualTo(padPipeline.getToLatDeg());
    assertThat(detail.getToLatitudeMinutes()).isEqualTo(padPipeline.getToLatMin());
    assertThat(detail.getToLatitudeSeconds()).isEqualTo(padPipeline.getToLatSec());
    assertThat(detail.getToLatitudeDirection()).isEqualTo(padPipeline.getToLatDir());
    assertThat(detail.getToLongitudeDegrees()).isEqualTo(padPipeline.getToLongDeg());
    assertThat(detail.getToLongitudeMinutes()).isEqualTo(padPipeline.getToLongMin());
    assertThat(detail.getToLongitudeSeconds()).isEqualTo(padPipeline.getToLongSec());
    assertThat(detail.getToLongitudeDirection()).isEqualTo(padPipeline.getToLongDir());
    assertThat(detail.getComponentPartsDesc()).isEqualTo(padPipeline.getComponentPartsDescription());
    assertThat(detail.getLength()).isEqualTo(padPipeline.getLength());
    assertThat(detail.getProductsToBeConveyed()).isEqualTo(padPipeline.getProductsToBeConveyed());
    assertThat(detail.getTrenchedBuriedFilledFlag()).isEqualTo(padPipeline.getTrenchedBuriedBackfilled());
    assertThat(detail.getTrenchingMethodsDesc()).isEqualTo(padPipeline.getTrenchingMethodsDescription());
    assertThat(detail.getPipelineFlexibility()).isEqualTo(padPipeline.getPipelineFlexibility());
    assertThat(detail.getPipelineMaterial()).isEqualTo(padPipeline.getPipelineMaterial());
    assertThat(detail.getOtherPipelineMaterialUsed()).isEqualTo(padPipeline.getOtherPipelineMaterialUsed());
    assertThat(detail.getPipelineDesignLife()).isEqualTo(padPipeline.getPipelineDesignLife());
    assertThat(detail.getFromCoordinates()).isEqualTo(padPipeline.getFromCoordinates());
    assertThat(detail.getToCoordinates()).isEqualTo(padPipeline.getToCoordinates());

  }

}