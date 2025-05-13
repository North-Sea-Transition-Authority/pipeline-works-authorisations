package uk.co.ogauthority.pwa.features.application.authorisation.permission;

import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.teams.Role;

/**
 * Essentially a cut down ApplicationInvolvement that simply deals with team membership on an application.
 * No other flags not directly related to a user's team involvement should be added here.
 */
public class UserRolesForApplicationDto {

  private final Set<PwaContactRole> userContactRoles;
  private final Set<Role> userHolderTeamRoles;
  private final Set<PwaRegulatorRole> userRegulatorRoles;
  private final Set<Role> userConsulteeRoles;

  UserRolesForApplicationDto(Set<PwaContactRole> userContactRoles,
                             Set<Role> userHolderTeamRoles,
                             Set<PwaRegulatorRole> userRegulatorRoles,
                             Set<Role> userConsulteeRoles) {
    this.userContactRoles = userContactRoles;
    this.userHolderTeamRoles = userHolderTeamRoles;
    this.userRegulatorRoles = userRegulatorRoles;
    this.userConsulteeRoles = userConsulteeRoles;
  }

  public Set<PwaContactRole> getUserContactRoles() {
    return userContactRoles;
  }

  public Set<Role> getUserHolderTeamRoles() {
    return userHolderTeamRoles;
  }

  public Set<PwaRegulatorRole> getUserRegulatorRoles() {
    return userRegulatorRoles;
  }

  public Set<Role> getUserConsulteeRoles() {
    return userConsulteeRoles;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserRolesForApplicationDto that = (UserRolesForApplicationDto) o;
    return Objects.equals(userContactRoles, that.userContactRoles)
        && Objects.equals(userHolderTeamRoles, that.userHolderTeamRoles)
        && Objects.equals(userRegulatorRoles, that.userRegulatorRoles)
        && Objects.equals(userConsulteeRoles, that.userConsulteeRoles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userContactRoles, userHolderTeamRoles, userRegulatorRoles, userConsulteeRoles);
  }

  @Override
  public String toString() {
    return "UserRolesForApplicationDto{" +
        "userContactRoles=" + userContactRoles +
        ", userHolderTeamRoles=" + userHolderTeamRoles +
        ", userRegulatorRoles=" + userRegulatorRoles +
        ", userConsulteeRoles=" + userConsulteeRoles +
        '}';
  }
}
