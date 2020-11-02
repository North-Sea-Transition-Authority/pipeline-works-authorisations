package uk.co.ogauthority.pwa.model.dto.appprocessing;

import java.util.Arrays;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

public class ApplicationInvolvementDto {

  private final PwaApplication pwaApplication;

  private final Set<PwaContactRole> contactRoles;

  private final boolean assignedAtResponderStage;

  private final Set<ConsulteeGroupMemberRole> consulteeRoles;

  private final boolean caseOfficerStageAndUserAssigned;

  public ApplicationInvolvementDto(PwaApplication pwaApplication,
                                   Set<PwaContactRole> contactRoles,
                                   boolean assignedAtResponderStage,
                                   Set<ConsulteeGroupMemberRole> consulteeRoles,
                                   boolean caseOfficerStageAndUserAssigned) {
    this.pwaApplication = pwaApplication;
    this.contactRoles = contactRoles;
    this.assignedAtResponderStage = assignedAtResponderStage;
    this.consulteeRoles = consulteeRoles;
    this.caseOfficerStageAndUserAssigned = caseOfficerStageAndUserAssigned;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public Set<PwaContactRole> getContactRoles() {
    return contactRoles;
  }

  public boolean isAssignedAtResponderStage() {
    return assignedAtResponderStage;
  }

  public Set<ConsulteeGroupMemberRole> getConsulteeRoles() {
    return consulteeRoles;
  }

  public boolean isCaseOfficerStageAndUserAssigned() {
    return caseOfficerStageAndUserAssigned;
  }

  public boolean hasAnyOfTheseContactRoles(PwaContactRole... roles) {
    return Arrays.stream(roles)
        .anyMatch(contactRoles::contains);
  }

  public boolean hasAnyOfTheseConsulteeRoles(ConsulteeGroupMemberRole... roles) {
    return Arrays.stream(roles)
        .anyMatch(consulteeRoles::contains);
  }

}
