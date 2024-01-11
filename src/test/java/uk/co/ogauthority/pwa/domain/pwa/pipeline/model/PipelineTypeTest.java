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
    assertThat(hydrogenPipelines)
        .isNotEmpty()
        .doesNotContain(PipelineCoreType.MULTI_CORE);
  }

  @Test
  public void hydrogenType_SpecificExclusions() {
    var hydrogenPipelines = PipelineType.streamDisplayValues(PwaResourceType.HYDROGEN)
        .collect(Collectors.toSet());
    assertThat(hydrogenPipelines)
        .isNotEmpty()
        .doesNotContainAnyElementsOf(List.of(
            PipelineType.GAS_LIFT_PIPELINE,
            PipelineType.GAS_LIFT_JUMPER,
            PipelineType.WATER_INJECTION_PIPELINE,
            PipelineType.WATER_INJECTION_JUMPER,
            PipelineType.POLYMER_INJECTION_PIPELINE
        ));
  }

  @Test
  public void petroleumType_SpecificExclusions() {
    var hydrogenPipelines = PipelineType.streamDisplayValues(PwaResourceType.PETROLEUM)
        .collect(Collectors.toSet());
    assertThat(hydrogenPipelines)
        .isNotEmpty()
        .doesNotContainAnyElementsOf(List.of(PipelineType.HYDROGEN_TRANSPORT));
  }

  @Test
  public void ccusType_includesAllPetroleum() {
    var ccusPipeline = PipelineType.streamDisplayValues(PwaResourceType.CCUS)
        .collect(Collectors.toList());
    var petroleumPipeline = PipelineType.streamDisplayValues(PwaResourceType.PETROLEUM)
        .collect(Collectors.toList());
    assertThat(ccusPipeline).containsAll(petroleumPipeline);
    assertThat(ccusPipeline).contains(PipelineType.CARBON_DIOXIDE_PIPELINE);
  }
}
