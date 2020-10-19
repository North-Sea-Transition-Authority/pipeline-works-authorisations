package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;

public class EnvironmentalDecommissioningForm {

  private Boolean transboundaryEffect;

  private Boolean emtHasSubmittedPermits;

  private String permitsSubmitted;

  private Boolean emtHasOutstandingPermits;

  private String permitsPendingSubmission;

  private Integer emtSubmissionDay;

  private Integer emtSubmissionMonth;

  private Integer emtSubmissionYear;

  private Set<EnvironmentalCondition> environmentalConditions;

  private Set<DecommissioningCondition> decommissioningConditions;

  public Boolean getTransboundaryEffect() {
    return transboundaryEffect;
  }

  public void setTransboundaryEffect(Boolean transboundaryEffect) {
    this.transboundaryEffect = transboundaryEffect;
  }

  public Boolean getEmtHasSubmittedPermits() {
    return emtHasSubmittedPermits;
  }

  public void setEmtHasSubmittedPermits(Boolean emtHasSubmittedPermits) {
    this.emtHasSubmittedPermits = emtHasSubmittedPermits;
  }

  public String getPermitsSubmitted() {
    return permitsSubmitted;
  }

  public void setPermitsSubmitted(String permitsSubmitted) {
    this.permitsSubmitted = permitsSubmitted;
  }

  public Boolean getEmtHasOutstandingPermits() {
    return emtHasOutstandingPermits;
  }

  public void setEmtHasOutstandingPermits(Boolean emtHasOutstandingPermits) {
    this.emtHasOutstandingPermits = emtHasOutstandingPermits;
  }

  public String getPermitsPendingSubmission() {
    return permitsPendingSubmission;
  }

  public void setPermitsPendingSubmission(String permitsPendingSubmission) {
    this.permitsPendingSubmission = permitsPendingSubmission;
  }

  public Integer getEmtSubmissionDay() {
    return emtSubmissionDay;
  }

  public void setEmtSubmissionDay(Integer emtSubmissionDay) {
    this.emtSubmissionDay = emtSubmissionDay;
  }

  public Integer getEmtSubmissionMonth() {
    return emtSubmissionMonth;
  }

  public void setEmtSubmissionMonth(Integer emtSubmissionMonth) {
    this.emtSubmissionMonth = emtSubmissionMonth;
  }

  public Integer getEmtSubmissionYear() {
    return emtSubmissionYear;
  }

  public void setEmtSubmissionYear(Integer emtSubmissionYear) {
    this.emtSubmissionYear = emtSubmissionYear;
  }

  public Set<EnvironmentalCondition> getEnvironmentalConditions() {
    return environmentalConditions;
  }

  public void setEnvironmentalConditions(
      Set<EnvironmentalCondition> environmentalConditions) {
    this.environmentalConditions = environmentalConditions;
  }

  public Set<DecommissioningCondition> getDecommissioningConditions() {
    return decommissioningConditions;
  }

  public void setDecommissioningConditions(
      Set<DecommissioningCondition> decommissioningConditions) {
    this.decommissioningConditions = decommissioningConditions;
  }
}
