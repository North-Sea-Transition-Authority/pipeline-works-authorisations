package uk.co.ogauthority.pwa.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.service.SystemAreaAccessService;

public class SystemAreaAccessServiceTest {

  private SystemAreaAccessService systemAreaAccessService = new SystemAreaAccessService();

  @Test
  public void canAccessTeamManagement() {
    var user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE));
    assertThat(systemAreaAccessService.canAccessTeamManagement(user)).isTrue();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE, PwaUserPrivilege.PWA_REGULATOR_ADMIN));
    assertThat(systemAreaAccessService.canAccessTeamManagement(user)).isTrue();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_REGULATOR_ADMIN, PwaUserPrivilege.PWA_WORKAREA));
    assertThat(systemAreaAccessService.canAccessTeamManagement(user)).isTrue();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_ORG_ADMIN, PwaUserPrivilege.PWA_APPLICATION_DRAFT));
    assertThat(systemAreaAccessService.canAccessTeamManagement(user)).isTrue();
  }

  @Test
  public void canAccessTeamManagement_wrongPrivs() {
    var user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());
    assertThat(systemAreaAccessService.canAccessTeamManagement(user)).isFalse();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_WORKAREA));
    assertThat(systemAreaAccessService.canAccessTeamManagement(user)).isFalse();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_APPLICATION_DRAFT));
    assertThat(systemAreaAccessService.canAccessTeamManagement(user)).isFalse();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_APPLICATION_DRAFT, PwaUserPrivilege.PWA_APPLICATION_SUBMIT));
    assertThat(systemAreaAccessService.canAccessTeamManagement(user)).isFalse();
  }

  @Test
  public void canAccessWorkArea() {
    var user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_WORKAREA));
    assertThat(systemAreaAccessService.canAccessWorkArea(user)).isTrue();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE, PwaUserPrivilege.PWA_WORKAREA));
    assertThat(systemAreaAccessService.canAccessWorkArea(user)).isTrue();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_REGULATOR_ADMIN, PwaUserPrivilege.PWA_WORKAREA));
    assertThat(systemAreaAccessService.canAccessWorkArea(user)).isTrue();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_APPLICATION_DRAFT));
    assertThat(systemAreaAccessService.canAccessWorkArea(user)).isTrue();
  }

  @Test
  public void canAccessWorkArea_wrongPrivs() {
    var user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());
    assertThat(systemAreaAccessService.canAccessTeamManagement(user)).isFalse();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_REGULATOR_ADMIN));
    assertThat(systemAreaAccessService.canAccessWorkArea(user)).isFalse();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_REG_ORG_MANAGE, PwaUserPrivilege.PWA_APPLICATION_DRAFT));
    assertThat(systemAreaAccessService.canAccessWorkArea(user)).isFalse();

    user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of(PwaUserPrivilege.PWA_APPLICATION_DRAFT, PwaUserPrivilege.PWA_APPLICATION_SUBMIT));
    assertThat(systemAreaAccessService.canAccessWorkArea(user)).isFalse();
  }

}
