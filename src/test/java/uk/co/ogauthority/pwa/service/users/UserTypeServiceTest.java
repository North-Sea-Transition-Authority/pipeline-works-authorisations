package uk.co.ogauthority.pwa.service.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

@RunWith(MockitoJUnitRunner.class)
public class UserTypeServiceTest {

  private UserTypeService userTypeService;

  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {
    userTypeService = new UserTypeService();
  }

  @Test
  public void getUserType_whenIndustryPriv() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_INDUSTRY));

    assertThat(userTypeService.getUserType(user)).isEqualTo(UserType.INDUSTRY);

  }

  @Test
  public void getUserType_whenRegulatorPriv() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_REGULATOR));

    assertThat(userTypeService.getUserType(user)).isEqualTo(UserType.OGA);

  }

  @Test
  public void getUserType_whenConsulteePriv() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_CONSULTEE));

    assertThat(userTypeService.getUserType(user)).isEqualTo(UserType.CONSULTEE);

  }

}
