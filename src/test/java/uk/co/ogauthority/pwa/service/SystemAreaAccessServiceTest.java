package uk.co.ogauthority.pwa.service;

import java.util.Set;
import org.junit.Test;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.service.SystemAreaAccessService;
import uk.co.ogauthority.pwa.util.AuthTestingUtils;

public class SystemAreaAccessServiceTest {

  private SystemAreaAccessService systemAreaAccessService = new SystemAreaAccessService();

  @Test
  public void canAccessTeamManagement() {
    AuthTestingUtils.testPrivilegeBasedAuthenticationFunction(
        Set.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE, PwaUserPrivilege.PWA_REGULATOR_ADMIN, PwaUserPrivilege.PWA_ORG_ADMIN),
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

}
