package uk.co.ogauthority.pwa.service.asbuilt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaTestUtil;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsBuiltNotificationAuthServiceTest {


  @Mock
  private AsBuiltNotificationGroupService asBuiltNotificationGroupService;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  @InjectMocks
  private AsBuiltNotificationAuthService asBuiltNotificationAuthService;

  private final AuthenticatedUserAccount regulator = AuthenticatedUserAccountTestUtil.createAllPrivWebUserAccount(1,
      PersonTestUtil.createPersonFrom(new PersonId(1)));

  private final AuthenticatedUserAccount industryAsBuiltSubmitter = AuthenticatedUserAccountTestUtil.createAllPrivWebUserAccount(2,
      PersonTestUtil.createPersonFrom(new PersonId(2)));

  private final AuthenticatedUserAccount industryUnauthorized = AuthenticatedUserAccountTestUtil.createAllPrivWebUserAccount(3,
      PersonTestUtil.createPersonFrom(new PersonId(3)));

  private final MasterPwa masterPwa = MasterPwaTestUtil.create();

  private static final int NOTIFICATION_GROUP_ID = 10;


  @BeforeEach
  void setup() {

    when(asBuiltNotificationGroupService.getMasterPwaForAsBuiltNotificationGroup(NOTIFICATION_GROUP_ID)).thenReturn(masterPwa);
    when(teamQueryService.userHasStaticRole(1L, TeamType.REGULATOR, Role.AS_BUILT_NOTIFICATION_ADMIN)).thenReturn(true);
    when(pwaHolderTeamService.isPersonInHolderTeamWithRole(masterPwa, industryAsBuiltSubmitter.getLinkedPerson(), PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER))
        .thenReturn(true);
  }

  @Test
  void canPersonAccessAsbuiltNotificationGroup_personIsRegulator_canAccess() {
    assertTrue(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(regulator, NOTIFICATION_GROUP_ID));
  }

  @Test
  void canPersonAccessAsbuiltNotificationGroup_personIsAsBuiltSubmitter_canAccess() {
    assertTrue(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(industryAsBuiltSubmitter, NOTIFICATION_GROUP_ID));
  }

  @Test
  void canPersonAccessAsbuiltNotificationGroup_personIsOtherIndustryUser_cannotAccess() {
    assertFalse(asBuiltNotificationAuthService.canPersonAccessAsbuiltNotificationGroup(industryUnauthorized, NOTIFICATION_GROUP_ID));
  }

  @Test
  void isUserAsBuiltNotificationAdmin_industryUser() {
    assertFalse(asBuiltNotificationAuthService.isUserAsBuiltNotificationAdmin(industryAsBuiltSubmitter));
  }

  @Test
  void isUserAsBuiltNotificationAdmin_OgaUser() {
    assertTrue(asBuiltNotificationAuthService.isUserAsBuiltNotificationAdmin(regulator));
  }

}
