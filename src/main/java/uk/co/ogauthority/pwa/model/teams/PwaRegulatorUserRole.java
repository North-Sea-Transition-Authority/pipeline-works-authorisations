package uk.co.ogauthority.pwa.model.teams;

import java.util.stream.Stream;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaUserRole;

/**
 * Roles associated with PWA regulator team.
 * TODO PWA-1149 this needs to be unified with PwaRegulatorRole.
 */
public enum PwaRegulatorUserRole implements PwaUserRole {

  CASE_OFFICER("Case Officer", "Process applications and run consultations (Case Officer)", 10),

  ORGANISATION_MANAGER("Organisation Team Manager", "Manage organisation access to PWAs (Organisation Team Manager)", 20),

  PWA_CONSENT_VIEWER("PWA Consent Viewer", "Search for and view consented PWA data (PWA Consent Viewer)", 30),

  PWA_MANAGER("PWA Manager", "Accept applications and allocate case officers (PWA Manager)", 40),

  RESOURCE_COORDINATOR("Team Administrator", "Manage access to the team (Team Administrator)", 50),

  AS_BUILT_NOTIF_ADMIN("As-built Notification Administrator", "Manage as-built notifications", 60);

  private final String roleName;
  private final String roleDescription;
  private final int displayOrder;

  PwaRegulatorUserRole(String roleName, String roleDescription, int displayOrder) {
    this.roleName = roleName;
    this.roleDescription = roleDescription;
    this.displayOrder = displayOrder;
  }

  @Override
  public String getRoleName() {
    return roleName;
  }

  @Override
  public String getRoleDescription() {
    return roleDescription;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<PwaRegulatorUserRole> stream() {
    return Stream.of(PwaRegulatorUserRole.values());
  }

}
