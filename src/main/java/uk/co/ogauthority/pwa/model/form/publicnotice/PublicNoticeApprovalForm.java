package uk.co.ogauthority.pwa.model.form.publicnotice;

public class PublicNoticeApprovalForm {


  private Boolean requestApproved;
  private String requestRejectedReason;


  public Boolean getRequestApproved() {
    return requestApproved;
  }

  public void setRequestApproved(Boolean requestApproved) {
    this.requestApproved = requestApproved;
  }

  public String getRequestRejectedReason() {
    return requestRejectedReason;
  }

  public void setRequestRejectedReason(String requestRejectedReason) {
    this.requestRejectedReason = requestRejectedReason;
  }
}
