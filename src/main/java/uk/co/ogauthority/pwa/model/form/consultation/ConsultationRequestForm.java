package uk.co.ogauthority.pwa.model.form.consultation;


import java.util.HashMap;
import java.util.Map;

public class ConsultationRequestForm {

  private Map<String, String> consulteeGroupSelection = new HashMap<>();
  private Integer daysToRespond = 28;
  private Boolean otherGroupSelected;
  private String otherGroupLogin;


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

  public Boolean getOtherGroupSelected() {
    return otherGroupSelected;
  }

  public void setOtherGroupSelected(Boolean otherGroupSelected) {
    this.otherGroupSelected = otherGroupSelected;
  }

  public String getOtherGroupLogin() {
    return otherGroupLogin;
  }

  public void setOtherGroupLogin(String otherGroupLogin) {
    this.otherGroupLogin = otherGroupLogin;
  }
}
