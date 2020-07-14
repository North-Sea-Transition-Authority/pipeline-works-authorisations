package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentDataRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;

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
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    padPipeline.setPipelineInBundle(false);

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline);
    assertThat(padPipeline.getMaxExternalDiameter()).isNull();
  }

  @Test
  public void createPipelineName_singleDiameter() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setMaxExternalDiameter(BigDecimal.valueOf(5));
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline);
    var expectedPipelineName = "my ref - 5 Millimetre " + PipelineType.PRODUCTION_FLOWLINE.getDisplayName();
    assertThat(padPipeline.getPipelineName().equals(expectedPipelineName));
  }

  @Test
  public void createPipelineName_multipleDiameters() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    padPipeline.setPipelineInBundle(false);

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline);
    var expectedPipelineName = "my ref - " + PipelineType.HYDRAULIC_JUMPER.getDisplayName();
    assertThat(padPipeline.getPipelineName().equals(expectedPipelineName));
  }

  @Test
  public void createPipelineName_singleDiameter_partOfBundle() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setMaxExternalDiameter(BigDecimal.valueOf(5));
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(true);
    padPipeline.setBundleName("my bundle");

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline);
    var expectedPipelineName = "my ref - 5 Millimetre " + PipelineType.PRODUCTION_FLOWLINE.getDisplayName() + " (my bundle)";
    assertThat(padPipeline.getPipelineName().equals(expectedPipelineName));
  }


}
