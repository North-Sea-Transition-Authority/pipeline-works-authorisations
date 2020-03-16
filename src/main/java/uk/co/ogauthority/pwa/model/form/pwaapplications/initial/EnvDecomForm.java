package uk.co.ogauthority.pwa.model.form.pwaapplications.initial;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public class EnvDecomForm {

  @NotNull(message = "You must select one")
  private Boolean transboundaryEffect;

  @NotNull(message = "You must select one")
  private Boolean emtHasSubmittedPermits;

  @Length(max = 4000, message = "Must be 4000 characters or less")
  private String permitsSubmitted;

  @NotNull(message = "You must select one")
  private Boolean emtHasOutstandingPermits;

  @Length(max = 4000, message = "Must be 4000 characters or less")
  private String permitsPendingSubmission;

  private Integer emtSubmissionDay;

  private Integer emtSubmissionMonth;

  private Integer emtSubmissionYear;

  @NotNull(message = "You must agree to this condition")
  private Boolean dischargeFundsAvailable;

  @NotNull(message = "You must agree to this condition")
  private Boolean acceptsOpolLiability;

  @NotNull(message = "You must provide decommissioning plans")
  @Length(max = 4000, message = "Must be 4000 characters or less")
  private String decommissioningPlans;

  @NotNull(message = "You must agree to this condition")
  private Boolean acceptsEolRegulations;

  @NotNull(message = "You must agree to this condition")
  private Boolean acceptsEolRemoval;

  @NotNull(message = "You must agree to this condition")
  private Boolean acceptsRemovalProposal;

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

  public Boolean getDischargeFundsAvailable() {
    return dischargeFundsAvailable;
  }

  public void setDischargeFundsAvailable(Boolean dischargeFundsAvailable) {
    this.dischargeFundsAvailable = dischargeFundsAvailable;
  }

  public Boolean getAcceptsOpolLiability() {
    return acceptsOpolLiability;
  }

  public void setAcceptsOpolLiability(Boolean acceptsOpolLiability) {
    this.acceptsOpolLiability = acceptsOpolLiability;
  }

  public String getDecommissioningPlans() {
    return decommissioningPlans;
  }

  public void setDecommissioningPlans(String decommissioningPlans) {
    this.decommissioningPlans = decommissioningPlans;
  }

  public Boolean getAcceptsEolRegulations() {
    return acceptsEolRegulations;
  }

  public void setAcceptsEolRegulations(Boolean acceptsEolRegulations) {
    this.acceptsEolRegulations = acceptsEolRegulations;
  }

  public Boolean getAcceptsEolRemoval() {
    return acceptsEolRemoval;
  }

  public void setAcceptsEolRemoval(Boolean acceptsEolRemoval) {
    this.acceptsEolRemoval = acceptsEolRemoval;
  }

  public Boolean getAcceptsRemovalProposal() {
    return acceptsRemovalProposal;
  }

  public void setAcceptsRemovalProposal(Boolean acceptsRemovalProposal) {
    this.acceptsRemovalProposal = acceptsRemovalProposal;
  }
}
