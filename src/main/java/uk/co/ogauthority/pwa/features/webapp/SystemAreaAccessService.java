package uk.co.ogauthority.pwa.features.webapp;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.HasTeamRoleService;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.auth.RoleGroup;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class SystemAreaAccessService {
  private static final Logger LOGGER = LoggerFactory.getLogger(SystemAreaAccessService.class);

  // TODO: Remove in PWARE-63
  public static final Set<PwaUserPrivilege> ALL_PWA_USER_PRIVILEGES = Arrays.stream(PwaUserPrivilege.values())
      .collect(Collectors.toSet());

  // TODO: Remove in PWARE-63
  public final Set<PwaUserPrivilege> validTeamManagementPrivileges = ALL_PWA_USER_PRIVILEGES;

  // TODO: Remove in PWARE-63
  public final Set<PwaUserPrivilege> validCreateOrganisationTeamPrivileges = ALL_PWA_USER_PRIVILEGES;

  private final Boolean allowStartApplication;
  private final HasTeamRoleService hasTeamRoleService;
  private final PwaContactService pwaContactService;

  @Autowired
  public SystemAreaAccessService(@Value("${pwa.features.start-application}") Boolean allowStartApplication,
                                 HasTeamRoleService hasTeamRoleService, PwaContactService pwaContactService) {
    this.allowStartApplication = allowStartApplication;
    this.hasTeamRoleService = hasTeamRoleService;
    this.pwaContactService = pwaContactService;

    LOGGER.info("allowStartApplication = {}", allowStartApplication);
  }

  /**
   * For use in WebSecurityConfig. In other instances call canAccessTeamManagement
   */
  // TODO: Remove in PWARE-63
  public String[] getValidTeamManagementGrantedAuthorities() {
    return validTeamManagementPrivileges.stream()
        .map(PwaUserPrivilege::name)
        .toArray(String[]::new);
  }

  // TODO: Remove in PWARE-63
  public String[] getValidCreateOrganisationTeamGrantedAuthorities() {
    return validCreateOrganisationTeamPrivileges.stream()
        .map(PwaUserPrivilege::name)
        .toArray(String[]::new);
  }

  public boolean canAccessTeamManagement(AuthenticatedUserAccount user) {
    return hasTeamRoleService.userIsMemberOfAnyTeam(user);
  }

  public boolean isManagement(AuthenticatedUserAccount user) {
    return hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.PWA_MANAGER));
  }

  public boolean canAccessWorkArea(AuthenticatedUserAccount user) {
    return hasTeamRoleService.userIsMemberOfAnyTeam(user) || pwaContactService.isPersonApplicationContact(user.getLinkedPerson());
  }

  public boolean canStartApplication(AuthenticatedUserAccount user) {
    return allowStartApplication
        && hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR));
  }

  public void canStartApplicationOrThrow(AuthenticatedUserAccount user) {
    if (!canStartApplication(user)) {
      throw new AccessDeniedException("User %d cannot create an application".formatted(user.getWuaId()));
    }
  }

  public boolean canAccessApplicationSearch(AuthenticatedUserAccount user) {
    return hasTeamRoleService.userHasAnyRoleInTeamTypes(user, RoleGroup.APPLICATION_SEARCH.getRolesByTeamType());
  }

  public boolean canAccessConsentSearch(AuthenticatedUserAccount user) {
    return hasTeamRoleService.userHasAnyRoleInTeamTypes(user, RoleGroup.CONSENT_SEARCH.getRolesByTeamType());
  }

  public boolean canAccessTemplateClauseManagement(AuthenticatedUserAccount user) {
    return hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.TEMPLATE_CLAUSE_MANAGER));
  }

  public boolean canAccessFeePeriodManagement(AuthenticatedUserAccount user) {
    return hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.PWA_MANAGER));
  }
}
