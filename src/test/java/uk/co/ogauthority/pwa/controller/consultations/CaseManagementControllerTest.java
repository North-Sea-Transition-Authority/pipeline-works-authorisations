package uk.co.ogauthority.pwa.controller.consultations;

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
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;

@RunWith(SpringRunner.class)
@WebMvcTest(CaseManagementController.class)
public class CaseManagementControllerTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService applicationContextService;

  @MockBean
  private PwaAppProcessingContextService appProcessingContextService;

  private AuthenticatedUserAccount user = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());

  @Test
  public void renderTeamTypes_allTeamTypesAvailable() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CaseManagementController.class).renderCaseManagement(null)))
        .with(authenticatedUserAndSession(user)))
        .andExpect(status().isOk());

  }



}
