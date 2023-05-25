package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

@RunWith(MockitoJUnitRunner.class)
public class PipelineTypeTest {

  @Test
  public void hydrogenType_noMultiCore() {
    var hydrogenPipelines = PipelineType.streamDisplayValues(PwaResourceType.HYDROGEN)
        .map(PipelineType::getCoreType)
        .collect(Collectors.toSet());
    assertThat(hydrogenPipelines).doesNotContain(PipelineCoreType.MULTI_CORE);
  }

  @Test
  public void hydrogenType_SpecificExclusions() {
    var hydrogenPipelines = PipelineType.streamDisplayValues(PwaResourceType.HYDROGEN)
        .collect(Collectors.toSet());
    assertThat(hydrogenPipelines).doesNotContainAnyElementsOf(List.of(
        PipelineType.GAS_LIFT_PIPELINE,
        PipelineType.GAS_LIFT_JUMPER,
        PipelineType.WATER_INJECTION_PIPELINE,
        PipelineType.WATER_INJECTION_JUMPER
    ));
  }

  @Test
  public void petroleumType_SpecificExclusions() {
    var hydrogenPipelines = PipelineType.streamDisplayValues(PwaResourceType.PETROLEUM)
        .collect(Collectors.toSet());
    assertThat(hydrogenPipelines).doesNotContainAnyElementsOf(List.of(
        PipelineType.HYDROGEN_TRANSPORT
    ));
  }
}
