package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.util.stream.Stream;

/**
 * Roles associated with organisation users.
 * TODO PWA-1149 this needs to be unified with PwaOrganisationRole.
 */
public enum PwaOrganisationUserRole implements PwaUserRole {

  ACCESS_MANAGER("Access manager", "Can add, update or remove users from your organisation account (Access manager)", 10),

  APPLICATION_CREATOR("Application creator", "Can create PWA and associated applications (Application creator)", 30),

  APPLICATION_SUBMITTER("Application submitter", "Can submit applications to the OGA (Application submitter)", 40),

  FINANCE_ADMIN("Finance administrator", "Can pay for any submitted PWA application (Finance administrator)", 50);

  private final String roleName;
  private final String roleDescription;
  private final int displayOrder;

  PwaOrganisationUserRole(String roleName, String roleDescription, int displayOrder) {
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

  public static Stream<PwaOrganisationUserRole> stream() {
    return Stream.of(PwaOrganisationUserRole.values());
  }

}
