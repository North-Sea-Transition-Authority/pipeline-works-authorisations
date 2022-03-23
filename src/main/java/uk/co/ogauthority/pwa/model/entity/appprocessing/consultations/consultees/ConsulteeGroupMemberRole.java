package uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees;

import java.util.stream.Stream;

/**
 * Enumeration of roles available to a consultee group team member.
 */
public enum ConsulteeGroupMemberRole {

  ACCESS_MANAGER("Access manager", "Control who can receive and respond on consultations from the NSTA (Access manager)", 10),
  RECIPIENT("Consultation recipient", "Receives PWA consultation requests from the NSTA (Consultation recipient)", 20),
  RESPONDER("Consultation responder", "Responds to the NSTA on PWA consultations (Consultation responder)", 30);

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
