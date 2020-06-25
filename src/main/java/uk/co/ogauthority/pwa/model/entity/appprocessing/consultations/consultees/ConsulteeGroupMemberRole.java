package uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees;

import java.util.stream.Stream;

/**
 * Enumeration of roles available to a consultee group team member.
 */
public enum ConsulteeGroupMemberRole {

  ACCESS_MANAGER("Access manager", "Can add, update and remove users in this team (Access manager)", 10),
  RECIPIENT("Recipient", "Can receive consultation requests for applications and assign to responders (Recipient)", 20),
  RESPONDER("Responder", "Can (re)assign responder for a consultation request and send response (Responder)", 30);

  private final String displayName;
  private final String description;
  private final int displayOrder;

  ConsulteeGroupMemberRole(String displayName, String description, int displayOrder) {
    this.displayName = displayName;
    this.description = description;
    this.displayOrder = displayOrder;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public static Stream<ConsulteeGroupMemberRole> stream() {
    return Stream.of(ConsulteeGroupMemberRole.values());
  }

}
