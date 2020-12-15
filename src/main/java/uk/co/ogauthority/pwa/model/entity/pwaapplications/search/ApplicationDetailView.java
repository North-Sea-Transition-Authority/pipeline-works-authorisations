package uk.co.ogauthority.pwa.model.entity.pwaapplications.search;

import java.time.Instant;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.model.entity.converters.SemiColonSeparatedListConverter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Entity
@Table(name = "application_detail_view")
@Immutable
public class ApplicationDetailView implements ApplicationDetailItemView {

  @Id
  private int pwaApplicationDetailId;

  private int pwaApplicationId;

  private int pwaId;

  private int pwaDetailId;

  private String pwaReference;

  private String padReference;

  @Enumerated(EnumType.STRING)
  private PwaApplicationType applicationType;

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

  @Column(name = "open_consultation_req_flag")
  private boolean openConsultationRequestFlag;

  private boolean openPublicNoticeFlag;

  private boolean tipVersionSatisfactoryFlag;

  public int getPwaApplicationDetailId() {
    return pwaApplicationDetailId;
  }

  public void setPwaApplicationDetailId(int pwaApplicationDetailId) {
    this.pwaApplicationDetailId = pwaApplicationDetailId;
  }

  public int getPwaApplicationId() {
    return pwaApplicationId;
  }

  public void setPwaApplicationId(int pwaApplicationId) {
    this.pwaApplicationId = pwaApplicationId;
  }

  public int getPwaId() {
    return pwaId;
  }

  public void setPwaId(int pwaId) {
    this.pwaId = pwaId;
  }

  public int getPwaDetailId() {
    return pwaDetailId;
  }

  public void setPwaDetailId(int pwaDetailId) {
    this.pwaDetailId = pwaDetailId;
  }

  public String getPwaReference() {
    return pwaReference;
  }

  public void setPwaReference(String pwaReference) {
    this.pwaReference = pwaReference;
  }

  public String getPadReference() {
    return padReference;
  }

  public void setPadReference(String padReference) {
    this.padReference = padReference;
  }

  public PwaApplicationType getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(PwaApplicationType applicationType) {
    this.applicationType = applicationType;
  }

  public PwaApplicationStatus getPadStatus() {
    return padStatus;
  }

  public void setPadStatus(PwaApplicationStatus padStatus) {
    this.padStatus = padStatus;
  }

  public Instant getPadCreatedTimestamp() {
    return padCreatedTimestamp;
  }

  public void setPadCreatedTimestamp(Instant padCreatedTimestamp) {
    this.padCreatedTimestamp = padCreatedTimestamp;
  }

  public Instant getPadSubmittedTimestamp() {
    return padSubmittedTimestamp;
  }

  public void setPadSubmittedTimestamp(Instant padSubmittedTimestamp) {
    this.padSubmittedTimestamp = padSubmittedTimestamp;
  }

  public Instant getPadInitialReviewApprovedTimestamp() {
    return padInitialReviewApprovedTimestamp;
  }

  public void setPadInitialReviewApprovedTimestamp(Instant padApprovedTimestamp) {
    this.padInitialReviewApprovedTimestamp = padApprovedTimestamp;
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

  public List<String> getPadFields() {
    return padFields;
  }

  public void setPadFields(List<String> padFields) {
    this.padFields = padFields;
  }

  public String getPadProjectName() {
    return padProjectName;
  }

  public void setPadProjectName(String padProjectName) {
    this.padProjectName = padProjectName;
  }

  public Instant getPadProposedStart() {
    return padProposedStart;
  }

  public void setPadProposedStart(Instant padProposedStart) {
    this.padProposedStart = padProposedStart;
  }

  public Instant getPadStatusTimestamp() {
    return padStatusTimestamp;
  }

  public void setPadStatusTimestamp(Instant padStatusTimestamp) {
    this.padStatusTimestamp = padStatusTimestamp;
  }

  public boolean wasSubmittedAsFastTrack() {
    return submittedAsFastTrackFlag;
  }

  public void setSubmittedAsFastTrackFlag(boolean fastTrackFlag) {
    this.submittedAsFastTrackFlag = fastTrackFlag;
  }

  public Integer getCaseOfficerPersonId() {
    return caseOfficerPersonId;
  }

  public void setCaseOfficerPersonId(Integer caseOfficerPersonId) {
    this.caseOfficerPersonId = caseOfficerPersonId;
  }

  public String getCaseOfficerName() {
    return caseOfficerName;
  }

  public void setCaseOfficerName(String caseOfficerName) {
    this.caseOfficerName = caseOfficerName;
  }

  public List<String> getPwaHolderNameList() {
    return pwaHolderNameList;
  }

  public void setPwaHolderNameList(List<String> pwaHolderNameList) {
    this.pwaHolderNameList = pwaHolderNameList;
  }

  public List<String> getPadHolderNameList() {
    return padHolderNameList;
  }

  public void setPadHolderNameList(List<String> padHolderNameList) {
    this.padHolderNameList = padHolderNameList;
  }

  public boolean getOpenUpdateRequestFlag() {
    return openUpdateRequestFlag;
  }

  public void setOpenUpdateRequestFlag(Boolean openUpdateRequestFlag) {
    this.openUpdateRequestFlag = openUpdateRequestFlag;
  }

  public boolean isOpenConsultationRequestFlag() {
    return openConsultationRequestFlag;
  }

  public void setOpenConsultationRequestFlag(boolean openConsultationRequestFlag) {
    this.openConsultationRequestFlag = openConsultationRequestFlag;
  }

  public boolean isOpenPublicNoticeFlag() {
    return openPublicNoticeFlag;
  }

  public void setOpenPublicNoticeFlag(boolean openPublicNoticeFlag) {
    this.openPublicNoticeFlag = openPublicNoticeFlag;
  }

  public boolean isSubmittedAsFastTrackFlag() {
    return submittedAsFastTrackFlag;
  }

  public boolean isTipVersionSatisfactoryFlag() {
    return tipVersionSatisfactoryFlag;
  }

  public void setTipVersionSatisfactoryFlag(boolean tipVersionSatisfactoryFlag) {
    this.tipVersionSatisfactoryFlag = tipVersionSatisfactoryFlag;
  }
}
