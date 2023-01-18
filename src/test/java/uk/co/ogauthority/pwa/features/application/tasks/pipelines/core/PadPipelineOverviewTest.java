package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;

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
    padPipeline.setPipelineType(PipelineType.HYDRAULIC_JUMPER_MULTI_CORE);
    padPipeline.setPipelineInBundle(false);

    padPipelineOverview = new PadPipelineOverview(padPipeline);
    var expectedPipelineName = "my ref - " + PipelineType.HYDRAULIC_JUMPER_MULTI_CORE.getDisplayName();
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

  @Test
  public void getRelevantQuestions_all() throws IllegalAccessException {

    var pipe = new PadPipeline();
    pipe.setAlreadyExistsOnSeabed(true);
    pipe.setPipelineStatusReason("reason");

    var overview = new PadPipelineOverview(pipe);
    FieldUtils.writeDeclaredField(overview, "pipelineStatusReason", "reason", true);
    FieldUtils.writeDeclaredField(overview, "alreadyExistsOnSeabed", true, true);

    assertThat(overview.getRelevantQuestions())
        .containsExactlyInAnyOrder(PipelineHeaderQuestion.OUT_OF_USE_ON_SEABED_REASON, PipelineHeaderQuestion.ALREADY_EXISTS_ON_SEABED);

  }

  @Test
  public void getRelevantQuestions_outOfUseReason() throws IllegalAccessException {

    var pipe = new PadPipeline();

    var overview = new PadPipelineOverview(pipe);
    FieldUtils.writeDeclaredField(overview, "pipelineStatusReason", "reason", true);

    assertThat(overview.getRelevantQuestions()).containsExactly(PipelineHeaderQuestion.OUT_OF_USE_ON_SEABED_REASON);

  }

  @Test
  public void getRelevantQuestions_alreadyExists() throws IllegalAccessException {

    var pipe = new PadPipeline();

    var overview = new PadPipelineOverview(pipe);
    FieldUtils.writeDeclaredField(overview, "alreadyExistsOnSeabed", true, true);

    assertThat(overview.getRelevantQuestions()).containsExactly(PipelineHeaderQuestion.ALREADY_EXISTS_ON_SEABED);

  }

}
