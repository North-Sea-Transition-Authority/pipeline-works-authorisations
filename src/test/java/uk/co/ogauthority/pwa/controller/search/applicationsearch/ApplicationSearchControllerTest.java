package uk.co.ogauthority.pwa.controller.search.applicationsearch;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationDetailSearchService;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextTestUtil;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchDisplayItemCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParametersBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationSearchController.class)
public class ApplicationSearchControllerTest extends AbstractControllerTest {

  private static final String APP_REF_SEARCH = "SEARCH_REF";

  private final AuthenticatedUserAccount permittedUser = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      EnumSet.of(PwaUserPrivilege.PWA_APPLICATION_SEARCH));

  private final AuthenticatedUserAccount prohibitedUser = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA));

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService pwaAppProcessingContextService;

  @MockBean
  private ApplicationDetailSearchService applicationDetailSearchService;

  @MockBean
  private ApplicationSearchDisplayItemCreator applicationSearchDisplayItemCreator;

  @MockBean
  private ApplicationSearchContextCreator applicationSearchContextCreator;

  private ApplicationSearchContext permittedUserSearchContext;

  @Before
  public void setUp() throws Exception {
    permittedUserSearchContext = ApplicationSearchContextTestUtil.emptyUserContext(permittedUser, UserType.OGA);
    when(applicationSearchContextCreator.createContext(permittedUser)).thenReturn(permittedUserSearchContext);
  }

  @Test
  public void renderApplicationSearch_whenPermitted() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).renderApplicationSearch(
        null, ApplicationSearchController.AppSearchEntryState.LANDING, null
    )))
        .with(authenticatedUserAndSession(permittedUser)))
        .andExpect(status().isOk());

  }

  @Test
  public void renderApplicationSearch_whenProhibited() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).renderApplicationSearch(
        null, ApplicationSearchController.AppSearchEntryState.LANDING, null
    )))
        .with(authenticatedUserAndSession(prohibitedUser)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderApplicationSearch_whenNotLoggedIn() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).renderApplicationSearch(
        null, ApplicationSearchController.AppSearchEntryState.LANDING, null
    ))))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void renderApplicationSearch_runSearchWithParams() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).renderApplicationSearch(
        null, ApplicationSearchController.AppSearchEntryState.SEARCH, APP_REF_SEARCH
    )))
        .with(authenticatedUserAndSession(permittedUser)))
        .andExpect(status().isOk());

    var expectedSearchParams = new ApplicationSearchParametersBuilder()
        .setAppReference(APP_REF_SEARCH)
        .createApplicationSearchParameters();

    verify(applicationDetailSearchService, times(1)).search(expectedSearchParams, permittedUserSearchContext);

  }

  @Test
  public void renderApplicationSearch_whenProhibited_withSearchParams() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).renderApplicationSearch(
        null, ApplicationSearchController.AppSearchEntryState.SEARCH, APP_REF_SEARCH
    )))
        .with(authenticatedUserAndSession(prohibitedUser)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void doApplicationSearch_whenPermitted() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSearchController.class).doApplicationSearch(
        null, null
    )))
        .with(authenticatedUserAndSession(permittedUser))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void doApplicationSearch_whenProhibited() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSearchController.class).doApplicationSearch(
        null, null
    )))
        .with(authenticatedUserAndSession(prohibitedUser))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

  @Test
  public void doApplicationSearch_whenNotLoggedIn() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSearchController.class).doApplicationSearch(
        null, null
    ))))
        .andExpect(status().isForbidden());

  }
}