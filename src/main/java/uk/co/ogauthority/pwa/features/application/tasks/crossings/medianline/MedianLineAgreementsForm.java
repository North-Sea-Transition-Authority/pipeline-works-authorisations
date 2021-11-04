package uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline;

public class MedianLineAgreementsForm {

  private MedianLineStatus agreementStatus;

  private String negotiatorNameIfOngoing;
  private String negotiatorNameIfCompleted;

  private String negotiatorEmailIfOngoing;
  private String negotiatorEmailIfCompleted;

  public MedianLineStatus getAgreementStatus() {
    return agreementStatus;
  }

  public void setAgreementStatus(MedianLineStatus agreementStatus) {
    this.agreementStatus = agreementStatus;
  }

  public String getNegotiatorNameIfOngoing() {
    return negotiatorNameIfOngoing;
  }

  public void setNegotiatorNameIfOngoing(String negotiatorNameIfOngoing) {
    this.negotiatorNameIfOngoing = negotiatorNameIfOngoing;
  }

  public String getNegotiatorNameIfCompleted() {
    return negotiatorNameIfCompleted;
  }

  public void setNegotiatorNameIfCompleted(String negotiatorNameIfCompleted) {
    this.negotiatorNameIfCompleted = negotiatorNameIfCompleted;
  }

  public String getNegotiatorEmailIfOngoing() {
    return negotiatorEmailIfOngoing;
  }

  public void setNegotiatorEmailIfOngoing(String negotiatorEmailIfOngoing) {
    this.negotiatorEmailIfOngoing = negotiatorEmailIfOngoing;
  }

  public String getNegotiatorEmailIfCompleted() {
    return negotiatorEmailIfCompleted;
  }

  public void setNegotiatorEmailIfCompleted(String negotiatorEmailIfCompleted) {
    this.negotiatorEmailIfCompleted = negotiatorEmailIfCompleted;
  }
}
