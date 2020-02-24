package uk.co.ogauthority.pwa.temp.model.form;

import uk.co.ogauthority.pwa.temp.model.pwacontacts.ContactRole;

public class PwaContactForm {

  private String userIdentifier;
  private ContactRole role;

  public String getUserIdentifier() {
    return userIdentifier;
  }

  public void setUserIdentifier(String userIdentifier) {
    this.userIdentifier = userIdentifier;
  }

  public ContactRole getRole() {
    return role;
  }

  public void setRole(ContactRole role) {
    this.role = role;
  }
}
