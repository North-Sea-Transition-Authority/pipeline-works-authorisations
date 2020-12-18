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
  
}
