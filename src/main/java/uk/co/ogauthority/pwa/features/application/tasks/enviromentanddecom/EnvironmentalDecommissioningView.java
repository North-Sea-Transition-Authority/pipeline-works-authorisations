package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom;

import java.util.Set;

public class EnvironmentalDecommissioningView {

  
  private final Boolean transboundaryEffect;
  private final Boolean emtHasSubmittedPermits;
  private final String permitsSubmitted;
  private final Boolean emtHasOutstandingPermits;
  private final String permitsPendingSubmission;
  private final String emtSubmissionDate;

  private final Set<EnvironmentalCondition> environmentalConditions;
  private final Set<DecommissioningCondition> decommissioningConditions;


  public EnvironmentalDecommissioningView(Boolean transboundaryEffect, Boolean emtHasSubmittedPermits,
                                          String permitsSubmitted, Boolean emtHasOutstandingPermits,
                                          String permitsPendingSubmission,
                                          String emtSubmissionDate,
                                          Set<EnvironmentalCondition> environmentalConditions,
                                          Set<DecommissioningCondition> decommissioningConditions) {
    this.transboundaryEffect = transboundaryEffect;
    this.emtHasSubmittedPermits = emtHasSubmittedPermits;
    this.permitsSubmitted = permitsSubmitted;
    this.emtHasOutstandingPermits = emtHasOutstandingPermits;
    this.permitsPendingSubmission = permitsPendingSubmission;
    this.emtSubmissionDate = emtSubmissionDate;
    this.environmentalConditions = environmentalConditions;
    this.decommissioningConditions = decommissioningConditions;
  }

  public Boolean getTransboundaryEffect() {
    return transboundaryEffect;
  }

  public Boolean getEmtHasSubmittedPermits() {
    return emtHasSubmittedPermits;
  }

  public String getPermitsSubmitted() {
    return permitsSubmitted;
  }

  public Boolean getEmtHasOutstandingPermits() {
    return emtHasOutstandingPermits;
  }

  public String getPermitsPendingSubmission() {
    return permitsPendingSubmission;
  }

  public String getEmtSubmissionDate() {
    return emtSubmissionDate;
  }

  public Set<EnvironmentalCondition> getEnvironmentalConditions() {
    return environmentalConditions;
  }

  public Set<DecommissioningCondition> getDecommissioningConditions() {
    return decommissioningConditions;
  }
}
