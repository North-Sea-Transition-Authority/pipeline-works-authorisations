package uk.co.ogauthority.pwa.model.teams;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaUserRole;

public enum PwaRegulatorRole implements PwaUserRole {

  TEAM_ADMINISTRATOR(
      "RESOURCE_COORDINATOR",
      "Team Administrator",
      "Manage access to the team (Team Administrator)",
      50
  ),

  ORGANISATION_MANAGER(
      "ORGANISATION_MANAGER",
      "Organisation Team Manager",
      "Manage organisation access to PWAs (Organisation Team Manager)",
      20),

  PWA_MANAGER(
      "PWA_MANAGER",
      "PWA Manager",
      "Accept applications and allocate case officers (PWA Manager)",
      40),

  CASE_OFFICER(
      "CASE_OFFICER",
      "Case Officer",
      "Process applications and run consultations (Case Officer)",
      10),

  CONSENT_VIEWER(
      "PWA_CONSENT_VIEWER",
      "PWA Consent Viewer",
      "Search for and view consented PWA data (PWA Consent Viewer)",
      80),

  AS_BUILT_NOTIFICATION_ADMIN(
      "AS_BUILT_NOTIF_ADMIN",
      "As-built Notification Administrator",
      "Manage as-built notifications",
      60),

  TEMPLATE_CLAUSE_MANAGER(
      "TEMPLATE_CLAUSE_MANAGER",
      "Template Clause Manager",
      "Manage document template clauses",
      70);

  private final String portalTeamRoleName;
  private final String displayName;
  private final String description;
  private final int displayOrder;

  PwaRegulatorRole(String portalTeamRoleName, String displayName, String description, int displayOrder) {
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

  public static PwaRegulatorRole getValueByPortalTeamRoleName(String portalTeamRoleName) {
    return Stream.of(PwaRegulatorRole.values())
        .filter(r -> r.getPortalTeamRoleName().equals(portalTeamRoleName))
        .findFirst()
        .orElseThrow(() -> new RuntimeException(String.format(
            "Couldn't map portal team role name: %s to a PwaRegulatorRole value", portalTeamRoleName)));
  }

  public static Stream<PwaRegulatorRole> stream() {
    return Stream.of(PwaRegulatorRole.values());
  }

}
