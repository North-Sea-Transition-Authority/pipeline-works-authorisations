package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.util.FieldUtils;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdent;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineIdentData;
import uk.co.ogauthority.pwa.model.form.location.CoordinateForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentDataForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineIdentRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.util.CoordinateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelinePersisterServiceTest {

  @Mock
  private PadPipelineRepository padPipelineRepository;

  private PadPipelinePersisterService padPipelinePersisterService;

  @Before
  public void setUp() {
    padPipelinePersisterService = new PadPipelinePersisterService(padPipelineRepository);
  }


  @Test
  public void setMaxEternalDiameter_singleCore_multipleIdents() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);

    var ident1 = new PadPipelineIdent();
    var ident2 = new PadPipelineIdent();

    var identData1 = new PadPipelineIdentData();
    identData1.setExternalDiameter(BigDecimal.valueOf(8));
    identData1.setPadPipelineIdent(ident1);
    var identData2 = new PadPipelineIdentData();
    identData2.setExternalDiameter(BigDecimal.valueOf(5));
    identData2.setPadPipelineIdent(ident2);

    ident1.setPadPipeline(padPipeline);
    ident2.setPadPipeline(padPipeline);

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline, List.of(new IdentView(identData1), new IdentView(identData2)));
    assertThat(padPipeline.getMaxExternalDiameter()).isEqualTo(BigDecimal.valueOf(8));
  }

  @Test
  public void setMaxEternalDiameter_singleCore_zeroIdents() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline, List.of());
    assertThat(padPipeline.getMaxExternalDiameter()).isNull();
  }

  @Test
  public void setMaxEternalDiameter_multiCore() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    padPipeline.setPipelineInBundle(false);

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline, List.of());
    assertThat(padPipeline.getMaxExternalDiameter()).isNull();
  }

  @Test
  public void createPipelineName_singleDiameter() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setMaxExternalDiameter(BigDecimal.valueOf(5));
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline, List.of());
    var expectedPipelineName = "my ref - 5 Millimetre " + PipelineType.PRODUCTION_FLOWLINE.getDisplayName();
    assertThat(padPipeline.getPipelineName().equals(expectedPipelineName));
  }

  @Test
  public void createPipelineName_multipleDiameters() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    padPipeline.setPipelineInBundle(false);

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline, List.of());
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

    padPipelinePersisterService.savePadPipelineAndMaterialiseIdentData(padPipeline, List.of());
    var expectedPipelineName = "my ref - 5 Millimetre " + PipelineType.PRODUCTION_FLOWLINE.getDisplayName() + " (my bundle)";
    assertThat(padPipeline.getPipelineName().equals(expectedPipelineName));
  }


}
