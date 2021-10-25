package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineOverviewTest {

  private PadPipelineOverview padPipelineOverview;


  @Test
  public void getPipelineName_singleDiameter() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setMaxExternalDiameter(BigDecimal.valueOf(5));
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(false);

    padPipelineOverview = new PadPipelineOverview(padPipeline);
    var expectedPipelineName = "my ref - 5 Millimetre " + PipelineType.PRODUCTION_FLOWLINE.getDisplayName();
    assertThat(padPipelineOverview.getPipelineName()).isEqualTo(expectedPipelineName);
  }

  @Test
  public void getPipelineName_multipleDiameters() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    padPipeline.setPipelineInBundle(false);

    padPipelineOverview = new PadPipelineOverview(padPipeline);
    var expectedPipelineName = "my ref - " + PipelineType.HYDRAULIC_JUMPER.getDisplayName();
    assertThat(padPipelineOverview.getPipelineName()).isEqualTo(expectedPipelineName);
  }

  @Test
  public void getPipelineName_singleDiameter_partOfBundle() {
    PadPipeline padPipeline = new PadPipeline();
    padPipeline.setPipelineRef("my ref");
    padPipeline.setMaxExternalDiameter(BigDecimal.valueOf(5));
    padPipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    padPipeline.setPipelineInBundle(true);
    padPipeline.setBundleName("my bundle");

    padPipelineOverview = new PadPipelineOverview(padPipeline);
    var expectedPipelineName = "my ref - 5 Millimetre " + PipelineType.PRODUCTION_FLOWLINE.getDisplayName() + " (my bundle)";
    assertThat(padPipelineOverview.getPipelineName()).isEqualTo(expectedPipelineName);
  }

}
