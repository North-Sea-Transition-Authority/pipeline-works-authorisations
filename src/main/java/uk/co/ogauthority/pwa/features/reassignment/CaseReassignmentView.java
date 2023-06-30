package uk.co.ogauthority.pwa.features.reassignment;

import java.time.Instant;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.util.DateUtils;

public class CaseReassignmentView {
  private Integer padId;
  private String padReference;
  private String assignedCaseOfficer;
  private Instant inCaseOfficerReviewSince;

  public CaseReassignmentView(WorkAreaApplicationDetailSearchItem item) {
    this.padId = item.getPwaApplicationId();
    this.padReference = item.getPadReference();
    this.assignedCaseOfficer = item.getCaseOfficerName();
    this.inCaseOfficerReviewSince = item.getPadStatusTimestamp();
  }

  public Integer getPadId() {
    return padId;
  }

  public String getPadReference() {
    return padReference;
  }

  public String getAssignedCaseOfficer() {
    return assignedCaseOfficer;
  }

  public Instant getInCaseOfficerReviewSince() {
    return inCaseOfficerReviewSince;
  }

  public String getInCaseOfficerReviewSinceFormatted() {
    return DateUtils.formatDate(inCaseOfficerReviewSince);
  }
}
