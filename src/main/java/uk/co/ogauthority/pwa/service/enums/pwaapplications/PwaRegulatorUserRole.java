package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.util.stream.Stream;

/**
 * Roles associated with PWA regulator team.
 */
public enum PwaRegulatorUserRole implements PwaUserRole {

  CASE_OFFICER("Case Officer", "Process applications and run consultations (Case Officer)", 10),

  ORGANISATION_MANAGER("Organisation Team Manager", "Manage organisation access to PWAs (Organisation Team Manager)", 20),

  PWA_CONSENT_VIEWER("PWA Consent Viewer", "Search for and view consented PWA data (PWA Consent Viewer)", 30),

  PWA_MANAGER("PWA Manager", "Accept applications and allocate case officers (PWA Manager)", 40),

  RESOURCE_COORDINATOR("Team Administrator", "Manage access to the team (Team Administrator)", 50);

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
