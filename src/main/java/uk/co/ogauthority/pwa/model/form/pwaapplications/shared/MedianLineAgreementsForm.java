package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;

public class MedianLineAgreementsForm {

  private MedianLineStatus agreementStatus;

  public MedianLineStatus getAgreementStatus() {
    return agreementStatus;
  }

  public void setAgreementStatus(MedianLineStatus agreementStatus) {
    this.agreementStatus = agreementStatus;
  }
}
