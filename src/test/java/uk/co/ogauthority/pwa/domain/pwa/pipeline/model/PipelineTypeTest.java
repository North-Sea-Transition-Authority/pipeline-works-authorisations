package uk.co.ogauthority.pwa.domain.pwa.pipeline.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;

@ExtendWith(MockitoExtension.class)
class PipelineTypeTest {

  @Test
  void hydrogenType_noMultiCore() {
    var hydrogenPipelines = PipelineType.streamDisplayValues(PwaResourceType.HYDROGEN)
        .map(PipelineType::getCoreType)
        .collect(Collectors.toSet());
    assertThat(hydrogenPipelines)
        .isNotEmpty()
        .doesNotContain(PipelineCoreType.MULTI_CORE);
  }

  @Test
  void hydrogenType_SpecificExclusions() {
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
  void petroleumType_SpecificExclusions() {
    var hydrogenPipelines = PipelineType.streamDisplayValues(PwaResourceType.PETROLEUM)
        .collect(Collectors.toSet());
    assertThat(hydrogenPipelines)
        .isNotEmpty()
        .doesNotContainAnyElementsOf(List.of(PipelineType.HYDROGEN_TRANSPORT));
  }

  @Test
  void ccusType_includesAllPetroleum() {
    var ccusPipeline = PipelineType.streamDisplayValues(PwaResourceType.CCUS)
        .collect(Collectors.toList());
    var petroleumPipeline = PipelineType.streamDisplayValues(PwaResourceType.PETROLEUM)
        .collect(Collectors.toList());
    assertThat(ccusPipeline).containsAll(petroleumPipeline);
    assertThat(ccusPipeline).contains(PipelineType.CARBON_DIOXIDE_PIPELINE);
  }
}
