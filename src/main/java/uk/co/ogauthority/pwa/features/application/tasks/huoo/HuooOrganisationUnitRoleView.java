package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnitDetail;

public class HuooOrganisationUnitRoleView implements Comparable<HuooOrganisationUnitRoleView> {

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HuooOrganisationUnitRoleView that = (HuooOrganisationUnitRoleView) o;
    return Objects.equals(registeredNumber, that.registeredNumber)
        && Objects.equals(companyName, that.companyName)
        && Objects.equals(companyAddress, that.companyAddress)
        && Objects.equals(roles, that.roles)
        && Objects.equals(roleSet, that.roleSet)
        && Objects.equals(editUrl, that.editUrl)
        && Objects.equals(removeUrl, that.removeUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(registeredNumber, companyName, companyAddress, roles, roleSet, editUrl, removeUrl);
  }

  /**
   * Given two organisations with HUOO roles, return the result of comparing:
   * 1. the roles the companies have, whichever has the role with the lowest display order wins.
   * 2. if no winner can be found based on roles, compare the organisations by name, lowest alphabetically wins.
   */
  @Override
  public int compareTo(HuooOrganisationUnitRoleView that) {

    int thisDisplayOrder = this.getRoleSet().stream().mapToInt(HuooRole::getDisplayOrder).min().orElse(0);
    int thatDisplayOrder = that.getRoleSet().stream().mapToInt(HuooRole::getDisplayOrder).min().orElse(0);

    int comparison = Integer.compare(thisDisplayOrder, thatDisplayOrder);

    if (comparison == 0) {
      return Integer.compare(this.getCompanyName().compareTo(that.getCompanyName()), 0);
    }

    return comparison;

  }
}
