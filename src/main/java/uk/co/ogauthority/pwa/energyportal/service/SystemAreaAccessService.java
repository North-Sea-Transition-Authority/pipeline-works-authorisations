package uk.co.ogauthority.pwa.energyportal.service;

import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;

@Service
public class SystemAreaAccessService {

  public final Set<PwaUserPrivilege> validWorkAreaPrivs = EnumSet.of(
      PwaUserPrivilege.PWA_WORKAREA);

  public final Set<PwaUserPrivilege> validTeamManagementPrivileges = EnumSet.of(
      PwaUserPrivilege.PWA_REG_ORG_MANAGE,
      PwaUserPrivilege.PWA_REGULATOR_ADMIN,
      PwaUserPrivilege.PWA_ORG_ADMIN,
      PwaUserPrivilege.PWA_CONSULTEE_GROUP_ADMIN);

  public final Set<PwaUserPrivilege> validApplicationSearchPrivileges = EnumSet.of(
      PwaUserPrivilege.PWA_APPLICATION_SEARCH);

  public final Set<PwaUserPrivilege> validStartApplicationPrivileges = Set.of(PwaUserPrivilege.PWA_APPLICATION_CREATE);

  public final Set<PwaUserPrivilege> validConsentSearchPrivileges = EnumSet.of(PwaUserPrivilege.PWA_CONSENT_SEARCH);

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


  /**
   * For use in WebSecurityConfig. In other instances call canStartApplication
   */
  public String[] getStartApplicationGrantedAuthorities() {
    return validStartApplicationPrivileges.stream()
        .map(PwaUserPrivilege::name)
        .toArray(String[]::new);
  }

  public boolean canStartApplication(AuthenticatedUserAccount user) {
    return user.getUserPrivileges().stream()
        .anyMatch(validStartApplicationPrivileges::contains);
  }

  /**
   * For use in WebSecurityConfig. In other instances call canAccessApplicationSearch
   */
  public String[] getValidApplicationSearchGrantedAuthorities() {
    return validApplicationSearchPrivileges.stream()
        .map(PwaUserPrivilege::name)
        .toArray(String[]::new);
  }

  public boolean canAccessApplicationSearch(AuthenticatedUserAccount user) {
    return user.getUserPrivileges().stream()
        .anyMatch(validApplicationSearchPrivileges::contains);
  }

  /**
   * For use in WebSecurityConfig. In other instances call canAccessConsentSearch
   */
  public String[] getValidConsentSearchGrantedAuthorities() {
    return validConsentSearchPrivileges.stream()
        .map(PwaUserPrivilege::name)
        .toArray(String[]::new);
  }

  public boolean canAccessConsentSearch(AuthenticatedUserAccount user) {
    return user.getUserPrivileges().stream()
        .anyMatch(validConsentSearchPrivileges::contains);
  }

}
