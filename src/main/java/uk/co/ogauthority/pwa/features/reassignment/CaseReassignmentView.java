package uk.co.ogauthority.pwa.features.reassignment;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.Immutable;
import uk.co.ogauthority.pwa.component.AddToListComponent;
import uk.co.ogauthority.pwa.util.DateUtils;

@Entity
@Table(name = "case_reassignment_view")
@Immutable
public class CaseReassignmentView implements AddToListComponent {
  @Id
  private Integer applicationId;
  private String padReference;

  private String padName;

  private String padStatus;

  private Instant inCaseOfficerReviewSince;
  private String assignedCaseOfficer;

  private Integer assignedCaseOfficerPersonId;

  public Integer getApplicationId() {
    return applicationId;
  }

  public String getId() {
    return String.valueOf(getApplicationId());
  }

  public void setApplicationId(Integer applicationId) {
    this.applicationId = applicationId;
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

  public String getName() {
    return padReference + " : " + padName;
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

  public Boolean isValid() {
    return true;
  }
}
