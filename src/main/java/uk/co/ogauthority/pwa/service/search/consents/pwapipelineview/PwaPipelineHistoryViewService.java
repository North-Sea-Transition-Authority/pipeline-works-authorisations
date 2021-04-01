package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers.PipelinesSummaryService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineDiffableSummaryService;

@Service
public class PwaPipelineHistoryViewService {

  private final PipelineDiffableSummaryService pipelineDiffableSummaryService;
  private final PipelinesSummaryService pipelinesSummaryService;


  @Autowired
  public PwaPipelineHistoryViewService(PipelineDiffableSummaryService pipelineDiffableSummaryService,
                                       PipelinesSummaryService pipelinesSummaryService) {
    this.pipelineDiffableSummaryService = pipelineDiffableSummaryService;
    this.pipelinesSummaryService = pipelinesSummaryService;
  }


  public Map<String, Object> getDiffedPipelineSummaryModel(Integer pipelineId) {

    var diffableSummary = pipelineDiffableSummaryService.getConsentedPipeline(pipelineId);
    return pipelinesSummaryService.produceDiffedPipelineModel(diffableSummary, diffableSummary);

  }


}
