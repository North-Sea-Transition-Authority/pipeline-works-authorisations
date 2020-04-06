package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import java.util.Set;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;

public class EnvironmentalDecommissioningForm {

  public interface Full {
  }

  public interface Partial {
  }

  @NotNull(message = "Select yes if the development has significant trans-boundary effects", groups = Full.class)
  private Boolean transboundaryEffect;

  @NotNull(message = "Select yes if any relevant environmental permits have been submitted to BEIS", groups = Full.class)
  private Boolean emtHasSubmittedPermits;

  @Length(max = 4000, message = "Permits submitted to BEIS must be 4000 characters or less", groups = {Full.class, Partial.class})
  private String permitsSubmitted;

  @NotNull(message = "Select yes if you have any relevant permits that haven't been submitted to BEIS", groups = Full.class)
  private Boolean emtHasOutstandingPermits;

  @Length(max = 4000, message = "Permits pending BEIS submission must be 4000 characters or less", groups = {Full.class, Partial.class})
  private String permitsPendingSubmission;

  private Integer emtSubmissionDay;

  private Integer emtSubmissionMonth;

  private Integer emtSubmissionYear;

  private Set<EnvironmentalCondition> environmentalConditions;

  @NotNull(message = "You must provide your decommissioning plans", groups = Full.class)
  @Length(max = 4000, message = "Must be 4000 characters or less", groups = {Full.class, Partial.class})
  private String decommissioningPlans;

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

  public String getDecommissioningPlans() {
    return decommissioningPlans;
  }

  public void setDecommissioningPlans(String decommissioningPlans) {
    this.decommissioningPlans = decommissioningPlans;
  }

  public Set<DecommissioningCondition> getDecommissioningConditions() {
    return decommissioningConditions;
  }

  public void setDecommissioningConditions(
      Set<DecommissioningCondition> decommissioningConditions) {
    this.decommissioningConditions = decommissioningConditions;
  }
}
