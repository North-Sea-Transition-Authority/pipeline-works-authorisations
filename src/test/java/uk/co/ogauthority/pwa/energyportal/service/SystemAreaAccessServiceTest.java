package uk.co.ogauthority.pwa.energyportal.service;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import org.junit.Test;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;

public class SystemAreaAccessServiceTest {

  private final AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(
      new WebUserAccount(1, PersonTestUtil.createDefaultPerson()),
      EnumSet.allOf(PwaUserPrivilege.class));

  @Test
  public void constructor_allowStartApplicationIsFalse_userHasAllPrivs() {
    var service = new SystemAreaAccessService(false);

    assertThat(service.getStartApplicationGrantedAuthorities()).isEmpty();
    assertThat(service.validStartApplicationPrivileges).isEmpty();
    assertThat(service.canStartApplication(authenticatedUserAccount)).isFalse();
  }

  @Test
  public void constructor_allowStartApplicationIsTrue_userHasAllPrivs() {
    var service = new SystemAreaAccessService(true);

    assertThat(service.getStartApplicationGrantedAuthorities()).containsExactly(PwaUserPrivilege.PWA_APPLICATION_CREATE.name());
    assertThat(service.validStartApplicationPrivileges).containsExactly(PwaUserPrivilege.PWA_APPLICATION_CREATE);
    assertThat(service.canStartApplication(authenticatedUserAccount)).isTrue();
  }

}