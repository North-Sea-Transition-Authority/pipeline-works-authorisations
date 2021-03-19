package uk.co.ogauthority.pwa.model.entity.pwaapplications;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.converters.PersonIdConverter;
import uk.co.ogauthority.pwa.model.entity.converters.PipelinePropertyPhaseConverter;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.service.entitycopier.ParentEntity;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Entity
@Table(name = "pwa_application_details")
public final class PwaApplicationDetail implements ParentEntity {

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

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "submitted_by_person_id")
  private PersonId submittedByPersonId;

  private Instant submittedTimestamp;

  @Column(name = "init_review_approved_by_wua_id")
  private Integer initialReviewApprovedByWuaId;

  @Column(name = "init_review_approved_timestamp")
  private Instant initialReviewApprovedTimestamp;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  @Column(name = "confirmed_satisfactory_pers_id")
  private PersonId confirmedSatisfactoryByPersonId;

  @Column(name = "confirmed_satisfactory_ts")
  private Instant confirmedSatisfactoryTimestamp;

  private String confirmedSatisfactoryReason;

  private Boolean isLinkedToField;

  private String notLinkedDescription;

  private Boolean pipelinesCrossed;

  private Boolean cablesCrossed;

  private Boolean medianLineCrossed;

  private Boolean submittedAsFastTrackFlag;

  private Integer numOfHolders;

  @Convert(converter = PipelinePropertyPhaseConverter.class)
  private Set<PropertyPhase> pipelinePhaseProperties;

  private String otherPhaseDescription;

  private Boolean partnerLettersRequired;

  private Boolean partnerLettersConfirmed;

  private Boolean supplementaryDocumentsFlag;

  private Instant withdrawalTimestamp;

  private String withdrawalReason;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  private PersonId withdrawingPersonId;

  private Instant deletedTimestamp;

  @Basic
  @Convert(converter = PersonIdConverter.class)
  private PersonId deletingPersonId;


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

  @Override
  public Object getIdAsParent() {
    return this.getId();
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

  public PersonId getSubmittedByPersonId() {
    return submittedByPersonId;
  }

  public void setSubmittedByPersonId(PersonId submittedByPersonId) {
    this.submittedByPersonId = submittedByPersonId;
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

  public PersonId getConfirmedSatisfactoryByPersonId() {
    return confirmedSatisfactoryByPersonId;
  }

  public void setConfirmedSatisfactoryByPersonId(PersonId confirmedSatisfactoryByPersonId) {
    this.confirmedSatisfactoryByPersonId = confirmedSatisfactoryByPersonId;
  }

  public Instant getConfirmedSatisfactoryTimestamp() {
    return confirmedSatisfactoryTimestamp;
  }

  public void setConfirmedSatisfactoryTimestamp(Instant confirmedSatisfactoryTimestamp) {
    this.confirmedSatisfactoryTimestamp = confirmedSatisfactoryTimestamp;
  }

  public String getConfirmedSatisfactoryReason() {
    return confirmedSatisfactoryReason;
  }

  public void setConfirmedSatisfactoryReason(String confirmedSatisfactoryReason) {
    this.confirmedSatisfactoryReason = confirmedSatisfactoryReason;
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

  public MasterPwa getMasterPwa() {
    return this.pwaApplication.getMasterPwa();
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

  public boolean isFirstDraft() {
    return isFirstVersion() && PwaApplicationStatus.DRAFT.equals(this.status);
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

  public Set<PropertyPhase> getPipelinePhaseProperties() {
    return pipelinePhaseProperties;
  }

  public void setPipelinePhaseProperties(
      Set<PropertyPhase> pipelinePhaseProperties) {
    this.pipelinePhaseProperties = pipelinePhaseProperties;
  }

  public String getOtherPhaseDescription() {
    return otherPhaseDescription;
  }

  public void setOtherPhaseDescription(String otherPhaseDescription) {
    this.otherPhaseDescription = otherPhaseDescription;
  }

  public Boolean getPartnerLettersRequired() {
    return partnerLettersRequired;
  }

  public void setPartnerLettersRequired(Boolean partnerLettersRequired) {
    this.partnerLettersRequired = partnerLettersRequired;
  }

  public Boolean getPartnerLettersConfirmed() {
    return partnerLettersConfirmed;
  }

  public void setPartnerLettersConfirmed(Boolean partnerLettersConfirmed) {
    this.partnerLettersConfirmed = partnerLettersConfirmed;
  }

  public Boolean getSupplementaryDocumentsFlag() {
    return supplementaryDocumentsFlag;
  }

  public void setSupplementaryDocumentsFlag(Boolean supplementaryDocumentsFlag) {
    this.supplementaryDocumentsFlag = supplementaryDocumentsFlag;
  }

  public Instant getWithdrawalTimestamp() {
    return withdrawalTimestamp;
  }

  public void setWithdrawalTimestamp(Instant withdrawalTimestamp) {
    this.withdrawalTimestamp = withdrawalTimestamp;
  }

  public String getWithdrawalReason() {
    return withdrawalReason;
  }

  public void setWithdrawalReason(String withdrawalReason) {
    this.withdrawalReason = withdrawalReason;
  }

  public PersonId getWithdrawingPersonId() {
    return withdrawingPersonId;
  }

  public void setWithdrawingPersonId(PersonId withdrawingPersonId) {
    this.withdrawingPersonId = withdrawingPersonId;
  }

  public Instant getDeletedTimestamp() {
    return deletedTimestamp;
  }

  public void setDeletedTimestamp(Instant deletedTimestamp) {
    this.deletedTimestamp = deletedTimestamp;
  }

  public PersonId getDeletingPersonId() {
    return deletingPersonId;
  }

  public void setDeletingPersonId(PersonId deletingPersonId) {
    this.deletingPersonId = deletingPersonId;
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
        && Objects.equals(submittedByPersonId, that.submittedByPersonId)
        && Objects.equals(submittedTimestamp, that.submittedTimestamp)
        && Objects.equals(initialReviewApprovedByWuaId, that.initialReviewApprovedByWuaId)
        && Objects.equals(initialReviewApprovedTimestamp, that.initialReviewApprovedTimestamp)
        && Objects.equals(isLinkedToField, that.isLinkedToField)
        && Objects.equals(notLinkedDescription, that.notLinkedDescription)
        && Objects.equals(pipelinesCrossed, that.pipelinesCrossed)
        && Objects.equals(cablesCrossed, that.cablesCrossed)
        && Objects.equals(medianLineCrossed, that.medianLineCrossed)
        && Objects.equals(submittedAsFastTrackFlag, that.submittedAsFastTrackFlag)
        && Objects.equals(numOfHolders, that.numOfHolders)
        && Objects.equals(pipelinePhaseProperties, that.pipelinePhaseProperties)
        && Objects.equals(otherPhaseDescription, that.otherPhaseDescription)
        && Objects.equals(partnerLettersRequired, that.partnerLettersRequired)
        && Objects.equals(partnerLettersConfirmed, that.partnerLettersConfirmed)
        && Objects.equals(supplementaryDocumentsFlag, that.supplementaryDocumentsFlag)
        && Objects.equals(withdrawalTimestamp, that.withdrawalTimestamp)
        && Objects.equals(withdrawalReason, that.withdrawalReason)
        && Objects.equals(withdrawingPersonId, that.withdrawingPersonId)
        && Objects.equals(deletedTimestamp, that.deletedTimestamp)
        && Objects.equals(deletingPersonId, that.deletingPersonId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, pwaApplication, tipFlag, versionNo, status, statusLastModifiedTimestamp,
        statusLastModifiedByWuaId, createdByWuaId, createdTimestamp, submittedByPersonId, submittedTimestamp,
        initialReviewApprovedByWuaId, initialReviewApprovedTimestamp, isLinkedToField, notLinkedDescription,
        pipelinesCrossed, cablesCrossed, medianLineCrossed, submittedAsFastTrackFlag, numOfHolders,
        pipelinePhaseProperties, otherPhaseDescription, partnerLettersRequired, partnerLettersConfirmed,
        supplementaryDocumentsFlag, withdrawalTimestamp, withdrawalReason, withdrawingPersonId, deletedTimestamp, deletingPersonId);
  }
}
