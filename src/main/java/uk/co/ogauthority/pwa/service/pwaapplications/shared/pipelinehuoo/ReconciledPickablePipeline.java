package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;

/**
 * Captures relationship between pickablePipelineId and actual pipeline.
 * */
public class ReconciledPickablePipeline {

  private final PickablePipelineId pickablePipelineId;
  private final PipelineId pipelineId;

  public ReconciledPickablePipeline(
      PickablePipelineId pickablePipelineId, PipelineId pipelineId) {
    this.pickablePipelineId = pickablePipelineId;
    this.pipelineId = pipelineId;
  }

  public PickablePipelineId getPickablePipelineId() {
    return pickablePipelineId;
  }

  public PipelineId getPipelineId() {
    return pipelineId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReconciledPickablePipeline that = (ReconciledPickablePipeline) o;
    return Objects.equals(pickablePipelineId, that.pickablePipelineId)
        && Objects.equals(pipelineId, that.pipelineId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pickablePipelineId, pipelineId);
  }
}
