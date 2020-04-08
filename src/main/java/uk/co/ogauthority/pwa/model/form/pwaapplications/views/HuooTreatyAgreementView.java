package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;

public class HuooTreatyAgreementView {

  private final String country;
  private final String treatyAgreementText;
  private final String roles;
  private final Set<HuooRole> roleSet;
  private final String editUrl;
  private final String removeUrl;

  public HuooTreatyAgreementView(PadOrganisationRole padOrganisationRole, String editUrl, String removeUrl) {
    country = padOrganisationRole.getAgreement().getCountry();
    treatyAgreementText = padOrganisationRole.getAgreement().getAgreementText();
    roleSet = padOrganisationRole.getRoles();
    roles = padOrganisationRole.getRoles()
        .stream()
        .sorted(Comparator.comparing(HuooRole::getDisplayOrder))
        .map(HuooRole::getDisplayText)
        .collect(Collectors.joining(", "));
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

  public Set<HuooRole> getRoleSet() {
    return roleSet;
  }

  public String getEditUrl() {
    return editUrl;
  }

  public String getRemoveUrl() {
    return removeUrl;
  }
}
