package uk.co.ogauthority.pwa.model.form.consultation;


import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;

public class ConsultationResponseForm {

  private ConsultationResponseOptionGroup consultationResponseOptionGroup;

  private ConsultationResponseOption consultationResponseOption;

  private String option1Description;

  private String option2Description;

  public ConsultationResponseOptionGroup getConsultationResponseOptionGroup() {
    return consultationResponseOptionGroup;
  }

  public void setConsultationResponseOptionGroup(
      ConsultationResponseOptionGroup consultationResponseOptionGroup) {
    this.consultationResponseOptionGroup = consultationResponseOptionGroup;
  }

  public ConsultationResponseOption getConsultationResponseOption() {
    return consultationResponseOption;
  }

  public void setConsultationResponseOption(ConsultationResponseOption consultationResponseOption) {
    this.consultationResponseOption = consultationResponseOption;
  }

  public String getOption1Description() {
    return option1Description;
  }

  public void setOption1Description(String option1Description) {
    this.option1Description = option1Description;
  }

  public String getOption2Description() {
    return option2Description;
  }

  public void setOption2Description(String option2Description) {
    this.option2Description = option2Description;
  }

}
