package uk.co.ogauthority.pwa.temp.model.contacts;

public enum UooRole {

  USER("User"),
  OWNER("Owner"),
  OPERATOR("Operator");

  private String displayText;

  UooRole(String displayText) {
    this.displayText = displayText;
  }

  public String getDisplayText() {
    return displayText;
  }
}
