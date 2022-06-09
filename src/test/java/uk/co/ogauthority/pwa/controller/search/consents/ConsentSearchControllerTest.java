package uk.co.ogauthority.pwa.controller.search.consents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.controller.PwaMvcTestConfiguration;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.orgs.PwaOrganisationAccessor;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;

@RunWith(SpringRunner.class)
@WebMvcTest(ConsentSearchController.class)
@Import(PwaMvcTestConfiguration.class)
public class ConsentSearchControllerTest extends AbstractControllerTest {

  private final AuthenticatedUserAccount permittedUser = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      EnumSet.of(PwaUserPrivilege.PWA_CONSENT_SEARCH));

  private final AuthenticatedUserAccount prohibitedUser = new AuthenticatedUserAccount(
      new WebUserAccount(1, new Person()),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA));

  @MockBean
  private ConsentSearchService consentSearchService;

  @MockBean
  private ConsentSearchContextCreator consentSearchContextCreator;

  @MockBean
  private PwaOrganisationAccessor pwaOrganisationAccessor;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void renderSearch_whenPermitted_noActualSearch() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ConsentSearchController.class).renderSearch(null, null)))
        .with(authenticatedUserAndSession(permittedUser)))
        .andExpect(status().isOk());

    // search not done when basic render
    verifyNoInteractions(consentSearchContextCreator);
    verify(consentSearchService, times(0)).search(any(), any());
    verify(pwaOrganisationAccessor, times(1)).getOrgUnitsUserCanAccess(permittedUser);

  }

  @Test
  public void renderSearch_whenPermitted_searchDone() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ConsentSearchController.class).renderSearch(null, null)))
        .with(authenticatedUserAndSession(permittedUser))
        .queryParam("search", "true"))
        .andExpect(status().isOk());

    var params = new ConsentSearchParams();
    params.setSearch(true);

    // search done when param present
    verify(consentSearchContextCreator, times(1)).createContext(permittedUser);
    verify(consentSearchService, times(1)).search(eq(params), any());
    verify(pwaOrganisationAccessor, times(1)).getOrgUnitsUserCanAccess(permittedUser);

  }

  @Test
  public void renderSearch_searched_filterByOrgUnit() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ConsentSearchController.class).renderSearch(null, null)))
        .with(authenticatedUserAndSession(permittedUser))
        .queryParam("search", "true")
        .queryParam("holderOrgUnitId", "22"))
        .andExpect(status().isOk());

    var params = new ConsentSearchParams();
    params.setSearch(true);
    params.setHolderOrgUnitId(22);

    // search done when param present
    verify(consentSearchContextCreator, times(1)).createContext(permittedUser);
    verify(consentSearchService, times(1)).search(eq(params), any());

  }

  @Test
  public void renderSearch_whenProhibited() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ConsentSearchController.class).renderSearch(null, null)))
        .with(authenticatedUserAndSession(prohibitedUser)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void renderSearch_whenNotLoggedIn() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ConsentSearchController.class).renderSearch(null, null))))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void postSearch_whenPermitted_filtersPassedInRedirect_eventLogged() throws Exception {

    String viewName = Objects.requireNonNull(
        mockMvc.perform(post(ReverseRouter.route(on(ConsentSearchController.class).postSearch(null, null, Optional.empty())))
            .with(authenticatedUserAndSession(permittedUser))
            .with(csrf())
            .param("holderOuId", "25"))
            .andExpect(status().is3xxRedirection())
            .andReturn()
            .getModelAndView())
            .getViewName();

    assert viewName != null;
    String queryString = viewName.substring(viewName.indexOf("?"));
    assertThat(queryString)
        .contains("holderOrgUnitId=25")
        .contains("search=true");

    verify(analyticsService, times(1)).sendAnalyticsEvent(any(), eq(AnalyticsEventCategory.CONSENT_SEARCH), eq(Map.of("holderOrgUnitId", "true")));

  }


  @Test
  public void postSearch_whenProhibited() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ConsentSearchController.class).postSearch(null, null, Optional.empty())))
        .with(authenticatedUserAndSession(prohibitedUser))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }


  @Test
  public void postSearch_whenNotLoggedIn() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ConsentSearchController.class).postSearch(null, null, Optional.empty()))))
        .andExpect(status().isForbidden());

  }

}