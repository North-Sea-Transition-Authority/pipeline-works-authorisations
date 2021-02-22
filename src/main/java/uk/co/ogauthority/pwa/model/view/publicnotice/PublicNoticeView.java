package uk.co.ogauthority.pwa.model.view.publicnotice;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;

public class PublicNoticeView {


  private final PublicNoticeStatus status;
  private final String submittedTimestamp;

  public PublicNoticeView(PublicNoticeStatus status, String submittedTimestamp) {
    this.status = status;
    this.submittedTimestamp = submittedTimestamp;
  }


  public PublicNoticeStatus getStatus() {
    return status;
  }

  public String getSubmittedTimestamp() {
    return submittedTimestamp;
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
        && Objects.equals(submittedTimestamp, that.submittedTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, submittedTimestamp);
  }
}
