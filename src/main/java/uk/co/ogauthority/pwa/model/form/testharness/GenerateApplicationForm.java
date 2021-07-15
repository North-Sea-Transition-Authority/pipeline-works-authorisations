package uk.co.ogauthority.pwa.model.form.testharness;

import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

public class GenerateApplicationForm {

  private PwaApplicationType applicationType;
  private Integer pipelineQuantity;
  private PwaApplicationStatus applicationStatus;
  private Integer assignedCaseOfficerId;
  private Integer applicantPersonId;


  public PwaApplicationType getApplicationType() {
    return applicationType;
  }

  public void setApplicationType(PwaApplicationType applicationType) {
    this.applicationType = applicationType;
  }

  public Integer getPipelineQuantity() {
    return pipelineQuantity;
  }

  public void setPipelineQuantity(Integer pipelineQuantity) {
    this.pipelineQuantity = pipelineQuantity;
  }

  public PwaApplicationStatus getApplicationStatus() {
    return applicationStatus;
  }

  public void setApplicationStatus(PwaApplicationStatus applicationStatus) {
    this.applicationStatus = applicationStatus;
  }

  public Integer getAssignedCaseOfficerId() {
    return assignedCaseOfficerId;
  }

  public void setAssignedCaseOfficerId(Integer assignedCaseOfficerId) {
    this.assignedCaseOfficerId = assignedCaseOfficerId;
  }

  public Integer getApplicantPersonId() {
    return applicantPersonId;
  }

  public void setApplicantPersonId(Integer applicantPersonId) {
    this.applicantPersonId = applicantPersonId;
  }
}
