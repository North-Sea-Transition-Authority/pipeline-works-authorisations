package uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.ConsulteeGroupTeamView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.AddConsulteeGroupTeamMemberFormValidator;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(ConsulteeGroupTeamManagementController.class)
public class ConsulteeGroupTeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService applicationContextService;

  @MockBean
  private PwaAppProcessingContextService appProcessingContextService;

  @MockBean
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @MockBean
  private AddConsulteeGroupTeamMemberFormValidator addMemberFormValidator;

  @MockBean
  private TeamManagementService teamManagementService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person(1, null, null, null, null)), List.of());

  private ConsulteeGroupDetail emtGroupDetail;
  private ConsulteeGroupDetail oduGroupDetail;

  @Before
  public void setUp() {

    emtGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Environmental Management Team", "EMT");
    oduGroupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("Offshore Decommissioning Unit", "ODU");

    when(consulteeGroupTeamService.getManageableGroupDetailsForUser(user)).thenReturn(List.of(
        emtGroupDetail, oduGroupDetail));

    when(teamManagementService.getPersonByEmailAddressOrLoginId(any())).thenReturn(Optional.of(user.getLinkedPerson()));
    when(teamManagementService.getPerson(user.getLinkedPerson().getId().asInt())).thenReturn(user.getLinkedPerson());

  }

  @Test
  public void renderManageableGroups_groupsPresent() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupTeamViewsForUser(user)).thenReturn(List.of(
        new ConsulteeGroupTeamView(1, "group"),
        new ConsulteeGroupTeamView(2, "group2")
    ));

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderManageableGroups(null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderManageableGroups_noGroupsPresent() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupTeamViewsForUser(user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderManageableGroups(null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderManageableGroups_oneGroupPresent() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupTeamViewsForUser(user)).thenReturn(
        List.of(new ConsulteeGroupTeamView(1, "Group1")));

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderManageableGroups(null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:" + ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderTeamMembers(1, null))));

  }

  @Test
  public void renderTeamMembers_validTeam_canManage() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(emtGroupDetail.getConsulteeGroupId(), user)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk())
        .andExpect(view().name("teamManagement/teamMembers"));

  }

  @Test
  public void renderTeamMembers_validTeam_cannotManage() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupDetailsForUser(user)).thenReturn(List.of(oduGroupDetail));

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderTeamMembers(emtGroupDetail.getConsulteeGroupId(), user)))
            .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderAddUserToTeam() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderAddUserToTeam(emtGroupDetail.getConsulteeGroupId(), null, user)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());

  }

  @Test
  public void handleAddUserToTeamSubmit_validationError() throws Exception {

    ControllerTestUtils.mockSmartValidatorErrors(addMemberFormValidator, List.of("userIdentifier"));

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .handleAddUserToTeamSubmit(emtGroupDetail.getConsulteeGroupId(), null, null, user)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk());

  }

  @Test
  public void handleAddUserToTeamSubmit() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .handleAddUserToTeamSubmit(emtGroupDetail.getConsulteeGroupId(), null, null, user)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void renderAddUserToTeam_cannotManage() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupDetailsForUser(user)).thenReturn(List.of(oduGroupDetail));

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderAddUserToTeam(emtGroupDetail.getConsulteeGroupId(), null, user)))
            .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderMemberRoles() throws Exception {

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderMemberRoles(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null, user)))
            .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderMemberRoles_denied() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupDetailsForUser(user)).thenReturn(List.of(oduGroupDetail));

    mockMvc.perform(
        get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .renderMemberRoles(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null, user)))
            .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void handleMemberRolesUpdate() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .handleMemberRolesUpdate(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null, null, user)))
        .with(authenticatedUserAndSession(user))
        .param("userRoles", "ACCESS_MANAGER")
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

    verify(consulteeGroupTeamService, times(1))
        .updateUserRoles(eq(emtGroupDetail), eq(user.getLinkedPerson()), any(), any());

  }

  @Test
  public void handleMemberRolesUpdate_validationError() throws Exception {

    mockMvc.perform(
        post(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class)
            .handleMemberRolesUpdate(emtGroupDetail.getConsulteeGroupId(), user.getLinkedPerson().getId().asInt(), null, null, user)))
            .with(authenticatedUserAndSession(user))
            .param("userRoles", "")
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("teamManagement/memberRoles"));

  }

}
