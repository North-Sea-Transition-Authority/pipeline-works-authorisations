package uk.co.ogauthority.pwa.service.enums.pwaapplications.generic;

public enum ValidationType {

  FULL("Save and complete"),
  PARTIAL("Save and complete later");

  private final String buttonText;

  ValidationType(String buttonText) {
    this.buttonText = buttonText;
  }

  public String getButtonText() {
    return buttonText;
  }

}
