package uk.co.ogauthority.pwa.temp.model.contacts;

import java.util.Set;

public class UooAgreementView {

  private String agreementText;
  private Set<String> roles;

  public UooAgreementView(String agreementText, Set<String> roles) {
    this.agreementText = agreementText;
    this.roles = roles;
  }

  public String getAgreementText() {
    return agreementText;
  }

  public Set<String> getRoles() {
    return roles;
  }
}
