package uk.co.ogauthority.pwa.model.form.publicnotice;

import uk.co.ogauthority.pwa.service.enums.workflow.publicnotice.PwaApplicationPublicNoticeApprovalResult;

public class PublicNoticeApprovalForm {


  private PwaApplicationPublicNoticeApprovalResult requestApproved;
  private String requestRejectedReason;


  public PwaApplicationPublicNoticeApprovalResult getRequestApproved() {
    return requestApproved;
  }

  public void setRequestApproved(PwaApplicationPublicNoticeApprovalResult requestApproved) {
    this.requestApproved = requestApproved;
  }

  public String getRequestRejectedReason() {
    return requestRejectedReason;
  }

  public void setRequestRejectedReason(String requestRejectedReason) {
    this.requestRejectedReason = requestRejectedReason;
  }
}
