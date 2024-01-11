package uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers;

public class PadPipelineTransferClaimForm {

  private Integer pipelineId;

  private boolean assignNewPipelineNumber;

  private String lastIntelligentlyPigged;

  private boolean compatibleWithTarget;

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

  public String getLastIntelligentlyPigged() {
    return lastIntelligentlyPigged;
  }

  public PadPipelineTransferClaimForm setLastIntelligentlyPigged(String lastIntelligentlyPigged) {
    this.lastIntelligentlyPigged = lastIntelligentlyPigged;
    return this;
  }

  public boolean isCompatibleWithTarget() {
    return compatibleWithTarget;
  }

  public PadPipelineTransferClaimForm setCompatibleWithTarget(boolean compatibleWithTarget) {
    this.compatibleWithTarget = compatibleWithTarget;
    return this;
  }
}
