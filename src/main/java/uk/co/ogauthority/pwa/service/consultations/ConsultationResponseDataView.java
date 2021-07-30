package uk.co.ogauthority.pwa.service.consultations;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;

public class ConsultationResponseDataView {

  private ConsultationResponseOptionGroup consultationResponseOptionGroup;
  private ConsultationResponseOption consultationResponseOption;
  private String responseText;

  public ConsultationResponseOptionGroup getConsultationResponseOptionGroup() {
    return consultationResponseOptionGroup;
  }

  public void setConsultationResponseOptionGroup(ConsultationResponseOptionGroup consultationResponseOptionGroup) {
    this.consultationResponseOptionGroup = consultationResponseOptionGroup;
  }

  public ConsultationResponseOption getConsultationResponseOption() {
    return consultationResponseOption;
  }

  public void setConsultationResponseOption(ConsultationResponseOption consultationResponseOption) {
    this.consultationResponseOption = consultationResponseOption;
  }

  public String getResponseText() {
    return responseText;
  }

  public void setResponseText(String responseText) {
    this.responseText = responseText;
  }

  public static ConsultationResponseDataView from(ConsultationResponseData responseData) {

    var view = new ConsultationResponseDataView();
    view.setConsultationResponseOptionGroup(responseData.getResponseGroup());
    view.setConsultationResponseOption(responseData.getResponseType());
    view.setResponseText(responseData.getResponseText());
    return view;

  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsultationResponseDataView that = (ConsultationResponseDataView) o;
    return consultationResponseOptionGroup == that.consultationResponseOptionGroup
        && consultationResponseOption == that.consultationResponseOption
        && Objects.equals(responseText, that.responseText);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consultationResponseOptionGroup, consultationResponseOption, responseText);
  }

}
