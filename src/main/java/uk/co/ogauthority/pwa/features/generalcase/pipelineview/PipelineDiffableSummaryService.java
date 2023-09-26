package uk.co.ogauthority.pwa.features.generalcase.pipelineview;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PadTechnicalDrawingService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.TransferParticipantType;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
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
  private final PadPipelineTransferService padPipelineTransferService;
  private final MasterPwaService masterPwaService;

  @Autowired
  public PipelineDiffableSummaryService(PadPipelineService padPipelineService,
                                        PadPipelineIdentService padPipelineIdentService,
                                        PipelineDetailIdentViewService pipelineDetailIdentViewService,
                                        PipelineDetailService pipelineDetailService,
                                        PadTechnicalDrawingService padTechnicalDrawingService,
                                        PadPipelineTransferService padPipelineTransferService,
                                        MasterPwaService masterPwaService) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.pipelineDetailIdentViewService = pipelineDetailIdentViewService;
    this.pipelineDetailService = pipelineDetailService;
    this.padTechnicalDrawingService = padTechnicalDrawingService;
    this.padPipelineTransferService = padPipelineTransferService;
    this.masterPwaService = masterPwaService;
  }

  public List<PipelineDiffableSummary> getApplicationDetailPipelines(PwaApplicationDetail pwaApplicationDetail) {

    // Nested loop with a database hits, prime candidate for performance tuning effort.
    var pipelineOverviews = padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail);

    var pipelineIdDrawingViewMap = padTechnicalDrawingService.getPipelineDrawingViewsMap(pwaApplicationDetail);

    var pipelineToTransferMap = padPipelineTransferService.getPipelineToTransferMap(pwaApplicationDetail);

    var donorPwasSet = pipelineToTransferMap.entrySet().stream()
        .filter(e -> e.getValue().getTransferParticipantType(e.getKey()) == TransferParticipantType.RECIPIENT)
        .map(e -> e.getValue().getDonorApplicationDetail().getMasterPwa())
        .collect(Collectors.toSet());

    var donorPwaToReferenceMap = masterPwaService.findAllCurrentDetailsIn(donorPwasSet).stream()
        .collect(Collectors.toMap(MasterPwaDetail::getMasterPwa, MasterPwaDetail::getReference));

    var pipelineIdToTransferMap = pipelineToTransferMap.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue));

    var pipelineIdToPipelineMap = pipelineToTransferMap.keySet().stream()
        .collect(Collectors.toMap(Pipeline::getId, Function.identity()));

    return pipelineOverviews.stream()
        .map(pipelineOverview -> {

          var transferOpt = Optional.ofNullable(pipelineIdToTransferMap.get(pipelineOverview.getPipelineId()));
          String transferredFromPwaRef = null;
          if (transferOpt.isPresent()) {
            Pipeline pipeline = pipelineIdToPipelineMap.get(pipelineOverview.getPipelineId());
            if (transferOpt.get().getTransferParticipantType(pipeline) == TransferParticipantType.RECIPIENT) {
              var pwa = transferOpt.get().getDonorApplicationDetail().getMasterPwa();
              transferredFromPwaRef = donorPwaToReferenceMap.get(pwa);
            }
          }

          var pipelineHeaderView = new PipelineHeaderView(pipelineOverview, transferredFromPwaRef, null);
          var identViews = padPipelineIdentService.getIdentViewsFromOverview(pipelineOverview);
          var pipeDrawingSummaryView = pipelineIdDrawingViewMap.get(new PipelineId(pipelineOverview.getPipelineId()));

          return PipelineDiffableSummary.from(pipelineHeaderView, identViews, pipeDrawingSummaryView);

        })
        .collect(Collectors.toList());
  }

  public List<PipelineDiffableSummary> getConsentedPipelines(PwaApplication pwaApplication,
                                                             Set<PipelineId> pipelineIds) {

    var consentedPipelineDetails = pipelineDetailService.getActivePipelineDetailsForApplicationMasterPwaById(
        pwaApplication,
        pipelineIds);

    var transferPwasSet = new HashSet<MasterPwa>();

    consentedPipelineDetails.forEach(pipelineDetail -> {

      if (pipelineDetail.getTransferredFromPipeline() != null) {
        transferPwasSet.add(pipelineDetail.getTransferredFromPipeline().getMasterPwa());
      }

      if (pipelineDetail.getTransferredToPipeline() != null) {
        transferPwasSet.add(pipelineDetail.getTransferredToPipeline().getMasterPwa());
      }

    });

    var transferPwaToReferenceMap = masterPwaService.findAllCurrentDetailsIn(transferPwasSet).stream()
        .collect(Collectors.toMap(MasterPwaDetail::getMasterPwa, MasterPwaDetail::getReference));

    return consentedPipelineDetails.stream()
        .map(pipelineDetail ->  {

          var identViews = pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipeline(pipelineDetail.getPipelineId());

          var transferredFromRef = Optional.ofNullable(pipelineDetail.getTransferredFromPipeline())
              .map(pipe -> transferPwaToReferenceMap.get(pipe.getMasterPwa()))
              .orElse(null);

          var transferredToRef = Optional.ofNullable(pipelineDetail.getTransferredToPipeline())
              .map(pipe -> transferPwaToReferenceMap.get(pipe.getMasterPwa()))
              .orElse(null);

          PipelineHeaderView pipelineHeaderView = new PipelineHeaderView(pipelineDetail, transferredFromRef, transferredToRef);

          return PipelineDiffableSummary.from(pipelineHeaderView, identViews, null);

        })
        .collect(Collectors.toList());
  }

  public PipelineDiffableSummary getConsentedPipelineDetailSummary(Integer pipelineDetailId) {

    var pipelineDetail = pipelineDetailService.getByPipelineDetailId(pipelineDetailId);

    var identViews = pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipelineDetail(
        pipelineDetail.getPipelineId(), pipelineDetailId);

    String transferredFromPwaRef = null;
    String transferredToPwaRef = null;

    if (pipelineDetail.getTransferredFromPipeline() != null) {
      var fromPwaDetail = masterPwaService.getCurrentDetailOrThrow(pipelineDetail.getTransferredFromPipeline().getMasterPwa());
      transferredFromPwaRef = fromPwaDetail.getReference();
    }

    if (pipelineDetail.getTransferredToPipeline() != null) {
      var toPwaDetail = masterPwaService.getCurrentDetailOrThrow(pipelineDetail.getTransferredToPipeline().getMasterPwa());
      transferredToPwaRef = toPwaDetail.getReference();
    }

    PipelineHeaderView pipelineHeaderView = new PipelineHeaderView(pipelineDetail, transferredFromPwaRef, transferredToPwaRef);

    return PipelineDiffableSummary.from(pipelineHeaderView, identViews, null);

  }

}
