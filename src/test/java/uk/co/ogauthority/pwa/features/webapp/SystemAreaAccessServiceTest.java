package uk.co.ogauthority.pwa.features.webapp;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.HasTeamRoleService;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.auth.RoleGroup;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class SystemAreaAccessServiceTest {

  private final AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(
      new WebUserAccount(1, PersonTestUtil.createDefaultPerson()),
      EnumSet.allOf(PwaUserPrivilege.class)
  );

  @Mock
  private HasTeamRoleService hasTeamRoleService;

  @Mock
  private PwaContactService pwaContactService;

  @InjectMocks
  private SystemAreaAccessService underTest;

  @Test
  void canAccessTeamManagement_IsMemberOfAnyTeam_ReturnsTrue() {
    when(hasTeamRoleService.userIsMemberOfAnyTeam(authenticatedUserAccount)).thenReturn(true);

    boolean result = underTest.canAccessTeamManagement(authenticatedUserAccount);

    assertThat(result).isTrue();
  }

  @Test
  void canAccessTeamManagement_IsNotMemberOfAnyTeam_ReturnsFalse() {
    when(hasTeamRoleService.userIsMemberOfAnyTeam(authenticatedUserAccount)).thenReturn(false);

    boolean result = underTest.canAccessTeamManagement(authenticatedUserAccount);

    assertThat(result).isFalse();
  }

  @Test
  void canAccessWorkArea_UserIsMemberOfAnyTeam() {
    when(hasTeamRoleService.userIsMemberOfAnyTeam(authenticatedUserAccount))
        .thenReturn(true);

    boolean result = underTest.canAccessWorkArea(authenticatedUserAccount);

    assertThat(result).isTrue();
  }

  @Test
  void canAccessWorkArea_PersonIsApplicationContact() {
    when(pwaContactService.isPersonApplicationContact(authenticatedUserAccount.getLinkedPerson()))
        .thenReturn(true);

    boolean result = underTest.canAccessWorkArea(authenticatedUserAccount);

    assertThat(result).isTrue();
  }

  @Test
  void canStartApplication_IsApplicationCreatorAndCanStartApplicationFlagIsTrue_ReturnsTrue() {
    var service = new SystemAreaAccessService(true, hasTeamRoleService, pwaContactService);

    when(hasTeamRoleService.userHasAnyRoleInTeamType(authenticatedUserAccount, TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))).thenReturn(true);

    boolean result = service.canStartApplication(authenticatedUserAccount);

    assertThat(result).isTrue();
  }

  @Test
  void canStartApplication_IsNotApplicationCreatorAndCanStartApplicationFlagIsTrue_ReturnsFalse() {
    var service = new SystemAreaAccessService(true, hasTeamRoleService, pwaContactService);

    when(hasTeamRoleService.userHasAnyRoleInTeamType(authenticatedUserAccount, TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))).thenReturn(false);

    boolean result = service.canStartApplication(authenticatedUserAccount);

    assertThat(result).isFalse();
  }

  @Test
  void canStartApplication_IsApplicationCreatorButCanStartApplicationFlagIsFalse_ReturnsFalse() {
    var service = new SystemAreaAccessService(false, hasTeamRoleService, pwaContactService);

    lenient().when(hasTeamRoleService.userHasAnyRoleInTeamType(authenticatedUserAccount, TeamType.ORGANISATION, Set.of(Role.APPLICATION_CREATOR))).thenReturn(true);

    boolean result = service.canStartApplication(authenticatedUserAccount);

    assertThat(result).isFalse();
  }

  @Test
  void canAccessApplicationSearch(){
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(authenticatedUserAccount, RoleGroup.APPLICATION_SEARCH.getRolesByTeamType()))
        .thenReturn(true);

    boolean result = underTest.canAccessApplicationSearch(authenticatedUserAccount);

    assertThat(result).isTrue();
  }

  @Test
  void canAccessConsentSearch(){
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(authenticatedUserAccount, RoleGroup.CONSENT_SEARCH.getRolesByTeamType()))
        .thenReturn(true);

    boolean result = underTest.canAccessConsentSearch(authenticatedUserAccount);

    assertThat(result).isTrue();
  }

  @Test
  void canAccessTemplateClauseManagement(){
    when(hasTeamRoleService.userHasAnyRoleInTeamType(authenticatedUserAccount, TeamType.REGULATOR, Set.of(Role.TEMPLATE_CLAUSE_MANAGER)))
        .thenReturn(true);

    boolean result = underTest.canAccessTemplateClauseManagement(authenticatedUserAccount);

    assertThat(result).isTrue();
  }
}