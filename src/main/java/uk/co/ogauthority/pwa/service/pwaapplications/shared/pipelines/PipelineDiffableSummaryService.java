package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.NamedPipelineDto;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailIdentService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;

/**
 * Service to create diff friendly summaries of pipelines for an application or from a Master PWA.
 */
@Service
public class PipelineDiffableSummaryService {

  private final PadPipelineService padPipelineService;
  private final PadPipelineIdentService padPipelineIdentService;

  private final PipelineDetailIdentService pipelineDetailIdentService;
  private final PipelineDetailService pipelineDetailService;

  @Autowired
  public PipelineDiffableSummaryService(PadPipelineService padPipelineService,
                                        PadPipelineIdentService padPipelineIdentService,
                                        PipelineDetailIdentService pipelineDetailIdentService,
                                        PipelineDetailService pipelineDetailService) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.pipelineDetailIdentService = pipelineDetailIdentService;
    this.pipelineDetailService = pipelineDetailService;
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

  public List<PipelineDiffableSummary> getConsentedPipelines(PwaApplication pwaApplication,
                                                             Set<PipelineId> pipelineIds) {

    var consentedPipelineDetails = pipelineDetailService.getActivePipelineDetailsForApplicationMasterPwaById(
        pwaApplication,
        pipelineIds);

    return consentedPipelineDetails.stream()
        .map(pipelineDetail ->  {
          var identViews = pipelineDetailIdentService.getSortedPipelineIdentViewsForPipeline(pipelineDetail.getPipelineId());
          var namedPipeline = NamedPipelineDto.fromPipelineDetail(pipelineDetail);
          return PipelineDiffableSummary.from(namedPipeline, identViews);
        })
        .collect(Collectors.toList());
  }

}
