package uk.co.ogauthority.pwa.features.webapp;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.testutils.AuthTestingUtils;

class SystemAreaAccessServiceTest {

  private final AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(
      new WebUserAccount(1, PersonTestUtil.createDefaultPerson()),
      EnumSet.allOf(PwaUserPrivilege.class));


  private final SystemAreaAccessService systemAreaAccessService = new SystemAreaAccessService(true);

  @Test
  void constructor_allowStartApplicationIsFalse_userHasAllPrivs() {
    var service = new SystemAreaAccessService(false);

    assertThat(service.getStartApplicationGrantedAuthorities()).isEmpty();
    assertThat(service.validStartApplicationPrivileges).isEmpty();
    assertThat(service.canStartApplication(authenticatedUserAccount)).isFalse();
  }

  @Test
  void constructor_allowStartApplicationIsTrue_userHasAllPrivs() {
    var service = new SystemAreaAccessService(true);

    assertThat(service.getStartApplicationGrantedAuthorities()).containsExactly(PwaUserPrivilege.PWA_APPLICATION_CREATE.name());
    assertThat(service.validStartApplicationPrivileges).containsExactly(PwaUserPrivilege.PWA_APPLICATION_CREATE);
    assertThat(service.canStartApplication(authenticatedUserAccount)).isTrue();
  }

  @Test
  void canAccessTeamManagement() {
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE, PwaUserPrivilege.PWA_REGULATOR_ADMIN, PwaUserPrivilege.PWA_ORG_ADMIN, PwaUserPrivilege.PWA_CONSULTEE_GROUP_ADMIN),
        systemAreaAccessService::canAccessTeamManagement
    );
  }

  @Test
  void canAccessWorkArea() {
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_WORKAREA),
        systemAreaAccessService::canAccessWorkArea
    );
  }

  @Test
  void canStartApplication() {
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_APPLICATION_CREATE),
        systemAreaAccessService::canStartApplication);
  }

  @Test
  void canAccessApplicationSearch(){
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_APPLICATION_SEARCH),
        systemAreaAccessService::canAccessApplicationSearch);
  }

  @Test
  void canAccessConsentSearch(){
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_CONSENT_SEARCH, PwaUserPrivilege.PWA_MANAGER, PwaUserPrivilege.PWA_CASE_OFFICER,
            PwaUserPrivilege.PWA_REGULATOR, PwaUserPrivilege.PWA_REG_ORG_MANAGE),
        systemAreaAccessService::canAccessConsentSearch);
  }

  @Test
  void canAccessDocumentTemplateManagement() {
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_TEMPLATE_CLAUSE_MANAGE),
        systemAreaAccessService::canAccessTemplateClauseManagement);
  }

}