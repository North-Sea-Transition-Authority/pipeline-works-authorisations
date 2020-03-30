package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;

public class MedianLineAgreementsForm {

  private MedianLineStatus agreementStatus;
  private String negotiatorName;
  private String negotiatorEmail;

  public MedianLineStatus getAgreementStatus() {
    return agreementStatus;
  }

  public void setAgreementStatus(MedianLineStatus agreementStatus) {
    this.agreementStatus = agreementStatus;
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
