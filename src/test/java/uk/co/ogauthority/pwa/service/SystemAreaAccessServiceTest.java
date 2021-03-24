package uk.co.ogauthority.pwa.service;

import java.util.Set;
import org.junit.Test;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.service.SystemAreaAccessService;
import uk.co.ogauthority.pwa.testutils.AuthTestingUtils;

public class SystemAreaAccessServiceTest {

  private final SystemAreaAccessService systemAreaAccessService = new SystemAreaAccessService();

  @Test
  public void canAccessTeamManagement() {
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE, PwaUserPrivilege.PWA_REGULATOR_ADMIN, PwaUserPrivilege.PWA_ORG_ADMIN, PwaUserPrivilege.PWA_CONSULTEE_GROUP_ADMIN),
        systemAreaAccessService::canAccessTeamManagement
    );
  }

  @Test
  public void canAccessWorkArea() {
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_WORKAREA),
        systemAreaAccessService::canAccessWorkArea
    );
  }

  @Test
  public void canStartApplication() {
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_APPLICATION_CREATE),
        systemAreaAccessService::canStartApplication);
  }

  @Test
  public void canAccessApplicationSearch(){
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_APPLICATION_SEARCH),
        systemAreaAccessService::canAccessApplicationSearch);
  }

  @Test
  public void canAccessConsentSearch(){
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_CONSENT_SEARCH, PwaUserPrivilege.PWA_MANAGER, PwaUserPrivilege.PWA_CASE_OFFICER),
        systemAreaAccessService::canAccessConsentSearch);

  }

}
