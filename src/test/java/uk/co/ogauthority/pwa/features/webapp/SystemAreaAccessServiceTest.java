package uk.co.ogauthority.pwa.features.webapp;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.testutils.AuthTestingUtils;

@ExtendWith(MockitoExtension.class)
class SystemAreaAccessServiceTest {

  private final AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(
      new WebUserAccount(1, PersonTestUtil.createDefaultPerson()),
      EnumSet.allOf(PwaUserPrivilege.class)
  );

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private SystemAreaAccessService underTest;

  @Test
  void constructor_allowStartApplicationIsFalse_userHasAllPrivs() {
    var service = new SystemAreaAccessService(false, teamQueryService);

    assertThat(service.getStartApplicationGrantedAuthorities()).isEmpty();
    assertThat(service.validStartApplicationPrivileges).isEmpty();
    assertThat(service.canStartApplication(authenticatedUserAccount)).isFalse();
  }

  @Test
  void constructor_allowStartApplicationIsTrue_userHasAllPrivs() {
    var service = new SystemAreaAccessService(true, teamQueryService);

    assertThat(service.getStartApplicationGrantedAuthorities()).containsExactly(PwaUserPrivilege.PWA_APPLICATION_CREATE.name());
    assertThat(service.validStartApplicationPrivileges).containsExactly(PwaUserPrivilege.PWA_APPLICATION_CREATE);
    assertThat(service.canStartApplication(authenticatedUserAccount)).isTrue();
  }

  @Test
  void canAccessTeamManagement_IsMemberOfAnyTeam_ReturnsTrue() {
    when(teamQueryService.userIsMemberOfAnyTeam(authenticatedUserAccount.getWuaId())).thenReturn(true);

    boolean result = underTest.canAccessTeamManagement(authenticatedUserAccount);

    assertThat(result).isTrue();
  }

  @Test
  void canAccessTeamManagement_IsNotMemberOfAnyTeam_ReturnsFalse() {
    when(teamQueryService.userIsMemberOfAnyTeam(authenticatedUserAccount.getWuaId())).thenReturn(false);

    boolean result = underTest.canAccessTeamManagement(authenticatedUserAccount);

    assertThat(result).isFalse();
  }

  @Test
  void canAccessWorkArea() {
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_WORKAREA),
        underTest::canAccessWorkArea
    );
  }

  @Test
  void canStartApplication() {
    var service = new SystemAreaAccessService(true, teamQueryService);

    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_APPLICATION_CREATE),
        service::canStartApplication);
  }

  @Test
  void canAccessApplicationSearch(){
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_APPLICATION_SEARCH),
        underTest::canAccessApplicationSearch);
  }

  @Test
  void canAccessConsentSearch(){
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_CONSENT_SEARCH, PwaUserPrivilege.PWA_MANAGER, PwaUserPrivilege.PWA_CASE_OFFICER,
            PwaUserPrivilege.PWA_REGULATOR, PwaUserPrivilege.PWA_REG_ORG_MANAGE),
        underTest::canAccessConsentSearch);
  }

  @Test
  void canAccessDocumentTemplateManagement() {
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE),
        underTest::canAccessTemplateClauseManagement);
  }

}