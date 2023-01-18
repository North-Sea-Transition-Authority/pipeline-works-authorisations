package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdent;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentData;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentDataRepository;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentRepository;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelinePersisterServiceTest {

  @Mock
  private PadPipelineRepository padPipelineRepository;

  @Mock
  private PadPipelineIdentRepository padPipelineIdentRepository;

  @Mock
  private PadPipelineIdentDataRepository padPipelineIdentDataRepository;

  private PadPipelinePersisterService padPipelinePersisterService;

  @Before
  public void setUp() {
    padPipelinePersisterService = new PadPipelinePersisterService(padPipelineRepository, padPipelineIdentRepository, padPipelineIdentDataRepository);
  }


  @Test
  public void setMaxEternalDiameter_singleCore_multipleIdents() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);
    padPipeline.setId(1);

    List<PadPipelineIdent> identList = List.of();
    when(padPipelineIdentRepository.getAllByPadPipeline(padPipeline)).thenReturn(identList);

    var identData1 = new PadPipelineIdentData();
    identData1.setExternalDiameter(BigDecimal.valueOf(8));
    var identData2 = new PadPipelineIdentData();
    identData2.setExternalDiameter(BigDecimal.valueOf(5));
    when(padPipelineIdentDataRepository.getAllByPadPipelineIdentIn(identList)).thenReturn(List.of(identData1, identData2));

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline);
    assertThat(padPipeline.getMaxExternalDiameter()).isEqualTo(BigDecimal.valueOf(8));
  }

  @Test
  public void setMaxEternalDiameter_singleCore_zeroIdents() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline);
    assertThat(padPipeline.getMaxExternalDiameter()).isNull();
  }

  @Test
  public void setMaxEternalDiameter_multiCore() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER_MULTI_CORE);
    padPipeline.setPipelineInBundle(false);

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline);
    assertThat(padPipeline.getMaxExternalDiameter()).isNull();
  }


}
