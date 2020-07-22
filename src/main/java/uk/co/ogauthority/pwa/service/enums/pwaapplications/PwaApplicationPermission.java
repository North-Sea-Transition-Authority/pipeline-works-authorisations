package uk.co.ogauthority.pwa.service.enums.pwaapplications;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;

/**
 * Used to map permissions for a specific operation on a PWA application to the contact roles that have each permission.
 */
public enum PwaApplicationPermission {

  SUBMIT(Set.of(PwaContactRole.PREPARER)),

  EDIT(Set.of(PwaContactRole.PREPARER)),

  MANAGE_CONTACTS(Set.of(PwaContactRole.ACCESS_MANAGER)),

  VIEW(EnumSet.allOf(PwaContactRole.class));

  private Set<PwaContactRole> roles;

  PwaApplicationPermission(Set<PwaContactRole> roles) {
    this.roles = roles;
  }

  public Set<PwaContactRole> getRoles() {
    return roles;
  }

  public static Stream<PwaApplicationPermission> stream() {
    return Arrays.stream(PwaApplicationPermission.values());
  }
}
