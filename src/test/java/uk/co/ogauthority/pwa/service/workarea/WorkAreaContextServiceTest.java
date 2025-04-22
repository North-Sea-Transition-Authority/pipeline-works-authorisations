package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.auth.HasTeamRoleService;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.auth.RoleGroup;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class WorkAreaContextServiceTest {

  private final Person person = PersonTestUtil.createDefaultPerson();
  private final WebUserAccount wua = new WebUserAccount(1, person);

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(wua, Set.of(PwaUserPrivilege.PWA_ACCESS));

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private HasTeamRoleService hasTeamRoleService;

  @InjectMocks
  private WorkAreaContextService workAreaContextService;

  @Test
  void getTabsAvailableToUser_regulatorOnly() {
    when(userTypeService.getPriorityUserType(user)).thenReturn(Optional.of(UserType.OGA));
    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS);

  }

  @Test
  void getTabsAvailableToUser_industryOnly() {

    when(userTypeService.getPriorityUserType(user)).thenReturn(Optional.of(UserType.INDUSTRY));

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS,
        WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS);

  }

  @Test
  void getTabsAvailableToUser_consulteeOnly() {

    when(userTypeService.getPriorityUserType(user)).thenReturn(Optional.of(UserType.CONSULTEE));

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.OPEN_CONSULTATIONS);
  }

  @Test
  void getTabsAvailableToUser_regulatorAndConsultee() {

    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.CONSULTEE, UserType.OGA));

    var tabs = workAreaContextService.getTabsAvailableToUser(user);
    assertThat(tabs).containsExactly(WorkAreaTab.OPEN_CONSULTATIONS);

  }

  @Test
  void getTabsAvailableToUser_noTabs() {

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).isEmpty();

  }

  @Test
  void getTabsAvailableToUser_filterByRoleGroup_asBuiltNotifications() {

    when(userTypeService.getPriorityUserType(user)).thenReturn(Optional.of(UserType.OGA));
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(user, RoleGroup.ASBUILT_WORKAREA.getRolesByTeamType()))
        .thenReturn(true);

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS,
        WorkAreaTab.AS_BUILT_NOTIFICATIONS);

  }

  @Test
  void getTabsAvailableToUser_whenNoUserType_filterByRoleGroup_asBuiltNotifications() {
    when(userTypeService.getUserTypes(any())).thenReturn(Set.of());
    when(userTypeService.getPriorityUserType(user)).thenReturn(Optional.empty());
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(user, RoleGroup.ASBUILT_WORKAREA.getRolesByTeamType()))
        .thenReturn(true);

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs).containsExactly(WorkAreaTab.AS_BUILT_NOTIFICATIONS);
  }

  @Test
  void getTabsAvailableToUser_whenIndustry_filterByRoleGroup_assertIndustryAndAsBuilt() {
    when(userTypeService.getPriorityUserType(any()))
        .thenReturn(Optional.of(UserType.INDUSTRY));
    when(hasTeamRoleService.userHasAnyRoleInTeamTypes(user, RoleGroup.ASBUILT_WORKAREA.getRolesByTeamType()))
        .thenReturn(true);

    var tabs = workAreaContextService.getTabsAvailableToUser(user);

    assertThat(tabs)
        .containsExactly(
            WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS,
            WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS,
            WorkAreaTab.AS_BUILT_NOTIFICATIONS);
  }

  @Test
  void createWorkAreaContext_pwaManagerPriv() {
    when(hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.PWA_MANAGER)))
        .thenReturn(true);

    var context = workAreaContextService.createWorkAreaContext(user);

    assertThat(context.getApplicationEventSubscriberTypes())
        .containsExactlyInAnyOrder(WorkAreaUserType.PWA_MANAGER);

  }

  @Test
  void createWorkAreaContext_caseOfficerPriv() {
    when(hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.PWA_MANAGER)))
        .thenReturn(false);
    when(hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.CASE_OFFICER)))
        .thenReturn(true);

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
    when(pwaContactService.isPersonApplicationContact(person)).thenReturn(true);
    when(hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.PWA_MANAGER)))
        .thenReturn(true);
    when(hasTeamRoleService.userHasAnyRoleInTeamType(user, TeamType.REGULATOR, Set.of(Role.CASE_OFFICER)))
        .thenReturn(true);

    var context = workAreaContextService.createWorkAreaContext(user);

    assertThat(context.getApplicationEventSubscriberTypes())
        .containsExactlyInAnyOrder(
            WorkAreaUserType.APPLICATION_CONTACT,
            WorkAreaUserType.PWA_MANAGER,
            WorkAreaUserType.CASE_OFFICER
        );
  }
}