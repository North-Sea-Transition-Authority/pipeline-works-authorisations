package uk.co.ogauthority.pwa.features.application.authorisation.involvement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDto;
import uk.co.ogauthority.pwa.service.enums.appprocessing.appinvolvement.OpenConsentReview;
import uk.co.ogauthority.pwa.teams.Role;

public class ApplicationInvolvementDto {

  private final PwaApplication pwaApplication;

  private final Set<PwaContactRole> contactRoles;

  private final ConsultationInvolvementDto consultationInvolvement;

  private final boolean userIsAssignedCaseOfficer;

  private final boolean pwaManagerStage;

  private final boolean atLeastOneSatisfactoryVersion;

  private final boolean userInHolderTeam;
  private final boolean userInAppContactTeam;

  private final boolean userIsIndustryOnly;

  private final Set<Role> holderTeamRoles;

  private final OpenConsentReview openConsentReview;

  public ApplicationInvolvementDto(PwaApplication pwaApplication,
                                   Set<PwaContactRole> contactRoles,
                                   ConsultationInvolvementDto consultationInvolvement,
                                   boolean userIsAssignedCaseOfficer,
                                   boolean pwaManagerStage,
                                   boolean atLeastOneSatisfactoryVersion,
                                   Set<Role> holderTeamRoles,
                                   boolean userIsIndustryOnly,
                                   OpenConsentReview openConsentReview) {
    this.pwaApplication = pwaApplication;
    this.contactRoles = contactRoles;
    this.consultationInvolvement = consultationInvolvement;
    this.userIsAssignedCaseOfficer = userIsAssignedCaseOfficer;
    this.pwaManagerStage = pwaManagerStage;
    this.atLeastOneSatisfactoryVersion = atLeastOneSatisfactoryVersion;
    this.userInHolderTeam = !holderTeamRoles.isEmpty();
    this.userInAppContactTeam = !contactRoles.isEmpty();
    this.holderTeamRoles = holderTeamRoles;
    this.userIsIndustryOnly = userIsIndustryOnly;
    this.openConsentReview = openConsentReview;
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

  public boolean isUserAssignedCaseOfficer() {
    return userIsAssignedCaseOfficer;
  }

  public boolean isPwaManagerStage() {
    return pwaManagerStage;
  }

  public boolean hasAnyOfTheseContactRoles(PwaContactRole... roles) {
    return Arrays.stream(roles)
        .anyMatch(contactRoles::contains);
  }

  public boolean hasAnyOfTheseHolderRoles(Role... roles) {
    return hasAnyOfTheseHolderRoles(Arrays.asList(roles));
  }

  public boolean hasAnyOfTheseHolderRoles(Collection<Role> roles) {
    return roles.stream()
        .anyMatch(holderTeamRoles::contains);
  }

  public boolean hasAnyOfTheseConsulteeRoles(Role... roles) {
    return getConsultationInvolvement()
        .map(ci -> Arrays.stream(roles).anyMatch(ci.getConsulteeRoles()::contains))
        .orElse(false);
  }

  public boolean hasAtLeastOneSatisfactoryVersion() {
    return atLeastOneSatisfactoryVersion;
  }

  public boolean isUserInHolderTeam() {
    return userInHolderTeam;
  }

  public OpenConsentReview getOpenConsentReview() {
    return openConsentReview;
  }

  public boolean isUserInAppContactTeam() {
    return userInAppContactTeam;
  }

  public boolean hasOnlyIndustryInvolvement() {
    return userIsIndustryOnly;
  }

}
