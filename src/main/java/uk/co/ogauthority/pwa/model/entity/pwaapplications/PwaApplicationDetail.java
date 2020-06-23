package uk.co.ogauthority.pwa.model.entity.pwaapplications;

import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.ColumnDefault;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Entity(name = "pwa_application_details")
public class PwaApplicationDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "pwa_application_id")
  private PwaApplication pwaApplication;

  private boolean tipFlag;

  private Integer versionNo;

  @Enumerated(EnumType.STRING)
  private PwaApplicationStatus status;

  private Instant statusLastModifiedTimestamp;

  private Integer statusLastModifiedByWuaId;

  private Integer createdByWuaId;

  private Instant createdTimestamp;

  private Integer submittedByWuaId;

  private Instant submittedTimestamp;

  @Column(name = "init_review_approved_by_wua_id")
  private Integer initialReviewApprovedByWuaId;

  @Column(name = "init_review_approved_timestamp")
  private Instant initialReviewApprovedTimestamp;

  private Boolean isLinkedToField;

  private String notLinkedDescription;

  private Boolean pipelinesCrossed;

  private Boolean cablesCrossed;

  private Boolean medianLineCrossed;

  private Boolean submittedAsFastTrackFlag;

  private Integer numOfHolders;


  public PwaApplicationDetail() {
  }

  public PwaApplicationDetail(PwaApplication pwaApplication,
                              Integer versionNo,
                              Integer createdByWuaId,
                              Instant createdTimestamp) {
    this.pwaApplication = pwaApplication;
    this.tipFlag = true;
    this.status = PwaApplicationStatus.DRAFT;
    this.versionNo = versionNo;
    this.createdByWuaId = createdByWuaId;
    this.createdTimestamp = createdTimestamp;
    this.statusLastModifiedByWuaId = createdByWuaId;
    this.statusLastModifiedTimestamp = createdTimestamp;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public void setPwaApplication(PwaApplication pwaApplication) {
    this.pwaApplication = pwaApplication;
  }

  public boolean isTipFlag() {
    return tipFlag;
  }

  public void setTipFlag(boolean tipFlag) {
    this.tipFlag = tipFlag;
  }

  public Integer getVersionNo() {
    return versionNo;
  }

  public void setVersionNo(Integer versionNo) {
    this.versionNo = versionNo;
  }

  public PwaApplicationStatus getStatus() {
    return status;
  }

  public void setStatus(PwaApplicationStatus status) {
    this.status = status;
  }

  public Integer getCreatedByWuaId() {
    return createdByWuaId;
  }

  public void setCreatedByWuaId(Integer createdByWuaId) {
    this.createdByWuaId = createdByWuaId;
  }

  public Instant getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(Instant createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public Integer getSubmittedByWuaId() {
    return submittedByWuaId;
  }

  public void setSubmittedByWuaId(Integer submittedByWuaId) {
    this.submittedByWuaId = submittedByWuaId;
  }

  public Instant getSubmittedTimestamp() {
    return submittedTimestamp;
  }

  public void setSubmittedTimestamp(Instant submittedTimestamp) {
    this.submittedTimestamp = submittedTimestamp;
  }

  public Integer getInitialReviewApprovedByWuaId() {
    return initialReviewApprovedByWuaId;
  }

  public void setInitialReviewApprovedByWuaId(Integer approvedByWuaId) {
    this.initialReviewApprovedByWuaId = approvedByWuaId;
  }

  public Instant getInitialReviewApprovedTimestamp() {
    return initialReviewApprovedTimestamp;
  }

  public void setInitialReviewApprovedTimestamp(Instant approvedTimestamp) {
    this.initialReviewApprovedTimestamp = approvedTimestamp;
  }

  public Boolean getLinkedToField() {
    return isLinkedToField;
  }

  public void setLinkedToField(Boolean linkedToField) {
    isLinkedToField = linkedToField;
  }

  public Integer getMasterPwaApplicationId() {
    return this.pwaApplication.getId();
  }

  public String getPwaApplicationRef() {
    return this.pwaApplication.getAppReference();
  }

  public PwaApplicationType getPwaApplicationType() {
    return this.pwaApplication.getApplicationType();
  }

  public Instant getStatusLastModifiedTimestamp() {
    return statusLastModifiedTimestamp;
  }

  public void setStatusLastModifiedTimestamp(Instant statusLastModifiedTimestamp) {
    this.statusLastModifiedTimestamp = statusLastModifiedTimestamp;
  }

  public boolean isFirstVersion() {
    return this.versionNo == 1;
  }

  public Integer getStatusLastModifiedByWuaId() {
    return statusLastModifiedByWuaId;
  }

  public String getNotLinkedDescription() {
    return notLinkedDescription;
  }

  public void setNotLinkedDescription(String notLinkedDescription) {
    this.notLinkedDescription = notLinkedDescription;
  }

  public void setStatusLastModifiedByWuaId(Integer statusLastModifiedByWuaId) {
    this.statusLastModifiedByWuaId = statusLastModifiedByWuaId;
  }

  public Boolean getPipelinesCrossed() {
    return pipelinesCrossed;
  }

  public void setPipelinesCrossed(Boolean pipelinesCrossed) {
    this.pipelinesCrossed = pipelinesCrossed;
  }

  public Boolean getCablesCrossed() {
    return cablesCrossed;
  }

  public void setCablesCrossed(Boolean cablesCrossed) {
    this.cablesCrossed = cablesCrossed;
  }

  public Boolean getMedianLineCrossed() {
    return medianLineCrossed;
  }

  public void setMedianLineCrossed(Boolean medianLineCrossed) {
    this.medianLineCrossed = medianLineCrossed;
  }

  public Boolean getSubmittedAsFastTrackFlag() {
    return submittedAsFastTrackFlag;
  }

  public void setSubmittedAsFastTrackFlag(Boolean fastTrackFlag) {
    this.submittedAsFastTrackFlag = fastTrackFlag;
  }

  public Integer getNumOfHolders() {
    return numOfHolders;
  }

  public void setNumOfHolders(Integer numOfHolders) {
    this.numOfHolders = numOfHolders;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PwaApplicationDetail that = (PwaApplicationDetail) o;
    return tipFlag == that.tipFlag
        && Objects.equals(id, that.id)
        && Objects.equals(pwaApplication, that.pwaApplication)
        && Objects.equals(versionNo, that.versionNo)
        && status == that.status
        && Objects.equals(statusLastModifiedTimestamp, that.statusLastModifiedTimestamp)
        && Objects.equals(statusLastModifiedByWuaId, that.statusLastModifiedByWuaId)
        && Objects.equals(createdByWuaId, that.createdByWuaId)
        && Objects.equals(createdTimestamp, that.createdTimestamp)
        && Objects.equals(submittedByWuaId, that.submittedByWuaId)
        && Objects.equals(submittedTimestamp, that.submittedTimestamp)
        && Objects.equals(initialReviewApprovedByWuaId, that.initialReviewApprovedByWuaId)
        && Objects.equals(initialReviewApprovedTimestamp, that.initialReviewApprovedTimestamp)
        && Objects.equals(isLinkedToField, that.isLinkedToField)
        && Objects.equals(notLinkedDescription, that.notLinkedDescription)
        && Objects.equals(pipelinesCrossed, that.pipelinesCrossed)
        && Objects.equals(cablesCrossed, that.cablesCrossed)
        && Objects.equals(medianLineCrossed, that.medianLineCrossed)
        && Objects.equals(submittedAsFastTrackFlag, that.submittedAsFastTrackFlag)
        && Objects.equals(numOfHolders, that.numOfHolders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplication, tipFlag, versionNo, status, statusLastModifiedTimestamp,
        statusLastModifiedByWuaId, createdByWuaId, createdTimestamp, submittedByWuaId, submittedTimestamp,
        initialReviewApprovedByWuaId, initialReviewApprovedTimestamp, isLinkedToField, notLinkedDescription,
        pipelinesCrossed, cablesCrossed, medianLineCrossed, submittedAsFastTrackFlag, numOfHolders);
  }
}
