package uk.co.ogauthority.pwa.features.generalcase.pipelineview;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailIdentViewService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

/**
 * Service to create diff friendly summaries of pipelines for an application or from a Master PWA.
 */
@Service
public class PipelineDiffableSummaryService {

  private final PadPipelineService padPipelineService;
  private final PadPipelineIdentService padPipelineIdentService;
  private final PipelineDetailIdentViewService pipelineDetailIdentViewService;
  private final PipelineDetailService pipelineDetailService;
  private final PadTechnicalDrawingService padTechnicalDrawingService;
  private final MasterPwaService masterPwaService;

  @Autowired
  public PipelineDiffableSummaryService(PadPipelineService padPipelineService,
                                        PadPipelineIdentService padPipelineIdentService,
                                        PipelineDetailIdentViewService pipelineDetailIdentViewService,
                                        PipelineDetailService pipelineDetailService,
                                        PadTechnicalDrawingService padTechnicalDrawingService,
                                        MasterPwaService masterPwaService) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.pipelineDetailIdentViewService = pipelineDetailIdentViewService;
    this.pipelineDetailService = pipelineDetailService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.masterPwaService = masterPwaService;
  }

  public List<PipelineDiffableSummary> getApplicationDetailPipelines(PwaApplicationDetail pwaApplicationDetail) {

    // Nested loop with a database hits, prime candidate for performance tuning effort.
    var pipelineOverviews = padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail);

    var pipelineIdDrawingViewMap = padTechnicalDrawingService.getPipelineDrawingViewsMap(pwaApplicationDetail);

    return pipelineOverviews.stream()
        .map(pipelineOverview -> {
          var pipelineId = padPipelineService.getById(pipelineOverview.getPadPipelineId()).getPipeline().getId();
          var pipelineDetail = pipelineDetailService.getLatestByPipelineId(pipelineId);

              return PipelineDiffableSummary.from(
                  new PipelineHeaderView(
                      pipelineOverview,

                      pipelineDetail.getTransferredFrom() != null
                          ? masterPwaService.getCurrentDetailOrThrow(pipelineDetail.getTransferredFrom()).getReference()
                          : null,

                      pipelineDetail.getTransferredTo() != null
                          ? masterPwaService.getCurrentDetailOrThrow(pipelineDetail.getTransferredTo()).getReference()
                          : null
                  ),
                  padPipelineIdentService.getIdentViewsFromOverview(pipelineOverview),
                  pipelineIdDrawingViewMap.get(new PipelineId(pipelineOverview.getPipelineId())));
            }
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

          var identViews = pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipeline(pipelineDetail.getPipelineId());

          var transferredFromRef = pipelineDetail.getTransferredFrom() != null
              ? masterPwaService.getCurrentDetailOrThrow(pipelineDetail.getTransferredFrom()).getReference()
              : null;

          var transferredToRef = pipelineDetail.getTransferredTo() != null
              ? masterPwaService.getCurrentDetailOrThrow(pipelineDetail.getTransferredTo()).getReference()
              : null;

          PipelineHeaderView pipelineHeaderView = new PipelineHeaderView(pipelineDetail, transferredFromRef, transferredToRef);

          return PipelineDiffableSummary.from(pipelineHeaderView, identViews, null);

        })
        .collect(Collectors.toList());
  }

  public PipelineDiffableSummary getConsentedPipeline(Integer pipelineDetailId) {

    var pipelineDetail = pipelineDetailService.getByPipelineDetailId(pipelineDetailId);

    var identViews = pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipelineDetail(
        pipelineDetail.getPipelineId(), pipelineDetailId);

    var transferredFromRef = pipelineDetail.getTransferredFrom() != null
        ? masterPwaService.getCurrentDetailOrThrow(pipelineDetail.getTransferredFrom()).getReference()
        : null;

    var transferredToRef = pipelineDetail.getTransferredTo() != null
        ? masterPwaService.getCurrentDetailOrThrow(pipelineDetail.getTransferredTo()).getReference()
        : null;

    PipelineHeaderView pipelineHeaderView = new PipelineHeaderView(pipelineDetail, transferredFromRef, transferredToRef);

    return PipelineDiffableSummary.from(pipelineHeaderView, identViews, null);

  }

}
