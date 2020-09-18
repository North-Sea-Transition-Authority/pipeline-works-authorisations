package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.appdetailreconciliation;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

public class ReconciledPadPipelinePair {

  private final PadPipeline sourceDetailPadPipeline;
  private final PadPipeline reconciledPadPipeline;

  ReconciledPadPipelinePair(
      PadPipeline sourceDetailPadPipeline,
      PadPipeline reconciledPadPipeline) {
    this.sourceDetailPadPipeline = sourceDetailPadPipeline;
    this.reconciledPadPipeline = reconciledPadPipeline;
  }

  public PadPipeline getSourceDetailPadPipeline() {
    return sourceDetailPadPipeline;
  }

  public PadPipeline getReconciledPadPipeline() {
    return reconciledPadPipeline;
  }
}
