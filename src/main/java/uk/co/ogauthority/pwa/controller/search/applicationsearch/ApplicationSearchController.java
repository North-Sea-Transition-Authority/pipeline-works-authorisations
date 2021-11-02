package uk.co.ogauthority.pwa.controller.search.applicationsearch;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.controller.PortalOrganisationUnitRestController;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaAppAssignmentView;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.view.search.SearchScreenView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.objects.FormObjectMapper;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationDetailSearchService;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchDisplayItem;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchDisplayItemCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParametersBuilder;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/application-search")
public class ApplicationSearchController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSearchController.class);

  private final ApplicationDetailSearchService applicationDetailSearchService;
  private final ApplicationSearchContextCreator applicationSearchContextCreator;
  private final ApplicationSearchDisplayItemCreator applicationSearchDisplayItemCreator;
  private final ApplicationInvolvementService applicationInvolvementService;
  private final PwaHolderTeamService pwaHolderTeamService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  public static String routeToLandingPage() {
    return ReverseRouter.route(on(ApplicationSearchController.class)
        .getSearchResults(null, AppSearchEntryState.LANDING, null)
    );
  }

  public static String routeToBlankSearchUrl() {
    return ReverseRouter.route(on(ApplicationSearchController.class).getSearchResults(null, null, null));
  }

  @Autowired
  public ApplicationSearchController(ApplicationDetailSearchService applicationDetailSearchService,
                                     ApplicationSearchContextCreator applicationSearchContextCreator,
                                     ApplicationSearchDisplayItemCreator applicationSearchDisplayItemCreator,
                                     ApplicationInvolvementService applicationInvolvementService,
                                     PwaHolderTeamService pwaHolderTeamService,
                                     PortalOrganisationsAccessor portalOrganisationsAccessor) {
    this.applicationDetailSearchService = applicationDetailSearchService;
    this.applicationSearchContextCreator = applicationSearchContextCreator;
    this.applicationSearchDisplayItemCreator = applicationSearchDisplayItemCreator;
    this.applicationInvolvementService = applicationInvolvementService;
    this.pwaHolderTeamService = pwaHolderTeamService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
  }

  private ModelAndView redirectAndRunSearch(ApplicationSearchParameters applicationSearchParameters) {
    var safeParams = Objects.requireNonNullElse(
        applicationSearchParameters,
        ApplicationSearchParametersBuilder.createEmptyParams()
    );

    var paramMap = new LinkedMultiValueMap<String, String>();
    paramMap.setAll(FormObjectMapper.toMap(safeParams));

    return ReverseRouter.redirectWithQueryParamMap(on(ApplicationSearchController.class)
        .getSearchResults(
            null,
            AppSearchEntryState.SEARCH,
            null
        ), paramMap
    );
  }

  @GetMapping()
  public ModelAndView getSearchResults(AuthenticatedUserAccount authenticatedUserAccount,
                                       @RequestParam(name = "entryState", defaultValue = "SEARCH") AppSearchEntryState entryState,
                                       @ModelAttribute("form") ApplicationSearchParameters applicationSearchParameters) {
    return getSearchModelAndView(entryState, applicationSearchParameters, authenticatedUserAccount);
  }



  @PostMapping
  public ModelAndView submitSearchParams(@ModelAttribute("form") ApplicationSearchParameters applicationSearchParameters) {
    return redirectAndRunSearch(applicationSearchParameters);
  }

  @VisibleForTesting
  Map<String, String> getCaseOfficersAssignedToInProgressAppsMap() {
    var caseOfficersAssignedToOpenAppsMap = new LinkedHashMap<String, String>();
    applicationInvolvementService.getCaseOfficersAssignedToInProgressApps()
        .stream()
        .sorted(Comparator.comparing(PwaAppAssignmentView::getAssigneeName))
        .forEach(pwaAppAssignmentView -> caseOfficersAssignedToOpenAppsMap.put(
            String.valueOf(pwaAppAssignmentView.getAssigneePersonId()), pwaAppAssignmentView.getAssigneeName()));
    return caseOfficersAssignedToOpenAppsMap;
  }


  private ModelAndView getSearchModelAndView(AppSearchEntryState appSearchEntryState,
                                             ApplicationSearchParameters searchParameters,
                                             AuthenticatedUserAccount authenticatedUserAccount) {

    var searchContext = applicationSearchContextCreator.createContext(authenticatedUserAccount);
    SearchScreenView<ApplicationSearchDisplayItem> searchScreenView = null;
    if (appSearchEntryState.equals(AppSearchEntryState.SEARCH)) {

      var validatedParamBindingResult = applicationDetailSearchService
          .validateSearchParamsUsingContext(searchParameters, searchContext);

      if (!validatedParamBindingResult.hasErrors()) {

        var appDetailItemScreenView = applicationDetailSearchService.search(searchParameters, searchContext);

        var searchDisplayItems = appDetailItemScreenView.getSearchResults().stream()
            .map(applicationSearchDisplayItemCreator::createDisplayItem)
            .collect(Collectors.toList());

        searchScreenView = new SearchScreenView<>(appDetailItemScreenView.getFullResultCount(), searchDisplayItems);

      } else {
        LOGGER.error("WUA_ID:{} has provided invalid search params. Empty results returned.",
            searchContext.getWuaIdAsInt()
        );
      }

    }

    var pwaApplicationTypeMap = PwaApplicationType.stream()
        .sorted(Comparator.comparing(PwaApplicationType::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Enum::name, PwaApplicationType::getDisplayName));

    var modelAndView = new ModelAndView("search/applicationSearch/applicationSearch")
        .addObject("searchScreenView", searchScreenView)
        .addObject("appSearchEntryState", appSearchEntryState)
        // need to provide as search form changes do not include previous search results from the URL params
        .addObject("clearFiltersUrl", ApplicationSearchController.routeToLandingPage())
        .addObject("searchUrl", ApplicationSearchController.routeToBlankSearchUrl())
        .addObject("pwaApplicationTypeMap", pwaApplicationTypeMap)
        .addObject("assignedCaseOfficers", getCaseOfficersAssignedToInProgressAppsMap())
        .addObject("userTypes", searchContext.getUserTypes());

    updateModelAndViewWithHolderOrgAttributes(modelAndView, searchParameters, searchContext);

    return modelAndView;

  }

  private void updateModelAndViewWithHolderOrgAttributes(ModelAndView modelAndView,
                                                         ApplicationSearchParameters searchParameters,
                                                         ApplicationSearchContext searchContext) {

    var useLimitedOrgSearch = searchContext.containsSingleUserTypeOf(UserType.INDUSTRY);
    Map<String, String> limitedOrgUnitOptions = Map.of();

    if (useLimitedOrgSearch) {
      limitedOrgUnitOptions = pwaHolderTeamService.getPortalOrganisationUnitsWhereUserHasAnyOrgRole(
          searchContext.getAuthenticatedUserAccount(),
          EnumSet.allOf(PwaOrganisationRole.class)
      ).stream()
          .sorted(Comparator.comparing(PortalOrganisationUnit::getSelectionText))
          .collect(StreamUtils.toLinkedHashMap(
              PortalOrganisationUnit::getSelectionId,
              PortalOrganisationUnit::getSelectionText)
          );
      // if using REST selector and have selected an org unit, need to pre-populate selector onload.
    } else if (searchParameters.getHolderOrgUnitId() != null) {
      var portalOrg = portalOrganisationsAccessor.getOrganisationUnitById(searchParameters.getHolderOrgUnitId());
      portalOrg.ifPresent(portalOrganisationUnit ->
          modelAndView.addObject("preselectedHolderOrgUnits",
              Map.of(portalOrganisationUnit.getSelectionId(), portalOrganisationUnit.getSelectionText()))
      );
    }

    modelAndView
        .addObject("orgsRestUrl", SearchSelectorService.route(on(PortalOrganisationUnitRestController.class)
            .searchPortalOrgUnitsNoManualEntry(null)))
        .addObject("limitedOrgUnitOptions", limitedOrgUnitOptions)
        .addObject("useLimitedOrgSearch", useLimitedOrgSearch);

  }

  public enum AppSearchEntryState {
    SEARCH,
    LANDING
  }
}
