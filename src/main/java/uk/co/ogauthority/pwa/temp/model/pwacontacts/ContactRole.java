package uk.co.ogauthority.pwa.temp.model.pwacontacts;

public enum ContactRole {

  CONTRACTOR("Contractor"),
  SUBMITTER("Submitter");

  private String displayText;

  ContactRole(String displayText) {
    this.displayText = displayText;
  }

  @Override
  public String toString() {
    return displayText;
  }
}
