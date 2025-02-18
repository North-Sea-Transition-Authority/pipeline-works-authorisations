package uk.co.ogauthority.pwa.service.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;

@ExtendWith(MockitoExtension.class)
class UserTypeServiceTest {

  private UserTypeService userTypeService;

  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {
    userTypeService = new UserTypeService();
  }


  @Test
  void getPriorityUserType_whenNoPriv() {
    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.noneOf(PwaUserPrivilege.class));
    assertThrows(IllegalStateException.class, () ->

      userTypeService.getPriorityUserType(user));

  }

  @Test
  void getPriorityUserType_whenIndustryPrivOnly() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_INDUSTRY));

    assertThat(userTypeService.getPriorityUserType(user)).isEqualTo(UserType.INDUSTRY);

  }

  @Test
  void getPriorityUserType_whenRegulatorPrivOnly() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_REGULATOR));

    assertThat(userTypeService.getPriorityUserType(user)).isEqualTo(UserType.OGA);

  }

  @Test
  void getPriorityUserType_whenConsulteePrivOnly() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of(PwaUserPrivilege.PWA_CONSULTEE));

    assertThat(userTypeService.getPriorityUserType(user)).isEqualTo(UserType.CONSULTEE);

  }

  @Test
  void getPriorityUserType_whenAllPrivs() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));

    assertThat(userTypeService.getPriorityUserType(user)).isEqualTo(UserType.OGA);

  }

  @Test
  void getUserTypes_whenAllPrivs() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.allOf(PwaUserPrivilege.class));

    assertThat(userTypeService.getUserTypes(user)).containsExactlyInAnyOrder(UserType.values());

  }

  @Test
  void getUserTypes_whenNoPrivs() {

    user = new AuthenticatedUserAccount(new WebUserAccount(1), EnumSet.noneOf(PwaUserPrivilege.class));

    assertThat(userTypeService.getUserTypes(user)).isEmpty();

  }

}
