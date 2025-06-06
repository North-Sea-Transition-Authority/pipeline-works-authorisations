package uk.co.ogauthority.pwa.model.entity.pwaapplications.search;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.entity.converters.SemiColonSeparatedListConverter;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@Entity
@Table(name = "workarea_search_items")
@Immutable
public class WorkAreaApplicationDetailSearchItem implements ApplicationDetailItemView {

  @Id
  private int pwaApplicationId;

  private int pwaApplicationDetailId;

  private int pwaId;

  private int pwaDetailId;

  private String pwaReference;

  private String padReference;

  @Enumerated(EnumType.STRING)
  private PwaApplicationType applicationType;

  @Enumerated(EnumType.STRING)
  private PwaResourceType resourceType;

  @Convert(converter = SemiColonSeparatedListConverter.class)
  @Column(name = "pad_field_name_list")
  private List<String> padFields;

  // from the consented model
  @Convert(converter = SemiColonSeparatedListConverter.class)
  @Column(name = "pwa_holder_name_list")
  private List<String> pwaHolderNameList;

  // from the current app detail
  @Convert(converter = SemiColonSeparatedListConverter.class)
  @Column(name = "pad_holder_name_list")
  private List<String> padHolderNameList;

  private String padProjectName;

  @Column(name = "pad_proposed_start_timestamp")
  private Instant padProposedStart;

  @Enumerated(EnumType.STRING)
  private PwaApplicationStatus padStatus;

  private Instant padCreatedTimestamp;

  private Instant padSubmittedTimestamp;

  @Column(name = "pad_init_review_approved_ts")
  private Instant padInitialReviewApprovedTimestamp;

  private Instant padStatusTimestamp;

  private boolean tipFlag;

  private Integer versionNo;

  private boolean submittedAsFastTrackFlag;

  private Integer caseOfficerPersonId;

  private String caseOfficerName;

  private boolean openUpdateRequestFlag;

  @Column(name = "open_update_deadline_ts")
  private Instant openUpdateDeadlineTimestamp;

  @Column(name = "open_consultation_req_flag")
  private boolean openConsultationRequestFlag;

  @Enumerated(EnumType.STRING)
  private PublicNoticeStatus publicNoticeStatus;

  private boolean tipVersionSatisfactoryFlag;

  private boolean openConsentReviewFlag;

  // mapped as int not boolean to predictable number ordering e.g ascending or descending. e.g what would ordering a boolean actually mean?
  private Integer submittedFlag;

  @Override
  public int getPwaApplicationDetailId() {
    return pwaApplicationDetailId;
  }

  @Override
  public void setPwaApplicationDetailId(int pwaApplicationDetailId) {
    this.pwaApplicationDetailId = pwaApplicationDetailId;
  }

  @Override
  public int getPwaApplicationId() {
    return pwaApplicationId;
  }

  @Override
  public void setPwaApplicationId(int pwaApplicationId) {
    this.pwaApplicationId = pwaApplicationId;
  }

  @Override
  public int getPwaId() {
    return pwaId;
  }

  @Override
  public void setPwaId(int pwaId) {
    this.pwaId = pwaId;
  }

  @Override
  public int getPwaDetailId() {
    return pwaDetailId;
  }

  @Override
  public void setPwaDetailId(int pwaDetailId) {
    this.pwaDetailId = pwaDetailId;
  }

  @Override
  public String getPwaReference() {
    return pwaReference;
  }

  @Override
  public void setPwaReference(String pwaReference) {
    this.pwaReference = pwaReference;
  }

  @Override
  public String getPadReference() {
    return padReference;
  }

  @Override
  public void setPadReference(String padReference) {
    this.padReference = padReference;
  }

  @Override
  public PwaApplicationType getApplicationType() {
    return applicationType;
  }

  @Override
  public void setApplicationType(PwaApplicationType applicationType) {
    this.applicationType = applicationType;
  }

  @Override
  public PwaResourceType getResourceType() {
    return resourceType;
  }

  @Override
  public void setResourceType(PwaResourceType resourceType) {
    this.resourceType = resourceType;
  }

  @Override
  public PwaApplicationStatus getPadStatus() {
    return padStatus;
  }

  @Override
  public void setPadStatus(PwaApplicationStatus padStatus) {
    this.padStatus = padStatus;
  }

  @Override
  public Instant getPadCreatedTimestamp() {
    return padCreatedTimestamp;
  }

  @Override
  public void setPadCreatedTimestamp(Instant padCreatedTimestamp) {
    this.padCreatedTimestamp = padCreatedTimestamp;
  }

  @Override
  public Instant getPadSubmittedTimestamp() {
    return padSubmittedTimestamp;
  }

  @Override
  public void setPadSubmittedTimestamp(Instant padSubmittedTimestamp) {
    this.padSubmittedTimestamp = padSubmittedTimestamp;
  }

  @Override
  public Instant getPadInitialReviewApprovedTimestamp() {
    return padInitialReviewApprovedTimestamp;
  }

  @Override
  public void setPadInitialReviewApprovedTimestamp(Instant padApprovedTimestamp) {
    this.padInitialReviewApprovedTimestamp = padApprovedTimestamp;
  }

  @Override
  public boolean isTipFlag() {
    return tipFlag;
  }

  @Override
  public void setTipFlag(boolean tipFlag) {
    this.tipFlag = tipFlag;
  }

  @Override
  public Integer getVersionNo() {
    return versionNo;
  }

  @Override
  public void setVersionNo(Integer versionNo) {
    this.versionNo = versionNo;
  }

  @Override
  public List<String> getPadFields() {
    return padFields;
  }

  @Override
  public void setPadFields(List<String> padFields) {
    this.padFields = padFields;
  }

  @Override
  public String getPadProjectName() {
    return padProjectName;
  }

  @Override
  public void setPadProjectName(String padProjectName) {
    this.padProjectName = padProjectName;
  }

  @Override
  public Instant getPadProposedStart() {
    return padProposedStart;
  }

  @Override
  public void setPadProposedStart(Instant padProposedStart) {
    this.padProposedStart = padProposedStart;
  }

  @Override
  public Instant getPadStatusTimestamp() {
    return padStatusTimestamp;
  }

  @Override
  public void setPadStatusTimestamp(Instant padStatusTimestamp) {
    this.padStatusTimestamp = padStatusTimestamp;
  }

  @Override
  public boolean wasSubmittedAsFastTrack() {
    return submittedAsFastTrackFlag;
  }

  @Override
  public void setSubmittedAsFastTrackFlag(boolean fastTrackFlag) {
    this.submittedAsFastTrackFlag = fastTrackFlag;
  }

  @Override
  public Integer getCaseOfficerPersonId() {
    return caseOfficerPersonId;
  }

  @Override
  public void setCaseOfficerPersonId(Integer caseOfficerPersonId) {
    this.caseOfficerPersonId = caseOfficerPersonId;
  }

  @Override
  public String getCaseOfficerName() {
    return caseOfficerName;
  }

  @Override
  public void setCaseOfficerName(String caseOfficerName) {
    this.caseOfficerName = caseOfficerName;
  }

  @Override
  public List<String> getPwaHolderNameList() {
    return pwaHolderNameList;
  }

  @Override
  public void setPwaHolderNameList(List<String> pwaHolderNameList) {
    this.pwaHolderNameList = pwaHolderNameList;
  }

  @Override
  public List<String> getPadHolderNameList() {
    return padHolderNameList;
  }

  @Override
  public void setPadHolderNameList(List<String> padHolderNameList) {
    this.padHolderNameList = padHolderNameList;
  }

  @Override
  public boolean getOpenUpdateRequestFlag() {
    return openUpdateRequestFlag;
  }

  @Override
  public void setOpenUpdateRequestFlag(Boolean openUpdateRequestFlag) {
    this.openUpdateRequestFlag = openUpdateRequestFlag;
  }

  @Override
  public Instant getOpenUpdateDeadlineTimestamp() {
    return openUpdateDeadlineTimestamp;
  }

  @Override
  public void setOpenUpdateDeadlineTimestamp(Instant deadlineTimestamp) {
    this.openUpdateDeadlineTimestamp = deadlineTimestamp;
  }

  @Override
  public boolean isOpenConsultationRequestFlag() {
    return openConsultationRequestFlag;
  }

  @Override
  public void setOpenConsultationRequestFlag(boolean openConsultationRequestFlag) {
    this.openConsultationRequestFlag = openConsultationRequestFlag;
  }

  @Override
  public PublicNoticeStatus getPublicNoticeStatus() {
    return publicNoticeStatus;
  }

  @Override
  public void setPublicNoticeStatus(PublicNoticeStatus publicNoticeStatus) {
    this.publicNoticeStatus = publicNoticeStatus;
  }

  @Override
  public boolean isSubmittedAsFastTrackFlag() {
    return submittedAsFastTrackFlag;
  }

  @Override
  public boolean isTipVersionSatisfactoryFlag() {
    return tipVersionSatisfactoryFlag;
  }

  @Override
  public void setTipVersionSatisfactoryFlag(boolean tipVersionSatisfactoryFlag) {
    this.tipVersionSatisfactoryFlag = tipVersionSatisfactoryFlag;
  }

  @Override
  public boolean isOpenConsentReviewFlag() {
    return openConsentReviewFlag;
  }

  @Override
  public void setOpenConsentReviewFlag(boolean openConsentReviewFlag) {
    this.openConsentReviewFlag = openConsentReviewFlag;
  }

  public int getSubmittedFlag() {
    return submittedFlag;
  }

  public void setSubmittedFlag(int submittedFlag) {
    this.submittedFlag = submittedFlag;
  }
}
