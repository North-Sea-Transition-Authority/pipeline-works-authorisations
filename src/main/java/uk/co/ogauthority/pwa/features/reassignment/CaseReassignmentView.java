package uk.co.ogauthority.pwa.features.reassignment;

import java.time.Instant;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.util.DateUtils;

@Entity
@Table(name = "case_reassignment_view")
@Immutable
public class CaseReassignmentView {
  @Id
  private Integer padId;
  private String padReference;

  private String padName;

  private String padStatus;

  private Instant inCaseOfficerReviewSince;
  private String assignedCaseOfficer;

  private Integer assignedCaseOfficerPersonId;

  public Integer getPadId() {
    return padId;
  }

  public void setPadId(Integer padId) {
    this.padId = padId;
  }

  public String getPadReference() {
    return padReference;
  }

  public void setPadReference(String padReference) {
    this.padReference = padReference;
  }

  public String getPadName() {
    return padName;
  }

  public void setPadName(String padName) {
    this.padName = padName;
  }

  public String getPadStatus() {
    return padStatus;
  }

  public void setPadStatus(String padStatus) {
    this.padStatus = padStatus;
  }

  public Instant getInCaseOfficerReviewSince() {
    return inCaseOfficerReviewSince;
  }

  public String getInCaseOfficerReviewSinceFormatted() {
    return DateUtils.formatDate(inCaseOfficerReviewSince);
  }

  public void setInCaseOfficerReviewSince(Instant inCaseOfficerReviewSince) {
    this.inCaseOfficerReviewSince = inCaseOfficerReviewSince;
  }

  public String getAssignedCaseOfficer() {
    return assignedCaseOfficer;
  }

  public void setAssignedCaseOfficer(String assignedCaseOfficer) {
    this.assignedCaseOfficer = assignedCaseOfficer;
  }

  public Integer getAssignedCaseOfficerPersonId() {
    return assignedCaseOfficerPersonId;
  }

  public void setAssignedCaseOfficerPersonId(Integer assignedCaseOfficerPersonId) {
    this.assignedCaseOfficerPersonId = assignedCaseOfficerPersonId;
  }
}
