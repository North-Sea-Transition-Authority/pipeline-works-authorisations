package uk.co.ogauthority.pwa.controller.search.applicationsearch;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationSearchController.class)
public class ApplicationSearchControllerTest extends AbstractControllerTest {

  private AuthenticatedUserAccount permittedUser = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      EnumSet.of(PwaUserPrivilege.PWA_APPLICATION_SEARCH));

  private AuthenticatedUserAccount prohibitedUser = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA));

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService pwaAppProcessingContextService;


  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void renderApplicationSearch_whenPermitted() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).renderApplicationSearch(
        null
    )))
        .with(authenticatedUserAndSession(permittedUser)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderApplicationSearch_whenProhibited() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).renderApplicationSearch(
        null
    )))
        .with(authenticatedUserAndSession(prohibitedUser)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderApplicationSearch_whenNotLoggedIn() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).renderApplicationSearch(
        null
    ))))
        .andExpect(status().is3xxRedirection());

  }
}