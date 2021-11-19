package uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview;

public class InitialReviewForm {

  private Integer caseOfficerPersonId;

  private InitialReviewPaymentDecision initialReviewPaymentDecision;

  private String paymentWaivedReason;

  public InitialReviewForm() {
  }

  public Integer getCaseOfficerPersonId() {
    return caseOfficerPersonId;
  }

  public void setCaseOfficerPersonId(Integer caseOfficerPersonId) {
    this.caseOfficerPersonId = caseOfficerPersonId;
  }

  public InitialReviewPaymentDecision getInitialReviewPaymentDecision() {
    return initialReviewPaymentDecision;
  }

  public void setInitialReviewPaymentDecision(
      InitialReviewPaymentDecision initialReviewPaymentDecision) {
    this.initialReviewPaymentDecision = initialReviewPaymentDecision;
  }

  public String getPaymentWaivedReason() {
    return paymentWaivedReason;
  }

  public void setPaymentWaivedReason(String paymentWaivedReason) {
    this.paymentWaivedReason = paymentWaivedReason;
  }
}
