package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Service to create diff friendly summaries of pipelines for an application or from a Master PWA.
 */
@Service
public class PipelineDiffableSummaryService {

  private final PadPipelineService padPipelineService;
  private final PadPipelineIdentService padPipelineIdentService;

  @Autowired
  public PipelineDiffableSummaryService(PadPipelineService padPipelineService,
                                        PadPipelineIdentService padPipelineIdentService) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
  }

  public List<PipelineDiffableSummary> getApplicationDetailPipelines(PwaApplicationDetail pwaApplicationDetail) {
    // Nested loop with a database hits, prime candidate for performance tuning effort.
    var pipelineOverviews = padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail);
    return pipelineOverviews.stream()
        .map(pipelineOverview -> PipelineDiffableSummary.from(
            pipelineOverview,
            padPipelineIdentService.getIdentViewsFromOverview(pipelineOverview))
        )
        .collect(Collectors.toList());

  }


}
