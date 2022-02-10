package uk.co.ogauthority.pwa.service.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

@RunWith(MockitoJUnitRunner.class)
public class UserTypeServiceTest {

  private UserTypeService userTypeService;

  private AuthenticatedUserAccount user;

  @Before
  public void setUp() {
    userTypeService = new UserTypeService();
  }


  @Test(expected = IllegalStateException.class)
  public void getPriorityUserType_whenNoPriv() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.noneOf(PwaUserPrivilege.class));

    userTypeService.getPriorityUserType(user);

  }

  @Test
  public void getPriorityUserType_whenIndustryPrivOnly() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_INDUSTRY));

    assertThat(userTypeService.getPriorityUserType(user)).isEqualTo(UserType.INDUSTRY);

  }

  @Test
  public void getPriorityUserType_whenRegulatorPrivOnly() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_REGULATOR));

    assertThat(userTypeService.getPriorityUserType(user)).isEqualTo(UserType.OGA);

  }

  @Test
  public void getPriorityUserType_whenConsulteePrivOnly() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_CONSULTEE));

    assertThat(userTypeService.getPriorityUserType(user)).isEqualTo(UserType.CONSULTEE);

  }

  @Test
  public void getPriorityUserType_whenAllPrivs() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));

    assertThat(userTypeService.getPriorityUserType(user)).isEqualTo(UserType.OGA);

  }

  @Test
  public void getUserTypes_whenAllPrivs() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));

    assertThat(userTypeService.getUserTypes(user)).containsExactlyInAnyOrder(UserType.values());

  }

  @Test
  public void getUserTypes_whenNoPrivs() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.noneOf(PwaUserPrivilege.class));

    assertThat(userTypeService.getUserTypes(user)).isEmpty();

  }

}
