package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines;

import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;

public class ModifyPipelineForm {

  private String pipelineId;
  private PipelineStatus pipelineStatus;
  private String pipelineStatusReason;
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

  public String getPipelineStatusReason() {
    return pipelineStatusReason;
  }

  public void setPipelineStatusReason(String pipelineStatusReason) {
    this.pipelineStatusReason = pipelineStatusReason;
  }

  public Boolean getTransferAgreed() {
    return transferAgreed;
  }

  public void setTransferAgreed(Boolean transferAgreed) {
    this.transferAgreed = transferAgreed;
  }
}
