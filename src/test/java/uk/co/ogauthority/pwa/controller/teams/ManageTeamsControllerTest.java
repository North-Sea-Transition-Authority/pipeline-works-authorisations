package uk.co.ogauthority.pwa.controller.teams;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.enums.teams.ManageTeamType;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.teams.ManageTeamService;

@RunWith(SpringRunner.class)
@WebMvcTest(ManageTeamsController.class)
@Import(PwaMvcTestConfiguration.class)
public class ManageTeamsControllerTest extends AbstractControllerTest {

  @MockBean
  private ManageTeamService manageTeamService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());

  @Test
  public void renderTeamTypes_allTeamTypesAvailable() throws Exception {

    when(manageTeamService.getManageTeamTypesAndUrlsForUser(user)).thenReturn(Map.of(
        ManageTeamType.REGULATOR_TEAM, "regUrl",
        ManageTeamType.ORGANISATION_TEAMS, "orgUrl",
        ManageTeamType.CONSULTEE_GROUP_TEAMS, "consulteeUrl"
    ));

    mockMvc.perform(get(ReverseRouter.route(on(ManageTeamsController.class).renderTeamTypes(null)))
        .with(user(user)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderTeamTypes_oneTeamTypeAvailable() throws Exception {

    when(manageTeamService.getManageTeamTypesAndUrlsForUser(user)).thenReturn(Map.of(
        ManageTeamType.REGULATOR_TEAM, "regUrl"
    ));

    mockMvc.perform(get(ReverseRouter.route(on(ManageTeamsController.class).renderTeamTypes(null)))
        .with(user(user)))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:regUrl"));

  }

  @Test
  public void renderTeamTypes_noTeamTypesAvailable() throws Exception {

    when(manageTeamService.getManageTeamTypesAndUrlsForUser(user)).thenReturn(Map.of());

    mockMvc.perform(get(ReverseRouter.route(on(ManageTeamsController.class).renderTeamTypes(null)))
        .with(user(user)))
        .andExpect(status().isForbidden());

  }

}
