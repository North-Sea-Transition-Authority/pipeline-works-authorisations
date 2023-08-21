package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

public class PadPipelineTransferClaimForm {

  private Integer padPipelineId;

  private boolean assignNewPipelineNumber;

  public Integer getPadPipelineId() {
    return padPipelineId;
  }

  public PadPipelineTransferClaimForm setPadPipelineId(
      Integer padPipelineId) {
    this.padPipelineId = padPipelineId;
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
