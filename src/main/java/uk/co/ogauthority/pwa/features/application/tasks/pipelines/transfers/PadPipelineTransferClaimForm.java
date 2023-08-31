package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

public class PadPipelineTransferClaimForm {

  private Integer pipelineId;

  private boolean assignNewPipelineNumber;

  public Integer getPipelineId() {
    return pipelineId;
  }

  public PadPipelineTransferClaimForm setPipelineId(Integer pipelineId) {
    this.pipelineId = pipelineId;
    return this;
  }

  public boolean getAssignNewPipelineNumber() {
    return assignNewPipelineNumber;
  }

  public PadPipelineTransferClaimForm setAssignNewPipelineNumber(boolean assignNewPipelineNumber) {
    this.assignNewPipelineNumber = assignNewPipelineNumber;
    return this;
  }

}
