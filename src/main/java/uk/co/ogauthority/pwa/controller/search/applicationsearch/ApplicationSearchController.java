package uk.co.ogauthority.pwa.controller.search.applicationsearch;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaAppAssignmentView;
import uk.co.ogauthority.pwa.model.view.search.SearchScreenView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.service.objects.FormObjectMapper;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationDetailSearchService;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchDisplayItem;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchDisplayItemCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParametersBuilder;

@Controller
@RequestMapping("/application-search")
public class ApplicationSearchController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSearchController.class);

  private final ApplicationDetailSearchService applicationDetailSearchService;
  private final ApplicationSearchContextCreator applicationSearchContextCreator;
  private final ApplicationSearchDisplayItemCreator applicationSearchDisplayItemCreator;
  private final ApplicationInvolvementService applicationInvolvementService;

  @Autowired
  public ApplicationSearchController(ApplicationDetailSearchService applicationDetailSearchService,
                                     ApplicationSearchContextCreator applicationSearchContextCreator,
                                     ApplicationSearchDisplayItemCreator applicationSearchDisplayItemCreator,
                                     ApplicationInvolvementService applicationInvolvementService) {
    this.applicationDetailSearchService = applicationDetailSearchService;
    this.applicationSearchContextCreator = applicationSearchContextCreator;
    this.applicationSearchDisplayItemCreator = applicationSearchDisplayItemCreator;
    this.applicationInvolvementService = applicationInvolvementService;
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

  public static String routeToLandingPage() {
    return ReverseRouter.route(on(ApplicationSearchController.class)
        .getSearchResults(null, AppSearchEntryState.LANDING, null)
    );
  }

  @GetMapping()
  public ModelAndView getSearchResults(AuthenticatedUserAccount authenticatedUserAccount,
                                       @RequestParam(name = "entryState", defaultValue = "SEARCH") AppSearchEntryState entryState,
                                       @ModelAttribute("form") ApplicationSearchParameters applicationSearchParameters) {
    return getSearchModelAndView(entryState, applicationSearchParameters, authenticatedUserAccount);
  }

  public static String getBlankSearchUrl() {
    return ReverseRouter.route(on(ApplicationSearchController.class).getSearchResults(null, null, null));
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

    return new ModelAndView("search/applicationSearch/applicationSearch")
        .addObject("searchScreenView", searchScreenView)
        .addObject("appSearchEntryState", appSearchEntryState)
        // need to provide as search form changes do not include previous search results from the URL params
        .addObject("searchUrl", ApplicationSearchController.getBlankSearchUrl())
        .addObject("assignedCaseOfficers", getCaseOfficersAssignedToInProgressAppsMap())
        .addObject("userType", searchContext.getUserType());

  }

  public enum AppSearchEntryState {
    SEARCH,
    LANDING
  }

}
