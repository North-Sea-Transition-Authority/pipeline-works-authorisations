package uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.exception.LastUserInRoleRemovedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.ConsulteeGroupTeamView;
import uk.co.ogauthority.pwa.model.form.teammanagement.UserRolesForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.AddConsulteeGroupTeamMemberFormValidator;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.teammanagement.OldTeamManagementService;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;

@WebMvcTest(ConsulteeGroupTeamManagementController.class)
@Import(PwaMvcTestConfiguration.class)
class ConsulteeGroupTeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @MockBean
  private AddConsulteeGroupTeamMemberFormValidator addMemberFormValidator;

  @MockBean
  private OldTeamManagementService teamManagementService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person(1, null, null, null, null)), List.of(PwaUserPrivilege.PWA_ACCESS));

  private ConsulteeGroupDetail emtGroupDetail;
  private ConsulteeGroupDetail oduGroupDetail;

  @BeforeEach
  void setUp() {

    emtGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Environmental Management Team", "EMT");
    oduGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Offshore Decommissioning Unit", "ODU");

    when(consulteeGroupTeamService.getManageableGroupDetailsForUser(user)).thenReturn(List.of(
        emtGroupDetail, oduGroupDetail));

    when(teamManagementService.getPersonByEmailAddressOrLoginId(any())).thenReturn(Optional.of(user.getLinkedPerson()));
    when(teamManagementService.getPerson(user.getLinkedPerson().getId().asInt())).thenReturn(user.getLinkedPerson());

  }

  @Test
  void renderManageableGroups_groupsPresent() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupTeamViewsForUser(user)).thenReturn(List.of(
        new ConsulteeGroupTeamView(1, "group"),
        new ConsulteeGroupTeamView(2, "group2")
    ));

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderManageableGroups(null)))
        .with(user(user)))
        .andExpect(status().isOk());

  }

  @Test
  void renderManageableGroups_noGroupsPresent() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupTeamViewsForUser(user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderManageableGroups(null)))
        .with(user(user)))
        .andExpect(status().isForbidden());

  }

  @Test
  void renderManageableGroups_oneGroupPresent() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupTeamViewsForUser(user)).thenReturn(
        List.of(new ConsulteeGroupTeamView(1, "Group1")));

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderManageableGroups(null)))
        .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderTeamMembers(1, null))));

  }

  @Test
  void renderTeamMembers_validTeam_canManage() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(emtGroupDetail.getConsulteeGroupId(), user)))
        .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("teamManagementOld/teamMembers"));

  }

  @Test
  void renderTeamMembers_validTeam_cannotManage() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupDetailsForUser(user)).thenReturn(List.of(oduGroupDetail));

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(emtGroupDetail.getConsulteeGroupId(), user)))
            .with(user(user)))
        .andExpect(status().isForbidden());

  }

  @Test
  void renderAddUserToTeam() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderAddUserToTeam(emtGroupDetail.getConsulteeGroupId(), null, user)))
        .with(user(user)))
        .andExpect(status().isOk());

  }

  @Test
  void handleAddUserToTeamSubmit_validationError() throws Exception {

    ControllerTestUtils.mockSmartValidatorErrors(addMemberFormValidator, List.of("userIdentifier"));

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .handleAddUserToTeamSubmit(emtGroupDetail.getConsulteeGroupId(), null, null, user)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk());

  }

  @Test
  void handleAddUserToTeamSubmit() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .handleAddUserToTeamSubmit(emtGroupDetail.getConsulteeGroupId(), null, null, user)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  void renderAddUserToTeam_cannotManage() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupDetailsForUser(user)).thenReturn(List.of(oduGroupDetail));

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderAddUserToTeam(emtGroupDetail.getConsulteeGroupId(), null, user)))
            .with(user(user)))
        .andExpect(status().isForbidden());

  }

  @Test
  void renderMemberRoles() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderMemberRoles(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null, user)))
            .with(user(user)))
        .andExpect(status().isOk());

  }

  @Test
  void renderMemberRoles_denied() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupDetailsForUser(user)).thenReturn(List.of(oduGroupDetail));

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderMemberRoles(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null, user)))
            .with(user(user)))
        .andExpect(status().isForbidden());

  }

  @Test
  void renderMemberRoles_rolesPreFilled() throws Exception {

    var member = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.RECIPIENT, ConsulteeGroupMemberRole.RESPONDER));

    when(consulteeGroupTeamService.getTeamMemberByGroupAndPerson(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson()))
        .thenReturn(Optional.of(member));

    var form = (UserRolesForm) Objects.requireNonNull(mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderMemberRoles(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null,
                user)))
            .with(user(user)))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView())
        .getModel()
        .get("form");

    assertThat(form.getUserRoles()).containsExactlyInAnyOrder("RECIPIENT", "RESPONDER");

  }

  @Test
  void handleMemberRolesUpdate() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .handleMemberRolesUpdate(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null, null, user)))
        .with(user(user))
        .param("userRoles", "ACCESS_MANAGER")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(consulteeGroupTeamService, times(1))
        .updateUserRoles(eq(emtGroupDetail.getConsulteeGroup()), eq(user.getLinkedPerson()), any());

  }

  @Test
  void handleMemberRolesUpdate_validationError() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .handleMemberRolesUpdate(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null, null, user)))
            .with(user(user))
            .param("userRoles", "")
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("teamManagementOld/memberRoles"));

  }

  @Test
  void handleMemberRolesUpdate_lastInRoles() throws Exception {

    doThrow(new LastUserInRoleRemovedException("Access managers, Consultation responders"))
        .when(consulteeGroupTeamService).updateUserRoles(eq(emtGroupDetail.getConsulteeGroup()), eq(user.getLinkedPerson()), any());

    doCallRealMethod().when(consulteeGroupTeamService).mapGroupMemberToTeamMemberView(any());

    var bindingResult = (BeanPropertyBindingResult) Objects.requireNonNull(mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .handleMemberRolesUpdate(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null,
                null, user)))
            .with(user(user))
            .param("userRoles", "RECIPIENT")
            .with(csrf()))
        .andExpect(status().isOk())
        .andReturn()
        .getModelAndView())
        .getModel()
        .values()
        .stream()
        .filter(val -> val instanceof BindingResult)
        .findFirst()
        .orElseThrow();

    assertThat(bindingResult.getFieldError("userRoles"))
        .extracting(FieldError::getDefaultMessage)
        .isEqualTo("This update would leave no users in the following roles: Access managers, Consultation responders");

  }

  @Test
  void renderRemoveMemberScreen() throws Exception {

    var member = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.RECIPIENT, ConsulteeGroupMemberRole.RESPONDER));

    when(consulteeGroupTeamService.getTeamMemberOrError(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(member);

    doCallRealMethod().when(consulteeGroupTeamService).mapGroupMemberToTeamMemberView(any());

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderRemoveMemberScreen(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null)))
            .with(user(user)))
        .andExpect(status().isOk());

  }

  @Test
  void renderRemoveMemberScreen_notMember() throws Exception {

    when(consulteeGroupTeamService.getTeamMemberOrError(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenThrow(
        new PwaEntityNotFoundException(""));

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderRemoveMemberScreen(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null)))
            .with(user(user)))
        .andExpect(status().isNotFound());

  }

  @Test
  void removeMember() throws Exception {

    var member = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.RECIPIENT, ConsulteeGroupMemberRole.RESPONDER));

    when(consulteeGroupTeamService.getTeamMemberOrError(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(member);

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .removeMember(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(consulteeGroupTeamService, times(1)).removeTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson());

  }

  @Test
  void removeMember_notMember() throws Exception {

    doThrow(new PwaEntityNotFoundException(""))
        .when(consulteeGroupTeamService).removeTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson());

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .removeMember(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isNotFound());

  }

  @Test
  void removeMember_LastInRoles() throws Exception {

    var member = new ConsulteeGroupTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson(), Set.of(
        ConsulteeGroupMemberRole.ACCESS_MANAGER, ConsulteeGroupMemberRole.RESPONDER));

    when(consulteeGroupTeamService.getTeamMemberOrError(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson())).thenReturn(member);

    doThrow(new LastUserInRoleRemovedException("Access managers, Consultation responders"))
        .when(consulteeGroupTeamService).removeTeamMember(emtGroupDetail.getConsulteeGroup(), user.getLinkedPerson());

    doCallRealMethod().when(consulteeGroupTeamService).mapGroupMemberToTeamMemberView(any());

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .removeMember(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null)))
            .with(user(user))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(model().attribute(
            "error",
            "This person cannot be removed from the team as they are currently the only person in the following roles: Access managers, Consultation responders"));

  }

}
