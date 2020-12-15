package uk.co.ogauthority.pwa.model.entity.pwaapplications.search;

import java.time.Instant;
import java.util.List;
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

  boolean isOpenPublicNoticeFlag();

  boolean isTipVersionSatisfactoryFlag();



  void setPwaApplicationDetailId(int pwaApplicationDetailId);

  void setPwaApplicationId(int pwaApplicationId);

  void setPwaId(int pwaId);

  void setPwaDetailId(int pwaDetailId);

  void setPwaReference(String pwaReference);

  void setPadReference(String padReference);

  void setApplicationType(PwaApplicationType applicationType);

  void setPadFields(List<String> padFields);

  void setPwaHolderNameList(List<String> pwaHolderNameList);

  void setPadHolderNameList(List<String> padHolderNameList);

  void setPadProjectName(String padProjectName);

  void setPadProposedStart(Instant padProposedStart);

  void setPadStatus(PwaApplicationStatus padStatus);

  void setPadCreatedTimestamp(Instant padCreatedTimestamp);

  void setPadSubmittedTimestamp(Instant padSubmittedTimestamp);

  void setPadInitialReviewApprovedTimestamp(Instant padInitialReviewApprovedTimestamp);

  void setPadStatusTimestamp(Instant padStatusTimestamp);

  void setTipFlag(boolean tipFlag);

  void setVersionNo(Integer versionNo);

  void setSubmittedAsFastTrackFlag(boolean submittedAsFastTrackFlag);

  void setCaseOfficerPersonId(Integer caseOfficerPersonId);

  void setCaseOfficerName(String caseOfficerName);

  void setOpenUpdateRequestFlag(Boolean openUpdateRequestFlag);

  void setOpenConsultationRequestFlag(boolean openConsultationRequestFlag);

  void setOpenPublicNoticeFlag(boolean openPublicNoticeFlag);

  void setTipVersionSatisfactoryFlag(boolean tipVersionSatisfactoryFlag);
  
  
}
