package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

public class HuooTreatyAgreementView {

  private final String country;
  private final String treatyAgreementText;
  private final String roles;
  private final String editUrl;
  private final String removeUrl;

  public HuooTreatyAgreementView(PadOrganisationRole padOrganisationRole, String editUrl, String removeUrl) {
    this.country = padOrganisationRole.getAgreement().getCountry();
    this.treatyAgreementText = padOrganisationRole.getAgreement().getAgreementText();
    this.roles = padOrganisationRole.getRole().getDisplayText();
    this.editUrl = editUrl;
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

  public String getEditUrl() {
    return editUrl;
  }

  public String getRemoveUrl() {
    return removeUrl;
  }
}
