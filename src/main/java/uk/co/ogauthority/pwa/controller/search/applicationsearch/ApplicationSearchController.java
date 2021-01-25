package uk.co.ogauthority.pwa.controller.search.applicationsearch;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.objects.FormObjectMapper;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationDetailSearchService;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchDisplayItemCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParametersBuilder;

@Controller
@RequestMapping("/application-search")
public class ApplicationSearchController {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationSearchController.class);
  private static final long MAX_RESULTS = 50L;

  private final ApplicationDetailSearchService applicationDetailSearchService;
  private final ApplicationSearchContextCreator applicationSearchContextCreator;
  private final ApplicationSearchDisplayItemCreator applicationSearchDisplayItemCreator;

  @Autowired
  public ApplicationSearchController(ApplicationDetailSearchService applicationDetailSearchService,
                                     ApplicationSearchContextCreator applicationSearchContextCreator,
                                     ApplicationSearchDisplayItemCreator applicationSearchDisplayItemCreator) {
    this.applicationDetailSearchService = applicationDetailSearchService;
    this.applicationSearchContextCreator = applicationSearchContextCreator;
    this.applicationSearchDisplayItemCreator = applicationSearchDisplayItemCreator;
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

    var context = applicationSearchContextCreator.createContext(authenticatedUserAccount);

    return getSearchModelAndView(entryState, applicationSearchParameters, context);

  }

  public static String getBlankSearchUrl() {
    return ReverseRouter.route(on(ApplicationSearchController.class).getSearchResults(null, null, null));
  }

  @PostMapping
  public ModelAndView submitSearchParams(@ModelAttribute("form") ApplicationSearchParameters applicationSearchParameters) {
    return redirectAndRunSearch(applicationSearchParameters);
  }


  private ModelAndView getSearchModelAndView(AppSearchEntryState appSearchEntryState,
                                             ApplicationSearchParameters searchParameters,
                                             ApplicationSearchContext applicationSearchContext
                                             ) {

    var modelAndView = new ModelAndView("search/applicationSearch/applicationSearch")
        .addObject("maxResults", MAX_RESULTS)
        .addObject("appSearchEntryState", appSearchEntryState)
        // need to provide a search form changes do not include previous search results from the URL params
        .addObject("searchUrl", ApplicationSearchController.getBlankSearchUrl())
        .addObject("userType", applicationSearchContext.getUserType());

    List<ApplicationDetailItemView> results = Collections.emptyList();
    if (appSearchEntryState.equals(AppSearchEntryState.SEARCH)) {
      var validatedParamBindingResult = applicationDetailSearchService.validateSearchParamsUsingContext(
          searchParameters,
          applicationSearchContext
      );

      if (!validatedParamBindingResult.hasErrors()) {
        results = applicationDetailSearchService.search(searchParameters, applicationSearchContext);
      } else {
        LOGGER.error("WUA_ID:{} has provided invalid search params. Empty results returned.",
            applicationSearchContext.getWuaIdAsInt()
        );
      }
    }

    var displayableResults = results.stream()
        // app id is directly stored in app ref, sort directly rather than deconstruct the reference so its sortable.
        .sorted(Comparator.comparing(ApplicationDetailItemView::getPwaApplicationId).reversed())
        .limit(MAX_RESULTS)
        .map(applicationSearchDisplayItemCreator::createDisplayItem)
        .collect(toList());

    modelAndView.addObject("showMaxResultsExceededMessage", results.size() > MAX_RESULTS)
        .addObject("displayableResults", displayableResults);

    return modelAndView;

  }

  public enum AppSearchEntryState {
    SEARCH,
    LANDING
  }

}
