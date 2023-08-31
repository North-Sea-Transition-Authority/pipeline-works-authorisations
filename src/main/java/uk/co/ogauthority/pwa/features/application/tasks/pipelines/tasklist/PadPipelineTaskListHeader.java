package uk.co.ogauthority.pwa.features.application.tasks.pipelines.tasklist;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;

/**
 * Dto object to simplify construction of pipeline task lists entries.
 */
public final class PadPipelineTaskListHeader {

  private PadPipeline padPipeline;
  private int padPipelineId;
  private long numberOfIdents;
  private PipelineStatus pipelineStatus;
  private String pipelineName;
  private boolean withdrawnTransfer;

  public PadPipelineTaskListHeader(
      PadPipeline padPipeline,
      long numberOfIdents,
      PipelineStatus pipelineStatus,
      String pipelineName,
      boolean withdrawnTransfer) {
    this.padPipeline = padPipeline;
    this.padPipelineId = padPipeline.getId();
    this.numberOfIdents = numberOfIdents;
    this.pipelineStatus = pipelineStatus;
    this.pipelineName = pipelineName;
    this.withdrawnTransfer = withdrawnTransfer;
  }


  public int getPadPipelineId() {
    return padPipelineId;
  }

  public PadPipeline getPadPipeline() {
    return padPipeline;
  }

  public long getNumberOfIdents() {
    return numberOfIdents;
  }

  public PipelineStatus getPipelineStatus() {
    return pipelineStatus;
  }

  public String getPipelineName() {
    return pipelineName;
  }

  public boolean isWithdrawnTransfer() {
    return withdrawnTransfer;
  }
}
