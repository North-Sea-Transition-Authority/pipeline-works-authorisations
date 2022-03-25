package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.teams.CreateTeamsController;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.form.teammanagement.AddOrganisationTeamForm;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.teams.TeamCreationService;
import uk.co.ogauthority.pwa.testutils.ControllerTestUtils;

@RunWith(SpringRunner.class)
@WebMvcTest(CreateTeamsController.class)
@Import(PwaMvcTestConfiguration.class)
public class CreateTeamsControllerTest extends AbstractControllerTest {

  @MockBean
  private TeamCreationService teamCreationService;

  private AuthenticatedUserAccount organisationAccessManager;
  private AuthenticatedUserAccount unAuthenticatedUserAccount;


  @Before
  public void setup() {

     organisationAccessManager = new AuthenticatedUserAccount(new WebUserAccount(3), List.of(
        PwaUserPrivilege.PWA_REG_ORG_MANAGE));
     unAuthenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(3), List.of());
  }


  @Test
  public void getNewOrganisationTeam_whenAuthenticated_thenAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
        on(CreateTeamsController.class).getNewOrganisationTeam(null)))
        .with(authenticatedUserAndSession(organisationAccessManager)))
        .andExpect(status().isOk());
  }

  @Test
  public void getNewOrganisationTeam_whenUnauthenticated_thenNoAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
        on(CreateTeamsController.class).getNewOrganisationTeam(null)))
        .with(authenticatedUserAndSession(unAuthenticatedUserAccount)))
        .andExpect(status().isForbidden());
  }



  @Test
  public void createNewOrganisationTeam_whenFullSaveAndInvalidForm_thenNoCreate() throws Exception {

    var form = new AddOrganisationTeamForm();
    form.setOrganisationGroup(null);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    bindingResult.addError(new FieldError("Error", "ErrorMessage", "default message"));

    when(teamCreationService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(post(ReverseRouter.route(
        on(CreateTeamsController.class).createNewOrganisationTeam(form, bindingResult, ValidationType.FULL, null)))
        .with(authenticatedUserAndSession(organisationAccessManager))
        .with(csrf())
        .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isOk());

    verify(teamCreationService, times(0)).getOrCreateOrganisationGroupTeam(any(), any());
  }

  @Test
  public void createNewOrganisationTeam_whenAuthenticatedAndFullSaveAndValidForm_thenCreate() throws Exception {

    var form = new AddOrganisationTeamForm("1");

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(teamCreationService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(post(ReverseRouter.route(
        on(CreateTeamsController.class).createNewOrganisationTeam(form, bindingResult, ValidationType.FULL, organisationAccessManager)))
        .with(authenticatedUserAndSession(organisationAccessManager))
        .with(csrf())
        .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().is3xxRedirection());

    verify(teamCreationService, times(1)).getOrCreateOrganisationGroupTeam(any(), any());
  }

  @Test
  public void createNewOrganisationTeam_whenUnauthenticated_thenNoAccess() throws Exception {

    var form = new AddOrganisationTeamForm("1");

    var bindingResult = new BeanPropertyBindingResult(form, "form");

    when(teamCreationService.validate(any(), any(), any())).thenReturn(bindingResult);

    mockMvc.perform(post(ReverseRouter.route(
        on(CreateTeamsController.class).createNewOrganisationTeam(form, bindingResult, ValidationType.FULL, null)))
        .with(authenticatedUserAndSession(unAuthenticatedUserAccount))
        .with(csrf())
        .params(ControllerTestUtils.fullValidationPostParams()))
        .andExpect(status().isForbidden());

    verify(teamCreationService, times(0)).getOrCreateOrganisationGroupTeam(any(), any());
  }


}