package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

/**
 * Used to map permissions for a specific operation on a PWA application to the various roles in different teams
 * that are allowed that permission.
 */
public enum PwaApplicationPermission {

  SUBMIT(
      EnumSet.noneOf(PwaContactRole.class),
      EnumSet.of(PwaOrganisationRole.APPLICATION_SUBMITTER),
      EnumSet.noneOf(PwaRegulatorRole.class),
      EnumSet.noneOf(ConsulteeGroupMemberRole.class)
  ),

  EDIT(
      EnumSet.of(PwaContactRole.PREPARER),
      EnumSet.noneOf(PwaOrganisationRole.class),
      EnumSet.noneOf(PwaRegulatorRole.class),
      EnumSet.noneOf(ConsulteeGroupMemberRole.class)
  ),

  MANAGE_CONTACTS(
      EnumSet.of(PwaContactRole.ACCESS_MANAGER),
      EnumSet.of(PwaOrganisationRole.APPLICATION_SUBMITTER, PwaOrganisationRole.APPLICATION_CREATOR),
      EnumSet.noneOf(PwaRegulatorRole.class),
      EnumSet.noneOf(ConsulteeGroupMemberRole.class)
  ),

  VIEW(
      EnumSet.allOf(PwaContactRole.class),
      EnumSet.of(PwaOrganisationRole.APPLICATION_SUBMITTER),
      EnumSet.allOf(PwaRegulatorRole.class),
      EnumSet.allOf(ConsulteeGroupMemberRole.class
  ));

  private final Set<PwaContactRole> contactRoles;
  private final Set<PwaOrganisationRole> holderTeamRoles;
  private final Set<PwaRegulatorRole> regulatorRoles;
  private final Set<ConsulteeGroupMemberRole> consulteeRoles;

  PwaApplicationPermission(Set<PwaContactRole> contactRoles,
                           Set<PwaOrganisationRole> holderTeamRoles,
                           Set<PwaRegulatorRole> regulatorRoles,
                           Set<ConsulteeGroupMemberRole> consulteeRoles) {
    this.contactRoles = contactRoles;
    this.holderTeamRoles = holderTeamRoles;
    this.regulatorRoles = regulatorRoles;
    this.consulteeRoles = consulteeRoles;
  }

  public Set<PwaContactRole> getContactRoles() {
    return contactRoles;
  }

  public Set<PwaOrganisationRole> getHolderTeamRoles() {
    return holderTeamRoles;
  }

  public Set<PwaRegulatorRole> getRegulatorRoles() {
    return regulatorRoles;
  }

  public Set<ConsulteeGroupMemberRole> getConsulteeRoles() {
    return consulteeRoles;
  }

  public static Stream<PwaApplicationPermission> stream() {
    return Arrays.stream(PwaApplicationPermission.values());
  }

}
