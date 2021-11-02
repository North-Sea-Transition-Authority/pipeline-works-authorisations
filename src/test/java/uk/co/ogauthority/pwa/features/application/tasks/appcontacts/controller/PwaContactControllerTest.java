package uk.co.ogauthority.pwa.features.application.tasks.appcontacts.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.AddPwaContactFormValidator;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teammanagement.TeamMemberView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.ApplicationBreadcrumbService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaHolderService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationEndpointTestBuilder;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PwaContactController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PwaContactControllerTest extends PwaApplicationContextAbstractControllerTest {

  @SpyBean
  private ApplicationBreadcrumbService applicationBreadcrumbService;

  @MockBean
  private TeamManagementService teamManagementService;

  @MockBean
  private AddPwaContactFormValidator addPwaContactFormValidator;

  @MockBean
  private PwaHolderService pwaHolderService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), Set.of());

  private PwaApplicationDetail detail;
  private PwaApplicationEndpointTestBuilder manageAndEditEndpointTester, manageEndpointTester;

  @Before
  public void setUp() {

    when(teamManagementService.getPerson(anyInt())).thenReturn(user.getLinkedPerson());
    var teamMemberView = new TeamMemberView(user.getLinkedPerson(), null, null, Set.of());
    when(pwaContactService.getTeamMemberView(any(), any())).thenReturn(teamMemberView);

    manageAndEditEndpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.DEPOSIT_CONSENT,
            PwaApplicationType.HUOO_VARIATION,
            PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.MANAGE_CONTACTS, PwaApplicationPermission.EDIT)
        .setAllowedStatuses(ApplicationState.IN_PROGRESS);

    manageEndpointTester = new PwaApplicationEndpointTestBuilder(mockMvc, pwaApplicationPermissionService, pwaApplicationDetailService)
        .setAllowedTypes(
            PwaApplicationType.INITIAL,
            PwaApplicationType.CAT_1_VARIATION,
            PwaApplicationType.CAT_2_VARIATION,
            PwaApplicationType.DECOMMISSIONING,
            PwaApplicationType.DEPOSIT_CONSENT,
            PwaApplicationType.HUOO_VARIATION,
            PwaApplicationType.OPTIONS_VARIATION)
        .setAllowedPermissions(PwaApplicationPermission.MANAGE_CONTACTS)
        .setAllowedStatuses(ApplicationState.IN_PROGRESS);

  }

  @Test
  public void renderContactsScreen_appTypeSmokeTest() {

    manageAndEditEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderContactsScreen(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    manageAndEditEndpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderContactsScreen_permissionSmokeTest() {

    manageAndEditEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderContactsScreen(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    manageAndEditEndpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderContactsScreen_appStatusSmokeTest() {

    manageAndEditEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderContactsScreen(type, applicationDetail.getMasterPwaApplicationId(), null, null)));

    manageAndEditEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderAddContact_appTypeSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderAddContact(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    manageEndpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddContact_permissionSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderAddContact(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    manageEndpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderAddContact_appStatusSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderAddContact(type, applicationDetail.getMasterPwaApplicationId(), null, null, null)));

    manageEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void addContact_appTypeSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .addContact(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null)));

    manageEndpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void addContact_permissionSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .addContact(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null)));

    manageEndpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void addContact_appStatusSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .addContact(type, applicationDetail.getMasterPwaApplicationId(), null, null, null, null)));

    manageEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderContactRolesScreen_appTypeSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderContactRolesScreen(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null)));

    manageEndpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderContactRolesScreen_permissionSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderContactRolesScreen(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null)));

    manageEndpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderContactRolesScreen_appStatusSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderContactRolesScreen(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null, null)));

    manageEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void updateContactRoles_appTypeSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .updateContactRoles(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null, null, null)));

    manageEndpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void updateContactRoles_permissionSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .updateContactRoles(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null, null, null)));

    manageEndpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void updateContactRoles_appStatusSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .updateContactRoles(type, applicationDetail.getMasterPwaApplicationId(), 1, null, null, null, null)));

    manageEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void renderRemoveContactScreen_appTypeSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderRemoveContactScreen(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    manageEndpointTester.performAppTypeChecks(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemoveContactScreen_permissionSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderRemoveContactScreen(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    manageEndpointTester.performAppPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void renderRemoveContactScreen_appStatusSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .renderRemoveContactScreen(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    manageEndpointTester.performAppStatusChecks(status().isOk(), status().isNotFound());

  }

  @Test
  public void removeContact_appTypeSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .removeContact(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    manageEndpointTester.performAppTypeChecks(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void removeContact_permissionSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .removeContact(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    manageEndpointTester.performAppPermissionCheck(status().is3xxRedirection(), status().isForbidden());

  }

  @Test
  public void removeContact_appStatusSmokeTest() {

    manageEndpointTester.setRequestMethod(HttpMethod.POST)
        .setEndpointUrlProducer((applicationDetail, type) ->
            ReverseRouter.route(on(PwaContactController.class)
                .removeContact(type, applicationDetail.getMasterPwaApplicationId(), null, 1, null)));

    manageEndpointTester.performAppStatusChecks(status().is3xxRedirection(), status().isNotFound());

  }

  @Test
  public void renderContactsScreen_holderNamesNotDuplicated() throws Exception {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(detail);
    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(Set.of(PwaApplicationPermission.MANAGE_CONTACTS));

    var orgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "ORGGRP", "OG");

    when(pwaHolderService.getPwaHolderOrgGroups(any(MasterPwa.class))).thenReturn(Set.of(orgGroup));

    mockMvc.perform(get(ReverseRouter.route(on(PwaContactController.class)
        .renderContactsScreen(PwaApplicationType.INITIAL, 1, null, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("orgGroupHolders", Set.of("ORGGRP")));

  }

  @Test
  public void renderContactsScreen_whenUserHasManageContactPrivOnly_andApplicationCanBeEdited() throws Exception {

    for(PwaApplicationStatus status: ApplicationState.INDUSTRY_EDITABLE.getStatuses()) {
      try {
        detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
        detail.setStatus(status);

        when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(detail);
        when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(
            Set.of(PwaApplicationPermission.MANAGE_CONTACTS));

        mockMvc.perform(get(ReverseRouter.route(on(PwaContactController.class)
            .renderContactsScreen(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user)))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("caseManagementUrl"))
            .andExpect(model().attributeExists("completeSectionUrl"))
            .andExpect(model().attribute("userCanAccessTaskList", false))
            .andExpect(model().attribute("showCaseManagementLink", true));
      } catch (AssertionError e){
        throw new AssertionError("Failed with status: " + status, e);
      }
    }
    verify(applicationBreadcrumbService, times(ApplicationState.INDUSTRY_EDITABLE.getStatuses().size()))
        .fromCaseManagement(any(), any(), any());
  }

  @Test
  public void renderContactsScreen_whenUserHasManageContactPrivOnly_andApplicationCannotBeEdited() throws Exception {


    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(detail);
    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(
        Set.of(PwaApplicationPermission.MANAGE_CONTACTS));

    mockMvc.perform(get(ReverseRouter.route(on(PwaContactController.class)
        .renderContactsScreen(PwaApplicationType.INITIAL, 1, null, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("caseManagementUrl"))
        .andExpect(model().attributeExists("completeSectionUrl"))
        .andExpect(model().attribute("userCanAccessTaskList", false))
        .andExpect(model().attribute("showCaseManagementLink", true));

    verify(applicationBreadcrumbService, times(1)).fromCaseManagement(any(), any(), any());
  }

  @Test
  public void renderContactsScreen_whenUserHasManageContactPrivAndEditPriv_andApplicationCannotBeEdited() throws Exception {


    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(detail);
    when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(
        Set.of(PwaApplicationPermission.MANAGE_CONTACTS, PwaApplicationPermission.EDIT));

    mockMvc.perform(get(ReverseRouter.route(on(PwaContactController.class)
        .renderContactsScreen(PwaApplicationType.INITIAL, 1, null, null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("caseManagementUrl"))
        .andExpect(model().attributeExists("completeSectionUrl"))
        .andExpect(model().attribute("userCanAccessTaskList", false))
        .andExpect(model().attribute("showCaseManagementLink", true));

    verify(applicationBreadcrumbService, times(1)).fromCaseManagement(any(), any(), any());
  }

  @Test
  public void renderContactsScreen_whenUserHasManageContactPrivAndEditPriv_andApplicationCanBeEdited() throws Exception {

    for(PwaApplicationStatus status: ApplicationState.INDUSTRY_EDITABLE.getStatuses()) {
      try {
        detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
        detail.setStatus(status);

        when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(detail);
        when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(
            Set.of(PwaApplicationPermission.MANAGE_CONTACTS, PwaApplicationPermission.EDIT));

        mockMvc.perform(get(ReverseRouter.route(on(PwaContactController.class)
            .renderContactsScreen(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user)))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("caseManagementUrl"))
            .andExpect(model().attributeExists("completeSectionUrl"))
            .andExpect(model().attribute("userCanAccessTaskList", true))
            .andExpect(model().attribute("showCaseManagementLink", true));
      } catch (AssertionError e){
        throw new AssertionError("Failed with status: " + status, e);
      }
    }


    verify(applicationBreadcrumbService, times(ApplicationState.INDUSTRY_EDITABLE.getStatuses().size()))
        .fromTaskList(any(), any(), any());

  }

  @Test
  public void renderContactsScreen_whenUserHasMEditPrivOnly_andApplicationCanBeEdited() throws Exception {

    for(PwaApplicationStatus status: ApplicationState.INDUSTRY_EDITABLE.getStatuses()) {
      try {
        detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
        detail.setStatus(status);

        when(pwaApplicationDetailService.getTipDetail(anyInt())).thenReturn(detail);
        when(pwaApplicationPermissionService.getPermissions(detail, user.getLinkedPerson())).thenReturn(
            Set.of(PwaApplicationPermission.EDIT));

        mockMvc.perform(get(ReverseRouter.route(on(PwaContactController.class)
            .renderContactsScreen(PwaApplicationType.INITIAL, 1, null, null)))
            .with(authenticatedUserAndSession(user)))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("caseManagementUrl"))
            .andExpect(model().attributeExists("completeSectionUrl"))
            .andExpect(model().attribute("userCanAccessTaskList", true))
            .andExpect(model().attribute("showCaseManagementLink", false));

      } catch (AssertionError e){
        throw new AssertionError("Failed with status: " + status, e);
      }
    }

    verify(applicationBreadcrumbService, times(ApplicationState.INDUSTRY_EDITABLE.getStatuses().size()))
        .fromTaskList(any(), any(), any());

  }

}
