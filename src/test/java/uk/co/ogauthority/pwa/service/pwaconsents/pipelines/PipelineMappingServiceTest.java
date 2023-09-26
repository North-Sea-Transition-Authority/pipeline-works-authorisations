package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineMappingServiceTest {

  private PipelineMappingService pipelineMappingService;
  private Pipeline sourcePipeline;

  @Before
  public void setUp() throws Exception {

    pipelineMappingService = new PipelineMappingService();
    sourcePipeline = new Pipeline();
    sourcePipeline.setId(100);

  }

  @Test
  public void mapPadPipelineToPipelineDetail() throws IllegalAccessException {

    var appDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var padPipelineIdentData = PadPipelineTestUtil.createPadPipeline(appDetail, PipelineType.METHANOL_PIPELINE);
    var padPipeline = padPipelineIdentData.getPadPipelineIdent().getPadPipeline();

    var sourcePipelineDetail = new PipelineDetail();
    sourcePipelineDetail.setPipeline(sourcePipeline);

    pipelineMappingService.mapPipelineEntities(sourcePipelineDetail, padPipeline);

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(sourcePipelineDetail,
        List.of("id", "startTimestamp", "endTimestamp", "tipFlag", "pwaConsent", "transferredFromPipeline", "transferredToPipeline"));

    // assert the mapper does not modify the pipeline of the entity where data being copied to.
    assertThat(padPipeline.getPipeline()).isNotEqualTo(sourcePipeline);

    assertThat(sourcePipelineDetail.getPipelineType()).isEqualTo(padPipeline.getPipelineType());
    assertThat(sourcePipelineDetail.getFromLocation()).isEqualTo(padPipeline.getFromLocation());
    assertThat(sourcePipelineDetail.getFromLatitudeDegrees()).isEqualTo(padPipeline.getFromLatDeg());
    assertThat(sourcePipelineDetail.getFromLatitudeMinutes()).isEqualTo(padPipeline.getFromLatMin());
    assertThat(sourcePipelineDetail.getFromLatitudeSeconds()).isEqualTo(padPipeline.getFromLatSec());
    assertThat(sourcePipelineDetail.getFromLatitudeDirection()).isEqualTo(padPipeline.getFromLatDir());
    assertThat(sourcePipelineDetail.getFromLongitudeDegrees()).isEqualTo(padPipeline.getFromLongDeg());
    assertThat(sourcePipelineDetail.getFromLongitudeMinutes()).isEqualTo(padPipeline.getFromLongMin());
    assertThat(sourcePipelineDetail.getFromLongitudeSeconds()).isEqualTo(padPipeline.getFromLongSec());
    assertThat(sourcePipelineDetail.getFromLongitudeDirection()).isEqualTo(padPipeline.getFromLongDir());
    assertThat(sourcePipelineDetail.getToLocation()).isEqualTo(padPipeline.getToLocation());
    assertThat(sourcePipelineDetail.getToLatitudeDegrees()).isEqualTo(padPipeline.getToLatDeg());
    assertThat(sourcePipelineDetail.getToLatitudeMinutes()).isEqualTo(padPipeline.getToLatMin());
    assertThat(sourcePipelineDetail.getToLatitudeSeconds()).isEqualTo(padPipeline.getToLatSec());
    assertThat(sourcePipelineDetail.getToLatitudeDirection()).isEqualTo(padPipeline.getToLatDir());
    assertThat(sourcePipelineDetail.getToLongitudeDegrees()).isEqualTo(padPipeline.getToLongDeg());
    assertThat(sourcePipelineDetail.getToLongitudeMinutes()).isEqualTo(padPipeline.getToLongMin());
    assertThat(sourcePipelineDetail.getToLongitudeSeconds()).isEqualTo(padPipeline.getToLongSec());
    assertThat(sourcePipelineDetail.getToLongitudeDirection()).isEqualTo(padPipeline.getToLongDir());
    assertThat(sourcePipelineDetail.getComponentPartsDescription()).isEqualTo(padPipeline.getComponentPartsDescription());
    assertThat(sourcePipelineDetail.getLength()).isEqualTo(padPipeline.getLength());
    assertThat(sourcePipelineDetail.getProductsToBeConveyed()).isEqualTo(padPipeline.getProductsToBeConveyed());
    assertThat(sourcePipelineDetail.getTrenchedBuriedBackfilled()).isEqualTo(padPipeline.getTrenchedBuriedBackfilled());
    assertThat(sourcePipelineDetail.getTrenchingMethodsDescription()).isEqualTo(padPipeline.getTrenchingMethodsDescription());
    assertThat(sourcePipelineDetail.getPipelineFlexibility()).isEqualTo(padPipeline.getPipelineFlexibility());
    assertThat(sourcePipelineDetail.getPipelineMaterial()).isEqualTo(padPipeline.getPipelineMaterial());
    assertThat(sourcePipelineDetail.getOtherPipelineMaterialUsed()).isEqualTo(padPipeline.getOtherPipelineMaterialUsed());
    assertThat(sourcePipelineDetail.getPipelineDesignLife()).isEqualTo(padPipeline.getPipelineDesignLife());
    assertThat(sourcePipelineDetail.getFromCoordinates()).isEqualTo(padPipeline.getFromCoordinates());
    assertThat(sourcePipelineDetail.getToCoordinates()).isEqualTo(padPipeline.getToCoordinates());
    assertThat(sourcePipelineDetail.getFootnote()).isEqualTo(padPipeline.getFootnote());

  }

  @Test(expected = NullPointerException.class)
  public void mapPadPipelineToPipelineDetail_noCoordinates() throws IllegalAccessException {

    var appDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var padPipelineIdentData = PadPipelineTestUtil.createPadPipeline(appDetail, PipelineType.METHANOL_PIPELINE);
    var padPipeline = padPipelineIdentData.getPadPipelineIdent().getPadPipeline();
    padPipeline.setFromCoordinates(null);
    padPipeline.setToCoordinates(null);

    var detail = new PipelineDetail();

    pipelineMappingService.mapPipelineEntities(detail, padPipeline);

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(detail,
        List.of("id", "pipeline", "startTimestamp", "endTimestamp", "tipFlag", "pwaConsent", "fromCoordinates", "toCoordinates", "transferredFrom", "transferredTo"));

  }

  @Test
  public void mapPadPipelineToPipelineDetail_pipelineTypeNull() throws IllegalAccessException {

    var appDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var padPipelineIdentData = PadPipelineTestUtil.createPadPipeline(appDetail, PipelineType.METHANOL_PIPELINE);
    var padPipeline = padPipelineIdentData.getPadPipelineIdent().getPadPipeline();
    padPipeline.setPipelineType(null);

    var detail = new PipelineDetail();

    pipelineMappingService.mapPipelineEntities(detail, padPipeline);

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(detail,
        List.of("id", "pipeline", "startTimestamp", "endTimestamp", "tipFlag", "pwaConsent", "transferredFromPipeline", "transferredToPipeline"));

    assertThat(detail.getPipelineType()).isEqualTo((PipelineType.UNKNOWN));

  }

}