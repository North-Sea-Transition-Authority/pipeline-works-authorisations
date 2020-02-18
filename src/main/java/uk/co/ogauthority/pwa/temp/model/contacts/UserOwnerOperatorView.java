package uk.co.ogauthority.pwa.temp.model.contacts;

import java.util.Set;

public class UserOwnerOperatorView {

  private Integer companiesHouseNumber;
  private String companyName;
  private String companyAddress;
  private Set<String> roles;

  public UserOwnerOperatorView(Integer companiesHouseNumber, String companyName, String companyAddress,
                               Set<String> roles) {
    this.companiesHouseNumber = companiesHouseNumber;
    this.companyName = companyName;
    this.companyAddress = companyAddress;
    this.roles = roles;
  }

  public Integer getCompaniesHouseNumber() {
    return companiesHouseNumber;
  }

  public void setCompaniesHouseNumber(Integer companiesHouseNumber) {
    this.companiesHouseNumber = companiesHouseNumber;
  }

  public String getCompanyName() {
    return companyName;
  }

  public void setCompanyName(String companyName) {
    this.companyName = companyName;
  }

  public String getCompanyAddress() {
    return companyAddress;
  }

  public void setCompanyAddress(String companyAddress) {
    this.companyAddress = companyAddress;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }
}
