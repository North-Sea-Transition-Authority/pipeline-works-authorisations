package uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineOverview;
import uk.co.ogauthority.pwa.features.generalcase.pipelinehuooview.PipelineNumberAndSplitsService;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;

@ExtendWith(MockitoExtension.class)
class PipelineNumberAndSplitsServiceTest {

  private PipelineNumberAndSplitsService pipelineNumberAndSplitsService;

  @BeforeEach
  void setUp() {
    pipelineNumberAndSplitsService = new PipelineNumberAndSplitsService();
  }

  @Test
  void getAllPipelineNumbersAndSplitsRole() {

    Map<PipelineId, PipelineOverview> pipelineIdAndSummaryMap = new HashMap<>();
    pipelineIdAndSummaryMap.put(new PipelineId(1), getPipelineOverview(1));

    var pipelineNumbersAndSplits = pipelineNumberAndSplitsService
        .getAllPipelineNumbersAndSplitsRole(() -> pipelineIdAndSummaryMap, Set::of);

    assertThat(pipelineNumbersAndSplits).hasSize(1);
    assertThat(pipelineNumbersAndSplits.get(new PipelineId(1)).getPipelineIdentifier().getPipelineIdAsInt())
        .isEqualTo(1);
    assertThat(pipelineNumbersAndSplits.get(new PipelineId(1)).getPipelineNumber()).isEqualTo("1");
  }

  private PadPipelineOverview getPipelineOverview(int id) {
    var padPipeline = new PadPipeline();
    padPipeline.setId(id);
    Pipeline pipeline = new Pipeline();
    pipeline.setId(id);
    padPipeline.setPipeline(pipeline);
    padPipeline.setPipelineRef(String.valueOf(id));
    return new PadPipelineOverview(padPipeline);
  }

  @Test
  void getAllPipelineNumbersAndSplitsRole_splitsTakePriority() {

    Map<PipelineId, PipelineOverview> pipelineIdAndSummaryMap = new HashMap<>();
    var id1 = new PipelineId(1);
    pipelineIdAndSummaryMap.put(id1, getPipelineOverview(1));
    var id2 = new PipelineId(2);
    pipelineIdAndSummaryMap.put(id2, getPipelineOverview(2));

    // split second pipe
    var split1 = PipelineSection.from(2, "from", IdentLocationInclusionMode.INCLUSIVE, "middle", IdentLocationInclusionMode.EXCLUSIVE, 1);
    var split2 = PipelineSection.from(2, "middle", IdentLocationInclusionMode.INCLUSIVE, "end", IdentLocationInclusionMode.INCLUSIVE, 2);
    var splitIdentifiers = new HashSet<PipelineIdentifier>();
    splitIdentifiers.add(split1);
    splitIdentifiers.add(split2);

    var pipelineNumbersAndSplits = pipelineNumberAndSplitsService
        .getAllPipelineNumbersAndSplitsRole(() -> pipelineIdAndSummaryMap, () -> splitIdentifiers);

    assertThat(pipelineNumbersAndSplits).hasSize(3);

    assertThat(pipelineNumbersAndSplits.get(id1).getPipelineIdentifier().getPipelineIdAsInt()).isEqualTo(1);
    assertThat(pipelineNumbersAndSplits.get(id1).getPipelineNumber()).isEqualTo("1");

    // only the split sections exist for pipe 2, not whole pipe
    assertThat(pipelineNumbersAndSplits.get(id2)).isNull();

    assertThat(pipelineNumbersAndSplits.get(split1)).satisfies(numberAndSplit -> {
      assertThat(numberAndSplit.getPipelineNumber()).isEqualTo("2");
      assertThat(numberAndSplit.getPipelineIdentifier().getPipelineIdAsInt()).isEqualTo(2);
      assertThat(numberAndSplit.getSplitInfo()).isEqualTo("From and including from to and not including middle");
    });

    assertThat(pipelineNumbersAndSplits.get(split2)).satisfies(numberAndSplit -> {
      assertThat(numberAndSplit.getPipelineNumber()).isEqualTo("2");
      assertThat(numberAndSplit.getPipelineIdentifier().getPipelineIdAsInt()).isEqualTo(2);
      assertThat(numberAndSplit.getSplitInfo()).isEqualTo("From and including middle to and including end");
    });

  }

}