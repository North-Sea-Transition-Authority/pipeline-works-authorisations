package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;

public class MedianLineAgreementView {

  private MedianLineStatus agreementStatus;
  private String negotiatorName;
  private String negotiatorEmail;

  public MedianLineAgreementView(MedianLineStatus agreementStatus, String negotiatorName, String negotiatorEmail) {
    this.agreementStatus = agreementStatus;
    this.negotiatorName = negotiatorName;
    this.negotiatorEmail = negotiatorEmail;
  }

  public MedianLineStatus getAgreementStatus() {
    return agreementStatus;
  }

  public String getNegotiatorName() {
    return negotiatorName;
  }

  public String getNegotiatorEmail() {
    return negotiatorEmail;
  }
}
