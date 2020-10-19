package uk.co.ogauthority.pwa.model.form.consultation;


import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;

public class ConsultationResponseForm {

  private ConsultationResponseOption consultationResponseOption;

  private String confirmedDescription;

  private String rejectedDescription;

  public ConsultationResponseOption getConsultationResponseOption() {
    return consultationResponseOption;
  }

  public void setConsultationResponseOption(
      ConsultationResponseOption consultationResponseOption) {
    this.consultationResponseOption = consultationResponseOption;
  }

  public String getConfirmedDescription() {
    return confirmedDescription;
  }

  public void setConfirmedDescription(String confirmedDescription) {
    this.confirmedDescription = confirmedDescription;
  }

  public String getRejectedDescription() {
    return rejectedDescription;
  }

  public void setRejectedDescription(String rejectedDescription) {
    this.rejectedDescription = rejectedDescription;
  }

}
