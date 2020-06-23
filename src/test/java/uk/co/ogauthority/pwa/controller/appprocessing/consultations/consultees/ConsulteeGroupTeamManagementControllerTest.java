package uk.co.ogauthority.pwa.controller.appprocessing.consultations.consultees;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.form.appprocessing.consultations.consultees.ConsulteeGroupTeamView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;

@RunWith(SpringRunner.class)
@WebMvcTest(ConsulteeGroupTeamManagementController.class)
public class ConsulteeGroupTeamManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService applicationContextService;

  @MockBean
  private PwaAppProcessingContextService appProcessingContextService;

  @MockBean
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());

  @Test
  public void renderManageableGroups_groupsPresent() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupTeamsForUser(user)).thenReturn(
        List.of(new ConsulteeGroupTeamView(1, "group"))
    );

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderManageableGroups(null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderManageableGroups_noGroupsPresent() throws Exception {

    when(consulteeGroupTeamService.getManageableGroupTeamsForUser(user)).thenReturn(List.of());

    mockMvc.perform(get(ReverseRouter.route(on(ConsulteeGroupTeamManagementController.class).renderManageableGroups(null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isForbidden());

  }


}
