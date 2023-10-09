package uk.co.ogauthority.pwa.features.application.tasks.pipelines.importconsented;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;

public class ModifyPipelineForm {

  private String pipelineId;
  private PipelineStatus pipelineStatus;
  private String outOfUseStatusReason;

  private String transferStatusReason;
  private Boolean transferAgreed;

  public String getPipelineId() {
    return pipelineId;
  }

  public void setPipelineId(String pipelineId) {
    this.pipelineId = pipelineId;
  }

  public PipelineStatus getPipelineStatus() {
    return pipelineStatus;
  }

  public void setPipelineStatus(PipelineStatus pipelineStatus) {
    this.pipelineStatus = pipelineStatus;
  }

  public String getOutOfUseStatusReason() {
    return outOfUseStatusReason;
  }

  public ModifyPipelineForm setOutOfUseStatusReason(String outOfUseStatusReason) {
    this.outOfUseStatusReason = outOfUseStatusReason;
    return this;
  }

  public String getTransferStatusReason() {
    return transferStatusReason;
  }

  public ModifyPipelineForm setTransferStatusReason(String transferStatusReason) {
    this.transferStatusReason = transferStatusReason;
    return this;
  }

  public Boolean getTransferAgreed() {
    return transferAgreed;
  }

  public void setTransferAgreed(Boolean transferAgreed) {
    this.transferAgreed = transferAgreed;
  }
}
