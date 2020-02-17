package uk.co.ogauthority.pwa.energyportal.service;

import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;

@Service
public class SystemAreaAccessService {

  public final Set<PwaUserPrivilege> validWorkAreaPrivs = Set.of(
      PwaUserPrivilege.PWA_WORKAREA);

  public final Set<PwaUserPrivilege> validTeamManagementPrivileges = Set.of(
      PwaUserPrivilege.PWA_REG_ORG_MANAGE,
      PwaUserPrivilege.PWA_REGULATOR_ADMIN,
      PwaUserPrivilege.PWA_ORG_ADMIN);

  /**
   * For use in WebSecurityConfig. In other instances call canAccessTeamManagement
   */
  public String[] getValidTeamManagementGrantedAuthorities() {
    return validTeamManagementPrivileges.stream()
        .map(PwaUserPrivilege::name)
        .toArray(String[]::new);
  }

  public boolean canAccessTeamManagement(AuthenticatedUserAccount user) {
    return user.getUserPrivileges().stream()
        .anyMatch(validTeamManagementPrivileges::contains);
  }

  /**
   * For use in WebSecurityConfig. In other instances call canAccessWorkArea
   */
  public String[] getValidWorkAreaGrantedAuthorities() {
    return validWorkAreaPrivs.stream()
        .map(PwaUserPrivilege::name)
        .toArray(String[]::new);
  }

  public boolean canAccessWorkArea(AuthenticatedUserAccount user) {
    return user.getUserPrivileges().stream()
        .anyMatch(validWorkAreaPrivs::contains);
  }


}
