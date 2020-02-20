package uk.co.ogauthority.pwa.temp.model.form;

import uk.co.ogauthority.pwa.temp.model.locations.MedianLineSelection;

public class LocationForm {

  private MedianLineSelection medianLineSelection;
  private String medianLineAgreement;
  private Boolean likelySignificantImpact;
  private Integer emtSubmitByDay;
  private Integer emtSubmitByMonth;
  private Integer emtSubmitByYear;
  private String emtStatement;
  private boolean acceptEolRegulations;
  private boolean acceptEolRemoval;
  private boolean acceptRemovalProposal;
  private String decommissioningPlans;
  private String negotiatorName;
  private String negotiatorEmail;

  public MedianLineSelection getMedianLineSelection() {
    return medianLineSelection;
  }

  public void setMedianLineSelection(MedianLineSelection medianLineSelection) {
    this.medianLineSelection = medianLineSelection;
  }

  public String getMedianLineAgreement() {
    return medianLineAgreement;
  }

  public void setMedianLineAgreement(String medianLineAgreement) {
    this.medianLineAgreement = medianLineAgreement;
  }

  public Boolean getLikelySignificantImpact() {
    return likelySignificantImpact;
  }

  public void setLikelySignificantImpact(Boolean likelySignificantImpact) {
    this.likelySignificantImpact = likelySignificantImpact;
  }

  public Integer getEmtSubmitByDay() {
    return emtSubmitByDay;
  }

  public void setEmtSubmitByDay(Integer emtSubmitByDay) {
    this.emtSubmitByDay = emtSubmitByDay;
  }

  public Integer getEmtSubmitByMonth() {
    return emtSubmitByMonth;
  }

  public void setEmtSubmitByMonth(Integer emtSubmitByMonth) {
    this.emtSubmitByMonth = emtSubmitByMonth;
  }

  public Integer getEmtSubmitByYear() {
    return emtSubmitByYear;
  }

  public void setEmtSubmitByYear(Integer emtSubmitByYear) {
    this.emtSubmitByYear = emtSubmitByYear;
  }

  public String getEmtStatement() {
    return emtStatement;
  }

  public void setEmtStatement(String emtStatement) {
    this.emtStatement = emtStatement;
  }

  public boolean isAcceptEolRegulations() {
    return acceptEolRegulations;
  }

  public void setAcceptEolRegulations(boolean acceptEolRegulations) {
    this.acceptEolRegulations = acceptEolRegulations;
  }

  public boolean isAcceptEolRemoval() {
    return acceptEolRemoval;
  }

  public void setAcceptEolRemoval(boolean acceptEolRemoval) {
    this.acceptEolRemoval = acceptEolRemoval;
  }

  public boolean isAcceptRemovalProposal() {
    return acceptRemovalProposal;
  }

  public void setAcceptRemovalProposal(boolean acceptRemovalProposal) {
    this.acceptRemovalProposal = acceptRemovalProposal;
  }

  public String getDecommissioningPlans() {
    return decommissioningPlans;
  }

  public void setDecommissioningPlans(String decommissioningPlans) {
    this.decommissioningPlans = decommissioningPlans;
  }

  public String getNegotiatorName() {
    return negotiatorName;
  }

  public void setNegotiatorName(String negotiatorName) {
    this.negotiatorName = negotiatorName;
  }

  public String getNegotiatorEmail() {
    return negotiatorEmail;
  }

  public void setNegotiatorEmail(String negotiatorEmail) {
    this.negotiatorEmail = negotiatorEmail;
  }
}
