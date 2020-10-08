package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;

@RunWith(MockitoJUnitRunner.class)
public class PipelineNumberAndSplitsServiceTest {

  private PipelineNumberAndSplitsService pipelineNumberAndSplitsService;

  @Before
  public void setUp() {
    pipelineNumberAndSplitsService = new PipelineNumberAndSplitsService();

  }



  @Test
  public void getAllPipelineNumbersAndSplitsRole() {

    Set<PipelineIdentifier> pipelineIdentifiers = Set.of(new PipelineId(1));
    Map<PipelineId, PipelineOverview> pipelineIdAndSummaryMap = new HashMap<>();
    var padPipeline = new PadPipeline();
    padPipeline.setId(1);
    Pipeline pipeline = new Pipeline();
    pipeline.setId(1);
    padPipeline.setPipeline(pipeline);
    padPipeline.setPipelineRef("1");
    var pipelineOverview = new PadPipelineOverview(padPipeline);
    pipelineIdAndSummaryMap.put(new PipelineId(1), pipelineOverview);

    var pipelineNumbersAndSplits = pipelineNumberAndSplitsService.getAllPipelineNumbersAndSplitsRole(
        HuooRole.HOLDER, () -> pipelineIdAndSummaryMap, () -> Set.of(), pipelineIdentifiers);

    assertThat(pipelineNumbersAndSplits).hasSize(1);
    assertThat(pipelineNumbersAndSplits.get(0).getPipelineIdentifier().getPipelineIdAsInt())
        .isEqualTo(1);
  }



}
