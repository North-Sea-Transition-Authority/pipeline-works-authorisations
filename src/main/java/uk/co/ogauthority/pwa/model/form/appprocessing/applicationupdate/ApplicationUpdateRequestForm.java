package uk.co.ogauthority.pwa.model.form.appprocessing.applicationupdate;

import java.util.Objects;

public class ApplicationUpdateRequestForm {

  private String requestReason;

  private String deadlineTimestampStr;


  public String getRequestReason() {
    return requestReason;
  }

  public void setRequestReason(String requestReason) {
    this.requestReason = requestReason;
  }

  public String getDeadlineTimestampStr() {
    return deadlineTimestampStr;
  }

  public void setDeadlineTimestampStr(String deadlineTimestampStr) {
    this.deadlineTimestampStr = deadlineTimestampStr;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationUpdateRequestForm that = (ApplicationUpdateRequestForm) o;
    return Objects.equals(requestReason, that.requestReason)
        && Objects.equals(deadlineTimestampStr, that.deadlineTimestampStr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestReason, deadlineTimestampStr);
  }
}
