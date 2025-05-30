package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import java.util.stream.Stream;

/**
 * Roles associated with users at the master PWA level.
 */
public enum PwaContactRole {

  ACCESS_MANAGER("Access manager", " Can add, update and remove users for this application (Access manager)", 10),

  PREPARER("Application preparer", "Can edit this application (Preparer)", 30),

  VIEWER("Application viewer", "Can view this application (Viewer)", 40);

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
