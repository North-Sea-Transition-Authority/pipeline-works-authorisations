package uk.co.ogauthority.pwa.model.entity.pwaapplications.search;

import com.google.common.annotations.VisibleForTesting;
import java.time.Instant;
import java.util.List;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public interface ApplicationDetailItemView {

  int getPwaApplicationDetailId();

  int getPwaApplicationId();

  int getPwaId();

  int getPwaDetailId();

  String getPwaReference();

  String getPadReference();

  PwaApplicationType getApplicationType();

  List<String> getPadFields();

  List<String> getPwaHolderNameList();

  List<String> getPadHolderNameList();

  String getPadProjectName();

  Instant getPadProposedStart();

  PwaApplicationStatus getPadStatus();

  Instant getPadCreatedTimestamp();

  Instant getPadSubmittedTimestamp();

  Instant getPadInitialReviewApprovedTimestamp();

  Instant getPadStatusTimestamp();

  boolean isTipFlag();

  Integer getVersionNo();

  boolean isSubmittedAsFastTrackFlag();

  boolean wasSubmittedAsFastTrack();

  Integer getCaseOfficerPersonId();

  String getCaseOfficerName();

  boolean getOpenUpdateRequestFlag();

  boolean isOpenConsultationRequestFlag();

  PublicNoticeStatus getPublicNoticeStatus();

  boolean isTipVersionSatisfactoryFlag();

  boolean isOpenConsentReviewFlag();


  @VisibleForTesting
  void setPwaApplicationDetailId(int pwaApplicationDetailId);

  @VisibleForTesting
  void setPwaApplicationId(int pwaApplicationId);

  @VisibleForTesting
  void setPwaId(int pwaId);

  @VisibleForTesting
  void setPwaDetailId(int pwaDetailId);

  @VisibleForTesting
  void setPwaReference(String pwaReference);

  @VisibleForTesting
  void setPadReference(String padReference);

  @VisibleForTesting
  void setApplicationType(PwaApplicationType applicationType);

  @VisibleForTesting
  void setPadFields(List<String> padFields);

  @VisibleForTesting
  void setPwaHolderNameList(List<String> pwaHolderNameList);

  @VisibleForTesting
  void setPadHolderNameList(List<String> padHolderNameList);

  @VisibleForTesting
  void setPadProjectName(String padProjectName);

  @VisibleForTesting
  void setPadProposedStart(Instant padProposedStart);

  @VisibleForTesting
  void setPadStatus(PwaApplicationStatus padStatus);

  @VisibleForTesting
  void setPadCreatedTimestamp(Instant padCreatedTimestamp);

  @VisibleForTesting
  void setPadSubmittedTimestamp(Instant padSubmittedTimestamp);

  @VisibleForTesting
  void setPadInitialReviewApprovedTimestamp(Instant padInitialReviewApprovedTimestamp);

  @VisibleForTesting
  void setPadStatusTimestamp(Instant padStatusTimestamp);

  @VisibleForTesting
  void setTipFlag(boolean tipFlag);

  @VisibleForTesting
  void setVersionNo(Integer versionNo);

  @VisibleForTesting
  void setSubmittedAsFastTrackFlag(boolean submittedAsFastTrackFlag);

  @VisibleForTesting
  void setCaseOfficerPersonId(Integer caseOfficerPersonId);

  @VisibleForTesting
  void setCaseOfficerName(String caseOfficerName);

  @VisibleForTesting
  void setOpenUpdateRequestFlag(Boolean openUpdateRequestFlag);

  @VisibleForTesting
  void setOpenConsultationRequestFlag(boolean openConsultationRequestFlag);

  @VisibleForTesting
  void setPublicNoticeStatus(PublicNoticeStatus publicNoticeStatus);

  @VisibleForTesting
  void setTipVersionSatisfactoryFlag(boolean tipVersionSatisfactoryFlag);

  @VisibleForTesting
  void setOpenConsentReviewFlag(boolean openConsentReviewFlag);

}
