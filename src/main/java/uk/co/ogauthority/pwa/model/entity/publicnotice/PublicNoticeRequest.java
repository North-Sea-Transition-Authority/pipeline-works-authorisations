package uk.co.ogauthority.pwa.model.entity.publicnotice;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestStatus;

@Entity
@Table(name = "public_notice_requests")
public class PublicNoticeRequest {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "public_notice_id")
  @ManyToOne
  private PublicNotice publicNotice;

  private String coverLetterText;

  @Enumerated(EnumType.STRING)
  private PublicNoticeRequestStatus status;

  @Enumerated(EnumType.STRING)
  private PublicNoticeRequestReason reason;

  private String reasonDescription;

  private Boolean requestApproved;

  private String rejectionReason;

  private Integer version;

  private Instant submittedTimestamp;

  private Instant endedTimestamp;

  private Integer createdByPersonId;
  private Integer endedByPersonId;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PublicNotice getPublicNotice() {
    return publicNotice;
  }

  public void setPublicNotice(PublicNotice publicNotice) {
    this.publicNotice = publicNotice;
  }

  public String getCoverLetterText() {
    return coverLetterText;
  }

  public void setCoverLetterText(String coverLetterText) {
    this.coverLetterText = coverLetterText;
  }

  public PublicNoticeRequestStatus getStatus() {
    return status;
  }

  public void setStatus(PublicNoticeRequestStatus status) {
    this.status = status;
  }

  public PublicNoticeRequestReason getReason() {
    return reason;
  }

  public void setReason(PublicNoticeRequestReason reason) {
    this.reason = reason;
  }

  public String getReasonDescription() {
    return reasonDescription;
  }

  public void setReasonDescription(String reasonDescription) {
    this.reasonDescription = reasonDescription;
  }

  public Boolean getRequestApproved() {
    return requestApproved;
  }

  public void setRequestApproved(Boolean requestApproved) {
    this.requestApproved = requestApproved;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public void setRejectionReason(String rejectionReason) {
    this.rejectionReason = rejectionReason;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Instant getSubmittedTimestamp() {
    return submittedTimestamp;
  }

  public void setSubmittedTimestamp(Instant submittedTimestamp) {
    this.submittedTimestamp = submittedTimestamp;
  }

  public Instant getEndedTimestamp() {
    return endedTimestamp;
  }

  public void setEndedTimestamp(Instant endedTimestamp) {
    this.endedTimestamp = endedTimestamp;
  }

  public Integer getCreatedByPersonId() {
    return createdByPersonId;
  }

  public void setCreatedByPersonId(Integer createdByPersonId) {
    this.createdByPersonId = createdByPersonId;
  }

  public Integer getEndedByPersonId() {
    return endedByPersonId;
  }

  public void setEndedByPersonId(Integer endedByPersonId) {
    this.endedByPersonId = endedByPersonId;
  }

}
