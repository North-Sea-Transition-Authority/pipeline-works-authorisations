package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.users.UserTypeService;

@ExtendWith(MockitoExtension.class)
class WorkAreaContextServiceTest {

  private WorkAreaContextService workAreaContextService;

  private Person person = PersonTestUtil.createDefaultPerson();
  private WebUserAccount wua = new WebUserAccount(1, person);

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(wua, Arrays.asList(PwaUserPrivilege.values()));

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private TeamService teamService;

  @Mock
  private PwaContactService pwaContactService;

  @BeforeEach
  void setUp() {

    workAreaContextService = new WorkAreaContextService(userTypeService, teamService, pwaContactService);

  }

  @Test
  void getTabsAvailableToUser_regulatorOnly() {
    when(userTypeService.getPriorityUserTypeOrThrow(user)).thenReturn(UserType.OGA);
    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS);

  }

  @Test
  void getTabsAvailableToUser_industryOnly() {

    when(userTypeService.getPriorityUserTypeOrThrow(user)).thenReturn(UserType.INDUSTRY);

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS,
        WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS);

  }

  @Test
  void getTabsAvailableToUser_consulteeOnly() {

    when(userTypeService.getPriorityUserTypeOrThrow(user)).thenReturn(UserType.CONSULTEE);

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.OPEN_CONSULTATIONS);
  }

  @Test
  void getTabsAvailableToUser_regulatorAndConsultee() {

    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.CONSULTEE, UserType.OGA));
    when(userTypeService.getPriorityUserTypeOrThrow(user)).thenReturn(UserType.OGA);

    var tabs = workAreaContextService.getTabsAvailableToUser(user);
    assertThat(tabs).containsExactly(WorkAreaTab.OPEN_CONSULTATIONS);

  }

  @Test
  void getTabsAvailableToUser_noTabs() {

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).isEmpty();

  }

  @Test
  void getTabsAvailableToUser_filterByPwaUserPriviledge_asBuiltNotifications() {

    when(userTypeService.getPriorityUserTypeOrThrow(user)).thenReturn(UserType.OGA);
    when(teamService.getAllUserPrivilegesForPerson(user.getLinkedPerson()))
        .thenReturn(Set.of(PwaUserPrivilege.PWA_ASBUILT_WORKAREA));

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS,
        WorkAreaTab.AS_BUILT_NOTIFICATIONS);

  }

  @Test
  void getTabsAvailableToUser_whenNoUserType_filterByPwaUserPrivilege_asBuiltNotifications() {
    when(userTypeService.getUserTypes(any())).thenReturn(Set.of());
    when(userTypeService.getPriorityUserTypeOrThrow(user)).thenReturn(null);
    when(teamService.getAllUserPrivilegesForPerson(user.getLinkedPerson()))
        .thenReturn(Set.of(PwaUserPrivilege.PWA_ASBUILT_WORKAREA));

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.AS_BUILT_NOTIFICATIONS);
  }

  @Test
  void getTabsAvailableToUser_whenIndustry_allPwaUserPrivileges_assertIndustryAndAsBuilt() {
    when(userTypeService.getPriorityUserTypeOrThrow(any()))
        .thenReturn(UserType.INDUSTRY);
    when(teamService.getAllUserPrivilegesForPerson(user.getLinkedPerson()))
        .thenReturn(Set.of(PwaUserPrivilege.values()));

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs)
        .containsExactly(
            WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS,
            WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS,
            WorkAreaTab.AS_BUILT_NOTIFICATIONS);
  }

  @Test
  void createWorkAreaContext_pwaManagerPriv() {
    user = new AuthenticatedUserAccount(wua, List.of(PwaUserPrivilege.PWA_MANAGER));

    var context = workAreaContextService.createWorkAreaContext(user);

    assertThat(context.getApplicationEventSubscriberTypes())
        .containsExactlyInAnyOrder(WorkAreaUserType.PWA_MANAGER);

  }

  @Test
  void createWorkAreaContext_caseOfficerPriv() {
    user = new AuthenticatedUserAccount(wua, List.of(PwaUserPrivilege.PWA_CASE_OFFICER));

    var context = workAreaContextService.createWorkAreaContext(user);

    assertThat(context.getApplicationEventSubscriberTypes())
        .containsExactlyInAnyOrder(WorkAreaUserType.CASE_OFFICER);

  }

  @Test
  void createWorkAreaContext_isAppContact() {

    user = AuthenticatedUserAccountTestUtil.createNoPrivUserAccount(10);//    when(pwaContactService.isPersonApplicationContact(person)).thenReturn(true);

    when(pwaContactService.isPersonApplicationContact(user.getLinkedPerson())).thenReturn(true);
    var context = workAreaContextService.createWorkAreaContext(user);

    assertThat(context.getApplicationEventSubscriberTypes())
        .containsExactlyInAnyOrder(WorkAreaUserType.APPLICATION_CONTACT);

  }

  @Test
  void createWorkAreaContext_isAppContact_andPwaManager_andCaseOfficer() {
    user = new AuthenticatedUserAccount(wua, List.of(PwaUserPrivilege.PWA_CASE_OFFICER, PwaUserPrivilege.PWA_MANAGER));
    when(pwaContactService.isPersonApplicationContact(person)).thenReturn(true);

    var context = workAreaContextService.createWorkAreaContext(user);

    assertThat(context.getApplicationEventSubscriberTypes())
        .containsExactlyInAnyOrder(
            WorkAreaUserType.APPLICATION_CONTACT,
            WorkAreaUserType.PWA_MANAGER,
            WorkAreaUserType.CASE_OFFICER
        );
  }
}