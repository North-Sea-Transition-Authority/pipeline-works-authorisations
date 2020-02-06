package uk.co.ogauthority.pwa.energyportal.model.dto.teams;

import java.util.Set;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;

public class PortalTeamMemberDto {

  // TODO TEAMS_REFACTOR Not using Person object here as that logic probably belongs in the application.
  //   Leaving it to the application means more control over what sql is run and when, also defers decision about
  //   whether we even need to get a "Person" object at all. This is up for discussion imo and can be changed.
  private final PersonId personId;

  private final Set<PortalRoleDto> roles;

  public PortalTeamMemberDto(PersonId personId, Set<PortalRoleDto> roles) {
    this.personId = personId;
    this.roles = roles;
  }

  public PersonId getPersonId() {
    return personId;
  }

  public Set<PortalRoleDto> getRoles() {
    return roles;
  }
}