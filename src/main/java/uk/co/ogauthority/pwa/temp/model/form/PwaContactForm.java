package uk.co.ogauthority.pwa.temp.model.form;

import uk.co.ogauthority.pwa.temp.model.pwacontacts.ContactRole;

public class PwaContactForm {

  private String name;
  private String emailAddress;
  private String telephoneNo;
  private ContactRole role;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public String getTelephoneNo() {
    return telephoneNo;
  }

  public void setTelephoneNo(String telephoneNo) {
    this.telephoneNo = telephoneNo;
  }

  public ContactRole getRole() {
    return role;
  }

  public void setRole(ContactRole role) {
    this.role = role;
  }
}
