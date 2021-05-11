package uk.co.ogauthority.pwa.service.asbuilt;

import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineDetailId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.PipelineChangeCategory;

/**
 * Data structure which captures enough information to generate a single as-built pipeline notification within a notification group.
 */
public final class AsBuiltPipelineNotificationSpec {
  private final PipelineDetailId pipelineDetailId;
  private final PipelineChangeCategory pipelineChangeCategory;

  public AsBuiltPipelineNotificationSpec(PipelineDetailId pipelineDetailId,
                                         PipelineChangeCategory pipelineChangeCategory) {
    this.pipelineDetailId = pipelineDetailId;
    this.pipelineChangeCategory = pipelineChangeCategory;
  }

  public PipelineDetailId getPipelineDetailId() {
    return pipelineDetailId;
  }

  public PipelineChangeCategory getPipelineChangeCategory() {
    return pipelineChangeCategory;
  }
}
