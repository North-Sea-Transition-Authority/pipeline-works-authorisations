package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.PipelinesSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummaryService;
import uk.co.ogauthority.pwa.service.search.consents.pwapipelineview.testutil.PwaPipelineViewTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaPipelineHistoryViewServiceTest {

  @Mock
  private PipelineDiffableSummaryService pipelineDiffableSummaryService;

  @Mock
  private PipelinesSummaryService pipelinesSummaryService;

  private PwaPipelineHistoryViewService pwaPipelineHistoryViewService;


  @Before
  public void setUp() throws Exception {
    pwaPipelineHistoryViewService = new PwaPipelineHistoryViewService(pipelineDiffableSummaryService,
        pipelinesSummaryService);
  }


  @Test
  public void getPipelineSummary() {

    int pipelineId1 = 1;
    var summary = PwaPipelineViewTestUtil.createPipelineDiffableSummary(pipelineId1);
    when(pipelineDiffableSummaryService.getConsentedPipeline(pipelineId1)).thenReturn(summary);

    pwaPipelineHistoryViewService.getDiffedPipelineSummaryModel(pipelineId1);
    verify(pipelinesSummaryService, times(1)).produceDiffedPipelineModel(summary, summary);
  }



}