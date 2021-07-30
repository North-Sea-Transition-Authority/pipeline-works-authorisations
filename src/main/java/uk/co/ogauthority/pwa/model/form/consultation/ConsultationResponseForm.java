package uk.co.ogauthority.pwa.model.form.consultation;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;

public class ConsultationResponseForm {

  private Map<ConsultationResponseOptionGroup, ConsultationResponseDataForm> responseDataForms;

  public Map<ConsultationResponseOptionGroup, ConsultationResponseDataForm> getResponseDataForms() {
    return responseDataForms;
  }

  public void setResponseDataForms(Map<ConsultationResponseOptionGroup, ConsultationResponseDataForm> responseDataForms) {
    this.responseDataForms = responseDataForms;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsultationResponseForm that = (ConsultationResponseForm) o;
    return Objects.equals(responseDataForms, that.responseDataForms);
  }

  @Override
  public int hashCode() {
    return Objects.hash(responseDataForms);
  }

}