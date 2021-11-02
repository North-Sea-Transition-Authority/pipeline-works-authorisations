package uk.co.ogauthority.pwa.integrations.energyportal.teams.external;

import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;

/**
 * Package private class only to be used to process raw Portal Teams data into into API DTOs.
 */
class PortalTeamMemberRoleResult {

  private final int resId;
  private final PersonId personId;
  private final String roleName;
  private final String roleTitle;
  private final String roleDescription;
  private final int roleDisplaySequence;

  public PortalTeamMemberRoleResult(int resId,
                                    int personId,
                                    String roleName,
                                    String roleTitle,
                                    String roleDescription,
                                    int roleDisplaySequence
  ) {
    this.resId = resId;
    this.personId = new PersonId(personId);
    this.roleName = roleName;
    this.roleTitle = roleTitle;
    this.roleDescription = roleDescription;
    this.roleDisplaySequence = roleDisplaySequence;
  }

  public int getResId() {
    return resId;
  }

  public PersonId getPersonId() {
    return personId;
  }

  public String getRoleName() {
    return roleName;
  }

  public String getRoleTitle() {
    return roleTitle;
  }

  public String getRoleDescription() {
    return roleDescription;
  }

  public int getRoleDisplaySequence() {
    return roleDisplaySequence;
  }
}
