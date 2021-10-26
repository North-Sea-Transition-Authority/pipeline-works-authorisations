package uk.co.ogauthority.pwa.features.application.tasks.huoo;

public class HuooTreatyAgreementView {

  private final String country;
  private final String treatyAgreementText;
  private final String roles;
  private final String removeUrl;

  public HuooTreatyAgreementView(PadOrganisationRole padOrganisationRole, String removeUrl) {
    this.country = padOrganisationRole.getAgreement().getCountry();
    this.treatyAgreementText = padOrganisationRole.getAgreement().getAgreementText();
    this.roles = padOrganisationRole.getRole().getDisplayText();
    this.removeUrl = removeUrl;
  }

  public String getCountry() {
    return country;
  }

  public String getTreatyAgreementText() {
    return treatyAgreementText;
  }

  public String getRoles() {
    return roles;
  }

  public String getRemoveUrl() {
    return removeUrl;
  }
}
