package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

/**
 * Use when validating application's HUOO's as a whole.
 */
public final class HuooSummaryValidationResult {

  // which roles have no org or treaty instances?
  private final Set<HuooRole> unassignedRoles;

  // organisation names assigned to at least 1 role but are inactive
  private final List<String> sortedInactiveOrganisationsWithRole;

  // specific or complex rules that have been breached that don't require error message context.
  private final Set<HuooRules> breachedBusinessRules;

  // does this object result represent a valid and complete HUOO section
  private final boolean isValid;

  public HuooSummaryValidationResult(Set<HuooRole> unassignedRoles,
                                     List<String> sortedInactiveOrganisationsWithRole,
                                     Set<HuooRules> breachedBusinessRules) {
    this.unassignedRoles = unassignedRoles;
    this.sortedInactiveOrganisationsWithRole = sortedInactiveOrganisationsWithRole;
    this.breachedBusinessRules = breachedBusinessRules;

    this.isValid = unassignedRoles.isEmpty() && sortedInactiveOrganisationsWithRole.isEmpty() && breachedBusinessRules.isEmpty();
  }

  public Set<HuooRole> getUnassignedRoles() {
    return unassignedRoles;
  }

  public List<String> getSortedInactiveOrganisationsWithRole() {
    return sortedInactiveOrganisationsWithRole;
  }

  public Set<HuooRules> getBreachedBusinessRules() {
    return breachedBusinessRules;
  }

  public boolean isValid() {
    return isValid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HuooSummaryValidationResult that = (HuooSummaryValidationResult) o;
    return isValid == that.isValid && Objects.equals(unassignedRoles,
        that.unassignedRoles) && Objects.equals(sortedInactiveOrganisationsWithRole,
        that.sortedInactiveOrganisationsWithRole) && Objects.equals(breachedBusinessRules,
        that.breachedBusinessRules);
  }

  @Override
  public int hashCode() {
    return Objects.hash(unassignedRoles, sortedInactiveOrganisationsWithRole, breachedBusinessRules, isValid);
  }

  @Override
  public String toString() {
    return "HuooSummaryValidationResult{" +
        "unassignedRoles=" + unassignedRoles +
        ", sortedInactiveOrganisationsWithRole=" + sortedInactiveOrganisationsWithRole +
        ", breachedBusinessRules=" + breachedBusinessRules +
        ", isValid=" + isValid +
        '}';
  }

  public enum HuooRules {
    CANNOT_HAVE_TREATY_AND_PORTAL_ORG_USERS
  }
}
