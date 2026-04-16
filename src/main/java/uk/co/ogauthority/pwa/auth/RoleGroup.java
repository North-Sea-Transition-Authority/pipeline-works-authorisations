package uk.co.ogauthority.pwa.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

public enum RoleGroup {
  APPLICATION_SEARCH(
      // Regulator Roles
      Set.of(Role.TEAM_ADMINISTRATOR,
          Role.ORGANISATION_MANAGER,
          Role.PWA_MANAGER,
          Role.CASE_OFFICER,
          Role.AS_BUILT_NOTIFICATION_ADMIN,
          Role.CONSENT_VIEWER),

      // Organisation Roles
      TeamType.ORGANISATION.getAllowedRolesAsSet(), // All organisation roles

      // Consultee Roles
      TeamType.CONSULTEE.getAllowedRolesAsSet(), // All consultee roles

      //Secondary regulator roles
      Set.of()
  ),
  CONSENT_SEARCH(
      // Regulator Roles
      TeamType.REGULATOR.getAllowedRolesAsSet(), // All regulator roles

      // Organisation Roles
      Set.of(Role.TEAM_ADMINISTRATOR,
          Role.APPLICATION_CREATOR,
          Role.APPLICATION_SUBMITTER),

      // Consultee Roles
      Set.of(),

      //Secondary regulator roles
      TeamType.SECONDARY_REGULATOR.getAllowedRolesAsSet()
  ),
  ASBUILT_WORKAREA(
      // Regulator Roles
      Set.of(Role.AS_BUILT_NOTIFICATION_ADMIN),

      // Organisation Roles
      Set.of(Role.AS_BUILT_NOTIFICATION_SUBMITTER),

      // Consultee Roles
      Set.of(),

      //Secondary regulator roles
      Set.of()
  ),
  ;

  private final Set<Role> regulatorRoles;
  private final Set<Role> orgRoles;
  private final Set<Role> consulteeRoles;
  private final Set<Role> secondaryRegulatorRoles;

  RoleGroup(Set<Role> regulatorRoles, Set<Role> orgRoles, Set<Role> consulteeRoles, Set<Role> secondaryRegulatorRoles) {
    this.regulatorRoles = regulatorRoles;
    this.orgRoles = orgRoles;
    this.consulteeRoles = consulteeRoles;
    this.secondaryRegulatorRoles = secondaryRegulatorRoles;
  }

  public Map<TeamType, Set<Role>> getRolesByTeamType() {
    Map<TeamType, Set<Role>> teamRolesMap = new HashMap<>();
    teamRolesMap.put(TeamType.REGULATOR, regulatorRoles);
    teamRolesMap.put(TeamType.ORGANISATION, orgRoles);
    teamRolesMap.put(TeamType.CONSULTEE, consulteeRoles);
    teamRolesMap.put(TeamType.SECONDARY_REGULATOR, secondaryRegulatorRoles);
    return teamRolesMap;
  }
}
