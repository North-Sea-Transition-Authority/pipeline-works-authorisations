package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.appdetailreconciliation;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public class ReconciledApplicationDetailPadPipelines {

  private final PwaApplicationDetail sourcePwaApplicationDetail;
  private final PwaApplicationDetail reconciledPwaApplicationDetail;

  private final Map<PipelineId, ReconciledPadPipelinePair> padPipelinePairsLookup;

  ReconciledApplicationDetailPadPipelines(PwaApplicationDetail sourcePwaApplicationDetail,
                                          PwaApplicationDetail reconciledPwaApplicationDetail,
                                          List<PadPipeline> sourcePadPipelines,
                                          List<PadPipeline> toReconcilePadPipelines
  ) {
    this.sourcePwaApplicationDetail = sourcePwaApplicationDetail;
    this.reconciledPwaApplicationDetail = reconciledPwaApplicationDetail;

    var toReconcilePadPipelinesLookup = toReconcilePadPipelines.stream()
        .collect(Collectors.toMap(PadPipeline::getPipelineId, pp -> pp));

    this.padPipelinePairsLookup = new HashMap<>();

    sourcePadPipelines.forEach(sourcePadPipeline -> {
      // will blow up  if cannot reconcile
      if (!toReconcilePadPipelinesLookup.containsKey(sourcePadPipeline.getPipelineId())) {
        throw new PwaEntityNotFoundException(
            String.format(
                "Could not reconcile PipelineId: %s for pad_id: %s",
                sourcePadPipeline.getPipelineId().asInt(),
                reconciledPwaApplicationDetail.getId())
        );
      }
      this.padPipelinePairsLookup.put(
          sourcePadPipeline.getPipelineId(),
          new ReconciledPadPipelinePair(sourcePadPipeline,
              toReconcilePadPipelinesLookup.get(sourcePadPipeline.getPipelineId())));
    });

  }

  @VisibleForTesting
  public int countReconciledPipelines() {
    return this.padPipelinePairsLookup.size();
  }

  public ReconciledPadPipelinePair findByPipelineIdOrError(PipelineId pipelineId) {
    if (!this.padPipelinePairsLookup.containsKey(pipelineId)) {
      throw new PwaEntityNotFoundException(
          "Could not find reconciled pipeline pair for pipeline Id: " + pipelineId.asInt() +
              "\n" + this.toString()
      );
    }

    return this.padPipelinePairsLookup.get(pipelineId);

  }

  @Override
  public String toString() {
    return "ReconciledApplicationDetailPadPipelines{" +
        "sourcePwaApplicationDetail.id=" + sourcePwaApplicationDetail.getId() +
        ", reconciledPwaApplicationDetail.id=" + reconciledPwaApplicationDetail.getId() +
        ", padPipelinePairsLookup=" + padPipelinePairsLookup.keySet().toString() +
        '}';
  }
}
