package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

/**
 * Used to map permissions for a specific operation on a PWA application to the contact roles that have each permission.
 */
public enum PwaApplicationPermission {

  EDIT(Set.of(PwaContactRole.SUBMITTER, PwaContactRole.PREPARER)),

  SUBMIT(Set.of(PwaContactRole.SUBMITTER)),

  MANAGE_CONTACTS(Set.of(PwaContactRole.ACCESS_MANAGER)),

  VIEW(PwaContactRole.stream().collect(Collectors.toSet()));

  private Set<PwaContactRole> roles;

  PwaApplicationPermission(Set<PwaContactRole> roles) {
    this.roles = roles;
  }

  public Set<PwaContactRole> getRoles() {
    return roles;
  }

  public void setRoles(Set<PwaContactRole> roles) {
    this.roles = roles;
  }
}
