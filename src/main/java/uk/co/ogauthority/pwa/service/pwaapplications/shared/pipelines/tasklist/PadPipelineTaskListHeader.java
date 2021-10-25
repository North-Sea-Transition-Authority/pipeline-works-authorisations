package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.tasklist;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;

/**
 * Dto object to simplify construction of pipeline task lists entries.
 */
public final class PadPipelineTaskListHeader {

  private PadPipeline padPipeline;
  private int padPipelineId;
  private long numberOfIdents;
  private PipelineStatus pipelineStatus;
  private String pipelineName;

  PadPipelineTaskListHeader(
      PadPipeline padPipeline,
      long numberOfIdents,
      PipelineStatus pipelineStatus,
      String pipelineName) {
    this.padPipeline = padPipeline;
    this.padPipelineId = padPipeline.getId();
    this.numberOfIdents = numberOfIdents;
    this.pipelineStatus = pipelineStatus;
    this.pipelineName = pipelineName;
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
}
