package uk.co.ogauthority.pwa.features.application.authorisation.appcontacts;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.util.enumutils.Displayable;

/**
 * Roles associated with users at the master PWA level.
 */
public enum PwaContactRole implements Displayable {

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

  @Override
  public String getDisplayName() {
    return "Role Name: %s%n Role Description: %s".formatted(roleName, roleDescription);
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<PwaContactRole> stream() {
    return Stream.of(PwaContactRole.values());
  }
  
  public static Set<PwaContactRole> allRoles() {
    return EnumSet.allOf(PwaContactRole.class);
  }

}
