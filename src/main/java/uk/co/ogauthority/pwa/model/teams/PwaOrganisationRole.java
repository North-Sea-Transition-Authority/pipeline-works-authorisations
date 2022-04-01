package uk.co.ogauthority.pwa.model.teams;

import java.util.Arrays;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.exception.ValueNotFoundException;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaUserRole;

/**
 * Roles associated with organisation users.
 */
public enum PwaOrganisationRole implements PwaUserRole {

  TEAM_ADMINISTRATOR(
      "RESOURCE_COORDINATOR",
      "Team administrator",
      "Can add, update or remove users from your organisation account (Team administrator)",
      10),

  APPLICATION_CREATOR(
      "APPLICATION_CREATE",
      "Application creator",
      "Can create PWA and associated applications (Application creator)",
      30),

  APPLICATION_SUBMITTER(
      "APPLICATION_SUBMITTER",
      "Application submitter",
      "Can submit applications to the NSTA (Application submitter)",
      40),

  FINANCE_ADMIN(
      "FINANCE_ADMIN",
      "Finance administrator",
      "Can pay for any submitted PWA application (Finance administrator)",
      50),

  AS_BUILT_NOTIFICATION_SUBMITTER(
      "AS_BUILT_NOTIF_SUBMITTER",
      "As-built notification submitter",
      "Can submit as-built notifications to the NSTA (As-built notification submitter)",
      60);

  private final String portalTeamRoleName;
  private final String displayName;
  private final String description;
  private final int displayOrder;

  PwaOrganisationRole(String portalTeamRoleName, String displayName, String description, int displayOrder) {
    this.portalTeamRoleName = portalTeamRoleName;
    this.displayName = displayName;
    this.description = description;
    this.displayOrder = displayOrder;
  }

  public String getPortalTeamRoleName() {
    return portalTeamRoleName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public static PwaOrganisationRole resolveFromRoleName(String roleName) {
    return Arrays.stream(PwaOrganisationRole.values())
        .filter(v -> v.getPortalTeamRoleName().equals(roleName))
        .findFirst()
        .orElseThrow(() -> new ValueNotFoundException(String.format(
            "Couldn't find PwaOrganisationRole by role name: [%s]", roleName)));
  }

  public static Stream<PwaOrganisationRole> stream() {
    return Stream.of(PwaOrganisationRole.values());
  }

}
