package uk.co.ogauthority.pwa.temp.model.form;

import uk.co.ogauthority.pwa.temp.model.contacts.UooAgreement;
import uk.co.ogauthority.pwa.temp.model.contacts.UooType;

public class UserOwnerOperatorForm {

  private UooType type;

  private String roles;
  private UooAgreement uooAgreement;
  private String uooAgreementOther;

  private String companyNumber;
  private String companyName;
  private String companyAddress;

  public UooType getType() {
    return type;
  }

  public void setType(UooType type) {
    this.type = type;
  }

  public String getRoles() {
    return roles;
  }

  public void setRoles(String roles) {
    this.roles = roles;
  }

  public UooAgreement getUooAgreement() {
    return uooAgreement;
  }

  public void setUooAgreement(UooAgreement uooAgreement) {
    this.uooAgreement = uooAgreement;
  }

  public String getUooAgreementOther() {
    return uooAgreementOther;
  }

  public void setUooAgreementOther(String uooAgreementOther) {
    this.uooAgreementOther = uooAgreementOther;
  }

  public String getCompanyNumber() {
    return companyNumber;
  }

  public void setCompanyNumber(String companyNumber) {
    this.companyNumber = companyNumber;
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
}
