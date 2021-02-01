package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

/**
 * Used to map permissions for a specific operation on a PWA application to the contact roles (or holder team roles)
 * that have each permission.
 */
public enum PwaApplicationPermission {

  SUBMIT(Set.of(), Set.of(PwaOrganisationRole.APPLICATION_SUBMITTER)),

  EDIT(Set.of(PwaContactRole.PREPARER), Set.of()),

  MANAGE_CONTACTS(Set.of(PwaContactRole.ACCESS_MANAGER), Set.of()),

  VIEW(EnumSet.allOf(PwaContactRole.class), Set.of(PwaOrganisationRole.APPLICATION_SUBMITTER));

  private final Set<PwaContactRole> contactRoles;
  private final Set<PwaOrganisationRole> holderTeamRoles;

  PwaApplicationPermission(Set<PwaContactRole> contactRoles,
                           Set<PwaOrganisationRole> holderTeamRoles) {
    this.contactRoles = contactRoles;
    this.holderTeamRoles = holderTeamRoles;
  }

  public Set<PwaContactRole> getContactRoles() {
    return contactRoles;
  }

  public Set<PwaOrganisationRole> getHolderTeamRoles() {
    return holderTeamRoles;
  }

  public static Stream<PwaApplicationPermission> stream() {
    return Arrays.stream(PwaApplicationPermission.values());
  }

}
