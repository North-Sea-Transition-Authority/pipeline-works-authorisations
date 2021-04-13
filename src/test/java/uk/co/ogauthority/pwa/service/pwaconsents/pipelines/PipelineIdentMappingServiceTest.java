package uk.co.ogauthority.pwa.service.pwaconsents.pipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineIdentMappingServiceTest {

  private PipelineIdentMappingService pipelineIdentMappingService;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    pipelineIdentMappingService = new PipelineIdentMappingService();

  }

  @Test
  public void mapPadPipelineIdentToPipelineIdent() throws IllegalAccessException {

    var padPipelineIdent = PadPipelineTestUtil
        .createPadPipeline(pwaApplicationDetail, PipelineType.GAS_LIFT_PIPELINE)
        .getPadPipelineIdent();

    var pipeDetail = new PipelineDetail();
    var pipeDetailIdent = new PipelineDetailIdent(pipeDetail);

    pipelineIdentMappingService.mapIdent(pipeDetailIdent, padPipelineIdent);

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(pipeDetailIdent, List.of("id"));

    assertThat(pipeDetailIdent.getIdentNo()).isEqualTo(padPipelineIdent.getIdentNo());
    assertThat(pipeDetailIdent.getFromLocation()).isEqualTo(padPipelineIdent.getFromLocation());
    assertThat(pipeDetailIdent.getFromLatDeg()).isEqualTo(padPipelineIdent.getFromLatDeg());
    assertThat(pipeDetailIdent.getFromLatMin()).isEqualTo(padPipelineIdent.getFromLatMin());
    assertThat(pipeDetailIdent.getFromLatSec()).isEqualTo(padPipelineIdent.getFromLatSec());
    assertThat(pipeDetailIdent.getFromLatDir()).isEqualTo(padPipelineIdent.getFromLatDir());
    assertThat(pipeDetailIdent.getFromLongDeg()).isEqualTo(padPipelineIdent.getFromLongDeg());
    assertThat(pipeDetailIdent.getFromLongMin()).isEqualTo(padPipelineIdent.getFromLongMin());
    assertThat(pipeDetailIdent.getFromLongSec()).isEqualTo(padPipelineIdent.getFromLongSec());
    assertThat(pipeDetailIdent.getFromLongDir()).isEqualTo(padPipelineIdent.getFromLongDir());
    assertThat(pipeDetailIdent.getFromLongDeg()).isEqualTo(padPipelineIdent.getFromLongDeg());
    assertThat(pipeDetailIdent.getToLocation()).isEqualTo(padPipelineIdent.getToLocation());
    assertThat(pipeDetailIdent.getToLatDeg()).isEqualTo(padPipelineIdent.getToLatDeg());
    assertThat(pipeDetailIdent.getToLatMin()).isEqualTo(padPipelineIdent.getToLatMin());
    assertThat(pipeDetailIdent.getToLatSec()).isEqualTo(padPipelineIdent.getToLatSec());
    assertThat(pipeDetailIdent.getToLatDir()).isEqualTo(padPipelineIdent.getToLatDir());
    assertThat(pipeDetailIdent.getToLongDeg()).isEqualTo(padPipelineIdent.getToLongDeg());
    assertThat(pipeDetailIdent.getToLongMin()).isEqualTo(padPipelineIdent.getToLongMin());
    assertThat(pipeDetailIdent.getToLongSec()).isEqualTo(padPipelineIdent.getToLongSec());
    assertThat(pipeDetailIdent.getToLongDir()).isEqualTo(padPipelineIdent.getToLongDir());
    assertThat(pipeDetailIdent.getLength()).isEqualTo(padPipelineIdent.getLength());
    assertThat(pipeDetailIdent.getDefiningStructure()).isEqualTo(padPipelineIdent.getIsDefiningStructure());
    assertThat(pipeDetailIdent.getFromCoordinates()).isEqualTo(padPipelineIdent.getFromCoordinates());
    assertThat(pipeDetailIdent.getToCoordinates()).isEqualTo(padPipelineIdent.getToCoordinates());

  }

}