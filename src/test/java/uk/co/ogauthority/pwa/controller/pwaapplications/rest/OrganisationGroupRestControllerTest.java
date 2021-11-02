package uk.co.ogauthority.pwa.controller.pwaapplications.rest;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.controller.OrganisationGroupRestController;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.orgs.PwaOrganisationAccessor;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@RunWith(SpringRunner.class)
@WebMvcTest(
    value = OrganisationGroupRestController.class,
    includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class)
)
public class OrganisationGroupRestControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final String SEARCH_TERM = "searchTerm";

  @MockBean
  private SearchSelectorService searchSelectorService;

  @MockBean
  protected PwaOrganisationAccessor pwaOrganisationAccessor;

  private AuthenticatedUserAccount authenticatedUser;

  @Before
  public void setup() {
    authenticatedUser = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()),
        List.of());
  }


  @Test
  public void searchOrganisations_whenAuthenticated_thenAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
        on(OrganisationGroupRestController.class).searchOrganisations(SEARCH_TERM)))
        .with(authenticatedUserAndSession(authenticatedUser)))
        .andExpect(status().isOk());

    verify(searchSelectorService, times(1)).search(SEARCH_TERM, List.of());
  }

  @Test
  public void searchOrganisations_whenUnauthenticated_thenNoAccess() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(
        on(OrganisationGroupRestController.class).searchOrganisations(SEARCH_TERM))))
        .andExpect(status().is3xxRedirection());

    verify(searchSelectorService, times(0)).search(SEARCH_TERM, List.of());
  }

}