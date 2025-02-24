package uk.co.ogauthority.pwa.service.asbuilt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaTestUtil;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsBuiltNotificationAuthServiceTest {

  private AsBuiltNotificationAuthService asBuiltNotificationAuthService;

  @Mock
  private AsBuiltNotificationGroupService asBuiltNotificationGroupService;

  @Mock
  private PwaTeamService pwaTeamService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;

  private final Person regulator = PersonTestUtil.createPersonFrom(new PersonId(1));
  private final Person industryAsBuiltSubmitter = PersonTestUtil.createPersonFrom(new PersonId(2));
  private final Person industryUnauthorized = PersonTestUtil.createPersonFrom(new PersonId(3));

  private final MasterPwa masterPwa = MasterPwaTestUtil.create();

  private static final int NOTIFICATION_GROUP_ID = 10;


  @BeforeEach
  void setup() {
    asBuiltNotificationAuthService = new AsBuiltNotificationAuthService(asBuiltNotificationGroupService, pwaTeamService, pwaHolderTeamService);

    when(asBuiltNotificationGroupService.getMasterPwaForAsBuiltNotificationGroup(NOTIFICATION_GROUP_ID)).thenReturn(masterPwa);
    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.AS_BUILT_NOTIFICATION_ADMIN)).thenReturn(Set.of(regulator));
    when(pwaHolderTeamService.isPersonInHolderTeamWithRole(masterPwa, industryAsBuiltSubmitter, PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER))
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
  void isPersonAsBuiltNotificationAdmin_industryUser() {
    assertFalse(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(industryAsBuiltSubmitter));
  }

  @Test
  void isPersonAsBuiltNotificationAdmin_OgaUser() {
    assertTrue(asBuiltNotificationAuthService.isPersonAsBuiltNotificationAdmin(regulator));
  }

}
