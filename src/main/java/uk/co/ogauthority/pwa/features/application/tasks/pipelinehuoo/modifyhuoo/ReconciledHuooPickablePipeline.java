package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo;

import java.util.Objects;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;

/**
 * Captures relationship between pickablePipelineId and valid pipeline identifiers.
 */
public class ReconciledHuooPickablePipeline {

  private final PickableHuooPipelineId pickableHuooPipelineId;
  private final PipelineIdentifier pipelineIdentifier;

  public ReconciledHuooPickablePipeline(
      PickableHuooPipelineId pickableHuooPipelineId, PipelineIdentifier pipelineIdentifier) {
    this.pickableHuooPipelineId = pickableHuooPipelineId;
    this.pipelineIdentifier = pipelineIdentifier;
  }

  public PickableHuooPipelineId getPickableHuooPipelineId() {
    return pickableHuooPipelineId;
  }

  public String getPickableIdAsString() {
    return this.pickableHuooPipelineId.asString();
  }

  public PipelineIdentifier getPipelineIdentifier() {
    return pipelineIdentifier;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReconciledHuooPickablePipeline that = (ReconciledHuooPickablePipeline) o;
    return Objects.equals(pickableHuooPipelineId, that.pickableHuooPipelineId)
        && Objects.equals(pipelineIdentifier, that.pipelineIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pickableHuooPipelineId, pipelineIdentifier);
  }
}
