package uk.co.ogauthority.pwa.features.webapp.devtools.testharness;

import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

public class GenerateApplicationForm {

  private PwaApplicationType applicationType;
  private Integer pipelineQuantity;
  private PwaApplicationStatus applicationStatus;
  private Integer assignedCaseOfficerId;
  private Integer applicantWuaId;

  private PwaResourceType resourceType;

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

  public Integer getApplicantWuaId() {
    return applicantWuaId;
  }

  public void setApplicantWuaId(Integer applicantWuaId) {
    this.applicantWuaId = applicantWuaId;
  }

  public PwaResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(PwaResourceType resourceType) {
    this.resourceType = resourceType;
  }
}
