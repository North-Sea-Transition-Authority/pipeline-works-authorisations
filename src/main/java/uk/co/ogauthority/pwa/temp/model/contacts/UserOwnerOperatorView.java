package uk.co.ogauthority.pwa.temp.model.contacts;

import java.util.Set;

public class UserOwnerOperatorView {

  private Integer companyHouseNumber;
  private String companyName;
  private String companyAddress;
  private Set<String> roles;

  public UserOwnerOperatorView(Integer companyHouseNumber, String companyName, String companyAddress, Set<String> roles) {
    this.companyHouseNumber = companyHouseNumber;
    this.companyName = companyName;
    this.companyAddress = companyAddress;
    this.roles = roles;
  }

  public Integer getCompanyHouseNumber() {
    return companyHouseNumber;
  }

  public String getCompanyName() {
    return companyName;
  }

  public String getCompanyAddress() {
    return companyAddress;
  }

  public Set<String> getRoles() {
    return roles;
  }
}
