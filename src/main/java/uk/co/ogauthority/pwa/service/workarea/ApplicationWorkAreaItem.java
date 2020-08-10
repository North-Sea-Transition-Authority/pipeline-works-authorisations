package uk.co.ogauthority.pwa.service.workarea;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

/**
 * An abstract class grouping common work area attributes linked to PWA applications.
 */
public abstract class ApplicationWorkAreaItem {

  private final int pwaApplicationId;

  private final String pwaApplicationReference;

  private final String masterPwaReference;

  private final PwaApplicationType applicationType;

  private final PwaApplicationStatus applicationStatus;

  private final Instant padStatusSetInstant;

  private final boolean tipFlag;

  private final String projectName;

  private final Instant proposedStartInstant;

  private final List<String> orderedFieldList;

  private final boolean submittedAsFastTrackFlag;

  private final Instant initialReviewApprovedInstant;

  private PersonId caseOfficerPersonId;
  private String caseOfficerName;

  private final String accessUrl;

  public ApplicationWorkAreaItem(ApplicationDetailSearchItem applicationDetailSearchItem,
                                 String accessUrl) {
    this.pwaApplicationId = applicationDetailSearchItem.getPwaApplicationId();
    this.pwaApplicationReference = applicationDetailSearchItem.getPadReference();
    this.masterPwaReference = applicationDetailSearchItem.getPwaReference();
    this.applicationType = applicationDetailSearchItem.getApplicationType();
    this.applicationStatus = applicationDetailSearchItem.getPadStatus();
    this.padStatusSetInstant = applicationDetailSearchItem.getPadStatusTimestamp();
    this.tipFlag = applicationDetailSearchItem.isTipFlag();
    this.projectName = applicationDetailSearchItem.getPadProjectName();
    this.proposedStartInstant = applicationDetailSearchItem.getPadProposedStart();
    this.orderedFieldList = applicationDetailSearchItem.getPadFields().stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());
    this.submittedAsFastTrackFlag = applicationDetailSearchItem.wasSubmittedAsFastTrack();
    this.initialReviewApprovedInstant = applicationDetailSearchItem.getPadInitialReviewApprovedTimestamp();
    this.accessUrl = accessUrl;

    if (applicationDetailSearchItem.getCaseOfficerPersonId() != null) {
      this.caseOfficerPersonId = new PersonId(applicationDetailSearchItem.getCaseOfficerPersonId());
      this.caseOfficerName = applicationDetailSearchItem.getCaseOfficerName();
    }

  }

  public int getPwaApplicationId() {
    return pwaApplicationId;
  }

  public String getApplicationReference() {
    return pwaApplicationReference;
  }

  public String getMasterPwaReference() {
    return masterPwaReference;
  }

  public String getApplicationTypeDisplay() {
    return applicationType.getDisplayName();
  }

  public String getApplicationStatusDisplay() {
    return applicationStatus.getDisplayName();
  }

  public String getFormattedStatusSetDatetime() {
    return Optional.ofNullable(this.padStatusSetInstant)
        .map(WorkAreaUtils.WORK_AREA_DATETIME_FORMAT::format)
        .orElse(null);
  }

  public boolean isTipFlag() {
    return tipFlag;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getProposedStartDateDisplay() {
    return Optional.ofNullable(this.proposedStartInstant)
        .map(WorkAreaUtils.WORK_AREA_DATE_FORMAT::format)
        .orElse(null);
  }

  public List<String> getOrderedFieldList() {
    return orderedFieldList;
  }

  public String getAccessUrl() {
    return accessUrl;
  }

  public boolean wasSubmittedAsFastTrack() {
    return submittedAsFastTrackFlag;
  }

  public boolean isFastTrackAccepted() {
    return submittedAsFastTrackFlag && initialReviewApprovedInstant != null;
  }

  public String getFastTrackLabelText() {

    if (!submittedAsFastTrackFlag) {
      return null;
    }

    return isFastTrackAccepted()
        ? ApplicationTask.FAST_TRACK.getDisplayName() + " accepted"
        : ApplicationTask.FAST_TRACK.getDisplayName();

  }

  public PersonId getCaseOfficerPersonId() {
    return caseOfficerPersonId;
  }

  public String getCaseOfficerName() {
    return caseOfficerName;
  }

}
