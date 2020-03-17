package uk.co.ogauthority.pwa.service.enums.masterpwas.contacts;

/**
 * Roles associated with users at the master PWA level.
 */
public enum PwaContactRole {

  ACCESS_MANAGER("Access manager", 10),

  SUBMITTER("Submitter", 20),

  PREPARER("Preparer", 30),

  VIEWER("Viewer", 40);

  private final String roleName;
  private final int displayOrder;

  PwaContactRole(String roleName, int displayOrder) {
    this.roleName = roleName;
    this.displayOrder = displayOrder;
  }

  public String getRoleName() {
    return roleName;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }
}
