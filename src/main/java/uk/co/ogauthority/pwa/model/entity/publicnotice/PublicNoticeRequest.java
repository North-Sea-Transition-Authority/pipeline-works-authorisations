package uk.co.ogauthority.pwa.model.entity.publicnotice;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
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

  private Instant createdTimestamp;

  private Instant responseTimestamp;

  private Integer createdByPersonId;
  private Integer responderPersonId;


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

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public Instant getResponseTimestamp() {
    return responseTimestamp;
  }

  public void setResponseTimestamp(Instant responseTimestamp) {
    this.responseTimestamp = responseTimestamp;
  }

  public Integer getCreatedByPersonId() {
    return createdByPersonId;
  }

  public void setCreatedByPersonId(Integer createdByPersonId) {
    this.createdByPersonId = createdByPersonId;
  }

  public Integer getResponderPersonId() {
    return responderPersonId;
  }

  public void setResponderPersonId(Integer responderPersonId) {
    this.responderPersonId = responderPersonId;
  }

}
