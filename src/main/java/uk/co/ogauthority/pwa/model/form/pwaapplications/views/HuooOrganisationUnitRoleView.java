package uk.co.ogauthority.pwa.model.form.pwaapplications.views;

import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnitDetail;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

public class HuooOrganisationUnitRoleView {

  private final String registeredNumber;
  private final String companyName;
  private final String companyAddress;
  private final String roles;
  private final Set<HuooRole> roleSet;
  private final String editUrl;
  private final String removeUrl;

  public HuooOrganisationUnitRoleView(PortalOrganisationUnitDetail detail, Set<HuooRole> roles,
                                      String editUrl, String removeUrl) {
    this.editUrl = editUrl;
    this.removeUrl = removeUrl;
    // Can be null if there isn't any address information associated with the org unit.
    if (detail == null) {
      this.registeredNumber = null;
      this.companyName = null;
      this.companyAddress = null;
    } else {
      this.registeredNumber = detail.getRegisteredNumber();
      this.companyName = detail.getOrganisationUnit().getName();
      this.companyAddress = detail.getLegalAddress();
    }
    this.roles = roles.stream()
        .sorted(Comparator.comparing(HuooRole::getDisplayOrder))
        .map(HuooRole::getDisplayText)
        .collect(Collectors.joining(", "));
    this.roleSet = roles;
  }

  public String getRegisteredNumber() {
    return registeredNumber;
  }

  public String getCompanyName() {
    return companyName;
  }

  public String getCompanyAddress() {
    return companyAddress;
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
