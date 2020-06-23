package uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees;

/**
 * Enumeration of roles available to a consultee group team member.
 */
public enum ConsulteeGroupMemberRole {

  ACCESS_MANAGER("Access manager"),
  RECIPIENT("Recipient"),
  RESPONDER("Responder");

  private final String displayName;

  ConsulteeGroupMemberRole(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
