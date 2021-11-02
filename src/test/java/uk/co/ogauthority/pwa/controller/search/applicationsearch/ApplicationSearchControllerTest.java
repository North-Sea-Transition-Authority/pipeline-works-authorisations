package uk.co.ogauthority.pwa.controller.search.applicationsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaAppAssignmentView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.model.view.search.SearchScreenView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.objects.FormObjectMapper;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationDetailSearchService;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextTestUtil;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchDisplayItemCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParametersBuilder;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

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

  @MockBean
  private ApplicationInvolvementService applicationInvolvementService;

  @MockBean
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @MockBean
  private PwaHolderTeamService pwaHolderTeamService;

  private PortalOrganisationUnit portalOrganisationUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();

  private ApplicationSearchContext permittedUserSearchContext;

  private ApplicationSearchController applicationSearchController;

  @Before
  public void setUp() throws Exception {
    applicationSearchController = new ApplicationSearchController(
        applicationDetailSearchService, applicationSearchContextCreator, applicationSearchDisplayItemCreator, applicationInvolvementService,
        pwaHolderTeamService, portalOrganisationsAccessor);

    permittedUserSearchContext = ApplicationSearchContextTestUtil.emptyUserContext(permittedUser, UserType.OGA);
    when(applicationSearchContextCreator.createContext(permittedUser)).thenReturn(permittedUserSearchContext);
    when(applicationDetailSearchService.validateSearchParamsUsingContext(any(), any()))
        .thenAnswer(invocation -> new BeanPropertyBindingResult(invocation.getArgument(0), "form"));
  }

  @Test
    public void getSearchResults_whenPermitted_landingEntry() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).getSearchResults(
        null, ApplicationSearchController.AppSearchEntryState.LANDING, null
    )))
        .with(authenticatedUserAndSession(permittedUser)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("userTypes", permittedUserSearchContext.getUserTypes()))
        .andExpect(model().attribute("searchUrl", ApplicationSearchController.routeToBlankSearchUrl()))
        .andExpect(model().attribute("appSearchEntryState", ApplicationSearchController.AppSearchEntryState.LANDING))
        .andExpect(model().attribute("assignedCaseOfficers", Map.of()))
        .andExpect(model().attributeDoesNotExist("searchScreenView"));
  }

  @Test
  public void getSearchResults_whenPermitted_landingEntry_industryOnlyUserType() throws Exception {

    var orgUnitId = new OrganisationUnitId(portalOrganisationUnit.getOuId());
    permittedUserSearchContext = ApplicationSearchContextTestUtil.industryContext(permittedUser, Set.of(orgUnitId));
    when(applicationSearchContextCreator.createContext(permittedUser)).thenReturn(permittedUserSearchContext);


    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).getSearchResults(
        null, ApplicationSearchController.AppSearchEntryState.LANDING, null
    )))
        .with(authenticatedUserAndSession(permittedUser)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("useLimitedOrgSearch", true));
  }
  @Test
  public void getSearchResults_whenPermitted_landingEntry_notIndustryOnlyUserType() throws Exception {

    var orgUnitId = new OrganisationUnitId(portalOrganisationUnit.getOuId());
    permittedUserSearchContext = ApplicationSearchContextTestUtil.combinedIndustryOgaContext(permittedUser, Set.of(orgUnitId));
    when(applicationSearchContextCreator.createContext(permittedUser)).thenReturn(permittedUserSearchContext);


    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).getSearchResults(
        null, ApplicationSearchController.AppSearchEntryState.LANDING, null
    )))
        .with(authenticatedUserAndSession(permittedUser)))
        .andExpect(status().isOk())
        .andExpect(model().attribute("useLimitedOrgSearch", false));
  }

  @Test
  public void getSearchResults_whenPermitted_landingEntry_notIndustryOnlyUserType_selectedHolderOrg() throws Exception {

    var orgUnitId = new OrganisationUnitId(portalOrganisationUnit.getOuId());
    permittedUserSearchContext = ApplicationSearchContextTestUtil.combinedIndustryOgaContext(permittedUser, Set.of(orgUnitId));
    when(applicationSearchContextCreator.createContext(permittedUser)).thenReturn(permittedUserSearchContext);

    when(portalOrganisationsAccessor.getOrganisationUnitById(portalOrganisationUnit.getOuId()))
        .thenReturn(Optional.of(portalOrganisationUnit));

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).getSearchResults(
        null, ApplicationSearchController.AppSearchEntryState.LANDING, null
    )))
        .with(authenticatedUserAndSession(permittedUser))
        .param("holderOrgUnitId", portalOrganisationUnit.getSelectionId()))
        .andExpect(status().isOk())
        .andExpect(model().attribute("useLimitedOrgSearch", false))
        .andExpect(model().attributeExists("preselectedHolderOrgUnits"));
  }


  @Test
  public void getSearchResults_openAppsAssignedCaseOfficersMapping() {

    var pwaApplication1 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1).getPwaApplication();
    var pwaApplication2 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 2).getPwaApplication();

    var assignmentViewOpenApp1 = new PwaAppAssignmentView();
    assignmentViewOpenApp1.setId(1);
    assignmentViewOpenApp1.setPwaApplicationId(pwaApplication1.getId());
    assignmentViewOpenApp1.setAssignment(WorkflowAssignment.CASE_OFFICER);
    assignmentViewOpenApp1.setAssigneePersonId(1);
    assignmentViewOpenApp1.setAssigneeName("case officer A");

    var assignmentViewOpenApp2 = new PwaAppAssignmentView();
    assignmentViewOpenApp2.setId(2);
    assignmentViewOpenApp2.setPwaApplicationId(pwaApplication2.getId());
    assignmentViewOpenApp2.setAssignment(WorkflowAssignment.CASE_OFFICER);
    assignmentViewOpenApp2.setAssigneePersonId(2);
    assignmentViewOpenApp2.setAssigneeName("case officer b");

    when(applicationInvolvementService.getCaseOfficersAssignedToInProgressApps()).thenReturn(List.of(assignmentViewOpenApp2, assignmentViewOpenApp1));

    var caseOfficersAssignedToOpenAppsMap = applicationSearchController.getCaseOfficersAssignedToInProgressAppsMap();
    assertThat(caseOfficersAssignedToOpenAppsMap).containsExactly(
        entry(String.valueOf(assignmentViewOpenApp1.getAssigneePersonId()), assignmentViewOpenApp1.getAssigneeName()),
        entry(String.valueOf(assignmentViewOpenApp2.getAssigneePersonId()), assignmentViewOpenApp2.getAssigneeName()));
  }

  @Test
  public void getSearchResults_whenProhibited() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).getSearchResults(
        null, ApplicationSearchController.AppSearchEntryState.LANDING, null
    )))
        .with(authenticatedUserAndSession(prohibitedUser)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void getSearchResults_whenNotLoggedIn() throws Exception {

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).getSearchResults(
        null, ApplicationSearchController.AppSearchEntryState.LANDING, null
    ))))
        .andExpect(status().is3xxRedirection());

  }

  @Test
  public void getSearchResults_runSearchWithParams() throws Exception {

    var screenView = new SearchScreenView<ApplicationDetailItemView>(0, List.of());
    when(applicationDetailSearchService.search(any(), any())).thenReturn(screenView);

    var params = new ApplicationSearchParametersBuilder()
        .setAppReference(APP_REF_SEARCH)
        .createApplicationSearchParameters();

    mockMvc.perform(get(ReverseRouter.routeWithQueryParamMap(on(ApplicationSearchController.class).getSearchResults(
        null, ApplicationSearchController.AppSearchEntryState.SEARCH, null
    ), paramsAsMap(params)
        ))
        .with(authenticatedUserAndSession(permittedUser)))
        .andExpect(status().isOk());

    verify(applicationDetailSearchService, times(1)).search(params, permittedUserSearchContext);

  }

  @Test
  public void getSearchResults_searchParamsInvalid() throws Exception {

    var params = new ApplicationSearchParametersBuilder()
        .setAppReference(APP_REF_SEARCH)
        .createApplicationSearchParameters();

    when(applicationDetailSearchService.validateSearchParamsUsingContext(any(), any()))
        .thenAnswer(invocation -> {
          var bindingResult = new BeanPropertyBindingResult(invocation.getArgument(0), "form");
          bindingResult.reject("appReference.invalid");
          return bindingResult;
        });

    mockMvc.perform(get(ReverseRouter.routeWithQueryParamMap(on(ApplicationSearchController.class).getSearchResults(
        null, ApplicationSearchController.AppSearchEntryState.SEARCH, null
        ), paramsAsMap(params)
    ))
        .with(authenticatedUserAndSession(permittedUser)))
        .andExpect(status().isOk());

    verify(applicationDetailSearchService, times(0)).search(any(), any());

  }

  private MultiValueMap<String, String> paramsAsMap(ApplicationSearchParameters searchParameters){
    var paramMap = new LinkedMultiValueMap<String, String>();
    paramMap.setAll(FormObjectMapper.toMap(searchParameters));
    return paramMap;
  }

  @Test
  public void getSearchResults_whenProhibited_withSearchParams() throws Exception {
    var params = new ApplicationSearchParametersBuilder()
        .setAppReference(APP_REF_SEARCH)
        .createApplicationSearchParameters();

    mockMvc.perform(get(ReverseRouter.route(on(ApplicationSearchController.class).getSearchResults(
        null, ApplicationSearchController.AppSearchEntryState.SEARCH, params
    )))
        .with(authenticatedUserAndSession(prohibitedUser)))
        .andExpect(status().isForbidden());

  }

  @Test
  public void submitSearchParams_whenPermitted() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSearchController.class).submitSearchParams(null)))
        .with(authenticatedUserAndSession(permittedUser))
        .with(csrf()))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void submitSearchParams_whenProhibited() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSearchController.class).submitSearchParams(null)))
        .with(authenticatedUserAndSession(prohibitedUser))
        .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  public void submitSearchParams_whenNotLoggedIn() throws Exception {

    mockMvc.perform(post(ReverseRouter.route(on(ApplicationSearchController.class).submitSearchParams(null))))
        .andExpect(status().isForbidden());
  }
}