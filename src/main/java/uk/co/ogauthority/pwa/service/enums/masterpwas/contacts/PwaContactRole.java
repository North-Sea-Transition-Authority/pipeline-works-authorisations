package uk.co.ogauthority.pwa.service.enums.masterpwas.contacts;

import java.util.stream.Stream;

/**
 * Roles associated with users at the master PWA level.
 */
public enum PwaContactRole {

  ACCESS_MANAGER("Access manager", "Can add, update and remove contacts for this application (Access manager)", 10),

  SUBMITTER("Submitter", "Can submit this application (Submitter)", 20),

  PREPARER("Preparer", "Can edit this application (Preparer)", 30),

  VIEWER("Viewer", "Can view this application (Viewer)", 40);

  private final String roleName;
  private final String roleDescription;
  private final int displayOrder;

  PwaContactRole(String roleName, String roleDescription, int displayOrder) {
    this.roleName = roleName;
    this.roleDescription = roleDescription;
    this.displayOrder = displayOrder;
  }

  public String getRoleName() {
    return roleName;
  }

  public String getRoleDescription() {
    return roleDescription;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<PwaContactRole> stream() {
    return Stream.of(PwaContactRole.values());
  }

}
