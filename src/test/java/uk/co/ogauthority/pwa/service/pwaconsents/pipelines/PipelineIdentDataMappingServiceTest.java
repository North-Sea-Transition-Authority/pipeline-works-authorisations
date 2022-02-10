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
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailIdentData;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineIdentDataMappingServiceTest {

  private PipelineIdentDataMappingService pipelineIdentDataMappingService;

  private PwaApplicationDetail appDetail;

  @Before
  public void setUp() throws Exception {

    appDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    pipelineIdentDataMappingService = new PipelineIdentDataMappingService();

  }

  @Test
  public void mapPadPipelineIdentDataToPipelineDetailIdentData() throws IllegalAccessException {

    var padIdentData = PadPipelineTestUtil.createPadPipeline(appDetail, PipelineType.METHANOL_PIPELINE);
    padIdentData.setExternalDiameterMultiCore("extdiamulticore");
    padIdentData.setInternalDiameterMultiCore("intdiamulticore");
    padIdentData.setWallThicknessMultiCore("wallthickmulticore");
    padIdentData.setInsulationCoatingTypeMultiCore("inscoatmulticore");
    padIdentData.setMaopMultiCore("maopmulticore");
    padIdentData.setProductsToBeConveyedMultiCore("prodconvmulticore");

    var pipelineIdentData = new PipelineDetailIdentData();

    pipelineIdentDataMappingService.mapPipelineIdentData(pipelineIdentData, padIdentData);

    ObjectTestUtils.assertAllExpectedFieldsHaveValue(pipelineIdentData, List.of("id", "pipelineDetailIdent"));

    assertThat(pipelineIdentData.getComponentPartsDesc()).isEqualTo(padIdentData.getComponentPartsDesc());
    assertThat(pipelineIdentData.getExternalDiameter()).isEqualTo(padIdentData.getExternalDiameter());
    assertThat(pipelineIdentData.getInternalDiameter()).isEqualTo(padIdentData.getInternalDiameter());
    assertThat(pipelineIdentData.getWallThickness()).isEqualTo(padIdentData.getWallThickness());
    assertThat(pipelineIdentData.getInsulationCoatingType()).isEqualTo(padIdentData.getInsulationCoatingType());
    assertThat(pipelineIdentData.getMaop()).isEqualTo(padIdentData.getMaop());
    assertThat(pipelineIdentData.getProductsToBeConveyed()).isEqualTo(padIdentData.getProductsToBeConveyed());
    assertThat(pipelineIdentData.getExternalDiameterMultiCore()).isEqualTo(padIdentData.getExternalDiameterMultiCore());
    assertThat(pipelineIdentData.getInternalDiameterMultiCore()).isEqualTo(padIdentData.getInternalDiameterMultiCore());
    assertThat(pipelineIdentData.getWallThicknessMultiCore()).isEqualTo(padIdentData.getWallThicknessMultiCore());
    assertThat(pipelineIdentData.getInsulationCoatingTypeMultiCore()).isEqualTo(padIdentData.getInsulationCoatingTypeMultiCore());
    assertThat(pipelineIdentData.getMaopMultiCore()).isEqualTo(padIdentData.getMaopMultiCore());
    assertThat(pipelineIdentData.getProductsToBeConveyedMultiCore()).isEqualTo(padIdentData.getProductsToBeConveyedMultiCore());

  }

}