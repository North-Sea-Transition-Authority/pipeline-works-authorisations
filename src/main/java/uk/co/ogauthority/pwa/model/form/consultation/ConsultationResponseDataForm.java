package uk.co.ogauthority.pwa.model.form.consultation;


import java.util.Objects;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;

public class ConsultationResponseDataForm {

  private ConsultationResponseOption consultationResponseOption;

  private String option1Description;

  private String option2Description;

  private String option3Description;

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

  public String getOption3Description() {
    return option3Description;
  }

  public void setOption3Description(String option3Description) {
    this.option3Description = option3Description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsultationResponseDataForm that = (ConsultationResponseDataForm) o;
    return consultationResponseOption == that.consultationResponseOption && Objects.equals(option1Description,
        that.option1Description) && Objects.equals(option2Description,
        that.option2Description) && Objects.equals(option3Description, that.option3Description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consultationResponseOption, option1Description, option2Description, option3Description);
  }

}
