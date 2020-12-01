package uk.co.ogauthority.pwa.model.dto.appprocessing;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

public class ApplicationInvolvementDto {

  private final PwaApplication pwaApplication;

  private final Set<PwaContactRole> contactRoles;

  private final ConsultationInvolvementDto consultationInvolvement;

  private final boolean caseOfficerStageAndUserAssigned;

  private final boolean atLeastOneSatisfactoryVersion;

  public ApplicationInvolvementDto(PwaApplication pwaApplication,
                                   Set<PwaContactRole> contactRoles,
                                   ConsultationInvolvementDto consultationInvolvement,
                                   boolean caseOfficerStageAndUserAssigned,
                                   boolean atLeastOneSatisfactoryVersion) {
    this.pwaApplication = pwaApplication;
    this.contactRoles = contactRoles;
    this.consultationInvolvement = consultationInvolvement;
    this.caseOfficerStageAndUserAssigned = caseOfficerStageAndUserAssigned;
    this.atLeastOneSatisfactoryVersion = atLeastOneSatisfactoryVersion;
  }

  public PwaApplication getPwaApplication() {
    return pwaApplication;
  }

  public Set<PwaContactRole> getContactRoles() {
    return contactRoles;
  }

  public Optional<ConsultationInvolvementDto> getConsultationInvolvement() {
    return Optional.ofNullable(consultationInvolvement);
  }

  public boolean isCaseOfficerStageAndUserAssigned() {
    return caseOfficerStageAndUserAssigned;
  }

  public boolean hasAnyOfTheseContactRoles(PwaContactRole... roles) {
    return Arrays.stream(roles)
        .anyMatch(contactRoles::contains);
  }

  public boolean hasAnyOfTheseConsulteeRoles(ConsulteeGroupMemberRole... roles) {
    return getConsultationInvolvement()
        .map(ci -> Arrays.stream(roles).anyMatch(ci.getConsulteeRoles()::contains))
        .orElse(false);
  }

  public boolean hasAtLeastOneSatisfactoryVersion() {
    return atLeastOneSatisfactoryVersion;
  }

}
