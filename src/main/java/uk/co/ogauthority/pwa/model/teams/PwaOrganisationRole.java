package uk.co.ogauthority.pwa.model.teams;

import java.util.Arrays;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;

public enum PwaOrganisationRole {

  TEAM_ADMINISTRATOR("RESOURCE_COORDINATOR"),
  APPLICATION_CREATOR("APPLICATION_CREATE"),
  APPLICATION_SUBMITTER("APPLICATION_SUBMITTER"),
  FINANCE_ADMIN("FINANCE_ADMIN");

  private final String portalTeamRoleName;

  PwaOrganisationRole(String portalTeamRoleName) {
    this.portalTeamRoleName = portalTeamRoleName;
  }

  public String getPortalTeamRoleName() {
    return portalTeamRoleName;
  }

  public static PwaOrganisationRole resolveFromRoleName(String roleName) {
    return Arrays.stream(PwaOrganisationRole.values())
        .filter(v -> v.getPortalTeamRoleName().equals(roleName))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(String.format(
            "Couldn't find PwaOrganisationRole by role name: [%s]", roleName)));
  }

}
