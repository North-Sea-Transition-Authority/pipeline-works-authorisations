package uk.co.ogauthority.pwa.model.entity.pwaapplications;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

  private Integer approvedByWuaId;

  private Instant approvedTimestamp;


  private Boolean isLinkedToField;

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

  public Integer getApprovedByWuaId() {
    return approvedByWuaId;
  }

  public void setApprovedByWuaId(Integer approvedByWuaId) {
    this.approvedByWuaId = approvedByWuaId;
  }

  public Instant getApprovedTimestamp() {
    return approvedTimestamp;
  }

  public void setApprovedTimestamp(Instant approvedTimestamp) {
    this.approvedTimestamp = approvedTimestamp;
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

  public void setStatusLastModifiedByWuaId(Integer statusLastModifiedByWuaId) {
    this.statusLastModifiedByWuaId = statusLastModifiedByWuaId;
  }
}
