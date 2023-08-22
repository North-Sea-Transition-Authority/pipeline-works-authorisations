package uk.co.ogauthority.pwa.model.view.publicnotice;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;

public class PublicNoticeView {


  private final PublicNoticeStatus status;
  private final PublicNoticeRequestStatus publicNoticeRequestStatus;
  private final String submittedTimestamp;
  private final String latestDocumentComments;
  private final String withdrawnByPersonName;
  private final String withdrawnTimestamp;
  private final String withdrawalReason;
  private final String publicationStartTimestamp;
  private final String publicationEndTimestamp;
  private final String rejectionReason;
  private final String documentDownloadUrl;
  private final List<PublicNoticeEvent> publicNoticeEvents;
  private final Map<String, String> personIdNameMap;


  //constructor for only the fields that are required as a minimum for a public notice view

  public PublicNoticeView(PublicNoticeStatus status, String submittedTimestamp, PublicNoticeRequestStatus requestStatus) {
    this.status = status;
    this.submittedTimestamp = submittedTimestamp;
    this.latestDocumentComments = null;
    this.withdrawnByPersonName = null;
    this.withdrawnTimestamp = null;
    this.withdrawalReason = null;
    this.publicationStartTimestamp = null;
    this.publicationEndTimestamp = null;
    this.publicNoticeRequestStatus = requestStatus;
    this.rejectionReason = null;
    this.documentDownloadUrl = null;
    this.publicNoticeEvents = null;
    this.personIdNameMap = null;
  }

  public PublicNoticeView(PublicNoticeStatus status,
                          String submittedTimestamp,
                          String latestDocumentComments,
                          String withdrawnByPersonName,
                          String withdrawnTimestamp,
                          String withdrawalReason,
                          String publicationStartTimestamp,
                          String publicationEndTimestamp,
                          PublicNoticeRequestStatus publicNoticeRequestStatus,
                          String rejectionReason,
                          String documentDownloadUrl,
                          List<PublicNoticeEvent> publicNoticeEvents, Map<String, String> personIdNameMap) {
    this.status = status;
    this.submittedTimestamp = submittedTimestamp;
    this.latestDocumentComments = latestDocumentComments;
    this.withdrawnByPersonName = withdrawnByPersonName;
    this.withdrawnTimestamp = withdrawnTimestamp;
    this.withdrawalReason = withdrawalReason;
    this.publicationStartTimestamp = publicationStartTimestamp;
    this.publicationEndTimestamp = publicationEndTimestamp;
    this.publicNoticeRequestStatus = publicNoticeRequestStatus;
    this.rejectionReason = rejectionReason;
    this.documentDownloadUrl = documentDownloadUrl;
    this.publicNoticeEvents = publicNoticeEvents;
    this.personIdNameMap = personIdNameMap;
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

  public String getWithdrawalReason() {
    return withdrawalReason;
  }

  public String getLatestDocumentComments() {
    return latestDocumentComments;
  }

  public String getPublicationStartTimestamp() {
    return publicationStartTimestamp;
  }

  public String getPublicationEndTimestamp() {
    return publicationEndTimestamp;
  }

  public PublicNoticeRequestStatus getPublicNoticeRequestStatus() {
    return publicNoticeRequestStatus;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public String getDocumentDownloadUrl() {
    return documentDownloadUrl;
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
        && Objects.equals(latestDocumentComments, that.latestDocumentComments)
        && Objects.equals(withdrawnByPersonName, that.withdrawnByPersonName)
        && Objects.equals(withdrawnTimestamp, that.withdrawnTimestamp)
        && Objects.equals(withdrawalReason, that.withdrawalReason)
        && Objects.equals(publicationStartTimestamp, that.publicationStartTimestamp)
        && Objects.equals(publicationEndTimestamp, that.publicationEndTimestamp)
        && publicNoticeRequestStatus == that.publicNoticeRequestStatus
        && Objects.equals(rejectionReason, that.rejectionReason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, submittedTimestamp, latestDocumentComments, withdrawnByPersonName,
        withdrawnTimestamp, withdrawalReason, publicationStartTimestamp, publicationEndTimestamp, publicNoticeRequestStatus,
        rejectionReason);
  }

  public List<PublicNoticeEvent> getPublicNoticeEvents() {
    return publicNoticeEvents;
  }

  public Map<String, String> getPersonIdNameMap() {
    return personIdNameMap;
  }
}
