package uk.co.ogauthority.pwa.model.view.publicnotice;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;

public class PublicNoticeView {


  private final PublicNoticeStatus status;
  private final String submittedTimestamp;
  private final String withdrawnByPersonName;
  private final String withdrawnTimestamp;


  //constructor for only the fields that are required as a minimum for a public notice view
  public PublicNoticeView(PublicNoticeStatus status, String submittedTimestamp) {
    this.status = status;
    this.submittedTimestamp = submittedTimestamp;
    this.withdrawnByPersonName = null;
    this.withdrawnTimestamp = null;
  }

  public PublicNoticeView(PublicNoticeStatus status, String submittedTimestamp, String withdrawnByPersonName,
                          String withdrawnTimestamp) {
    this.status = status;
    this.submittedTimestamp = submittedTimestamp;
    this.withdrawnByPersonName = withdrawnByPersonName;
    this.withdrawnTimestamp = withdrawnTimestamp;
  }


  public PublicNoticeStatus getStatus() {
    return status;
  }

  public String getSubmittedTimestamp() {
    return submittedTimestamp;
  }

  public String getWithdrawnByPersonName() {
    return withdrawnByPersonName;
  }

  public String getWithdrawnTimestamp() {
    return withdrawnTimestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicNoticeView that = (PublicNoticeView) o;
    return status == that.status
        && Objects.equals(submittedTimestamp, that.submittedTimestamp)
        && Objects.equals(withdrawnByPersonName, that.withdrawnByPersonName)
        && Objects.equals(withdrawnTimestamp, that.withdrawnTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, submittedTimestamp, withdrawnByPersonName, withdrawnTimestamp);
  }
}
