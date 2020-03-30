package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import org.hibernate.validator.constraints.Length;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;

public class MedianLineAgreementsForm {

  private MedianLineStatus agreementStatus;

  @Length(max = 4000, message = "Negotiator name must be 4000 characters or fewer")
  private String negotiatorNameIfOngoing;
  @Length(max = 4000, message = "Negotiator name must be 4000 characters or fewer")
  private String negotiatorNameIfCompleted;

  @Length(max = 4000, message = "Negotiator email must be 4000 characters or fewer")
  private String negotiatorEmailIfOngoing;
  @Length(max = 4000, message = "Negotiator email must be 4000 characters or fewer")
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
