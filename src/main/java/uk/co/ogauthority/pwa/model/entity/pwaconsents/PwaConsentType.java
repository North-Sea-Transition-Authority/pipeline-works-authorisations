package uk.co.ogauthority.pwa.model.entity.pwaconsents;

public enum PwaConsentType {

  INITIAL_PWA("W"),
  VARIATION("V"),
  DEPOSIT_CONSENT("D");

  private final String refLetter;

  PwaConsentType(String refLetter) {
    this.refLetter = refLetter;
  }

  public String getRefLetter() {
    return refLetter;
  }

}
