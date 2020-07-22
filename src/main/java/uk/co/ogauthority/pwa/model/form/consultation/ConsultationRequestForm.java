package uk.co.ogauthority.pwa.model.form.consultation;


import java.util.HashMap;
import java.util.Map;

public class ConsultationRequestForm {

  private Map<String, String> consulteeGroupSelection = new HashMap<>();
  private Integer daysToRespond;
  private String otherGroupLogin;


  public ConsultationRequestForm() {

  }

  public Map<String, String> getConsulteeGroupSelection() {
    return consulteeGroupSelection;
  }

  public void setConsulteeGroupSelection(Map<String, String> consulteeGroupSelection) {
    this.consulteeGroupSelection = consulteeGroupSelection;
  }

  public Integer getDaysToRespond() {
    return daysToRespond;
  }

  public void setDaysToRespond(Integer daysToRespond) {
    this.daysToRespond = daysToRespond;
  }

  public String getOtherGroupLogin() {
    return otherGroupLogin;
  }

  public void setOtherGroupLogin(String otherGroupLogin) {
    this.otherGroupLogin = otherGroupLogin;
  }
}
