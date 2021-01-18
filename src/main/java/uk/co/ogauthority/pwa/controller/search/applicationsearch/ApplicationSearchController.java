package uk.co.ogauthority.pwa.controller.search.applicationsearch;

import static java.util.stream.Collectors.toList;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationDetailSearchService;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchDisplayItemCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParametersBuilder;

@Controller
@RequestMapping("/application-search")
public class ApplicationSearchController {

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

    return ReverseRouter.redirect(on(ApplicationSearchController.class)
        .renderApplicationSearch(
            null,
            AppSearchEntryState.SEARCH,
            safeParams.getAppReference()
        )
    );
  }

  public static String routeToLandingPage() {
    return ReverseRouter.route(on(ApplicationSearchController.class)
        .renderApplicationSearch(null, AppSearchEntryState.LANDING, null)
    );
  }

  @GetMapping()
  public ModelAndView renderApplicationSearch(AuthenticatedUserAccount authenticatedUserAccount,
                                              @RequestParam(name = "entryState", defaultValue = "SEARCH") AppSearchEntryState entryState,
                                              @RequestParam(name = "appReference", required = false) String appReference) {


    List<? extends ApplicationDetailItemView> results = Collections.emptyList();

    if (entryState.equals(AppSearchEntryState.SEARCH)) {

      var context = applicationSearchContextCreator.createContext(authenticatedUserAccount);

      // TODO PWA-1058 -- validate params first?
      var appSearchParams = new ApplicationSearchParametersBuilder()
          .setAppReference(appReference)
          .createApplicationSearchParameters();

      results = applicationDetailSearchService.search(appSearchParams, context);
    }

    return getSearchModelAndView(results, entryState);

  }

  @PostMapping
  public ModelAndView doApplicationSearch(AuthenticatedUserAccount authenticatedUserAccount,
                                          @ModelAttribute("form") ApplicationSearchParameters applicationSearchParameters) {


    return redirectAndRunSearch(applicationSearchParameters);

  }


  private ModelAndView getSearchModelAndView(List<? extends ApplicationDetailItemView> applicationDetailItemViewList,
                                             AppSearchEntryState appSearchEntryState) {

    var displayableResults = applicationDetailItemViewList.stream()
        // app id is directly stored in app ref, sort directly rather than deconstruct the reference so its sortable.
        .sorted(Comparator.comparing(ApplicationDetailItemView::getPwaApplicationId).reversed())
        .limit(MAX_RESULTS)
        .map(applicationSearchDisplayItemCreator::createDisplayItem)
        .collect(toList());

    return new ModelAndView("search/applicationSearch/applicationSearch")
        .addObject("showMaxResultsExceededMessage", applicationDetailItemViewList.size() > MAX_RESULTS)
        .addObject("maxResults", MAX_RESULTS)
        .addObject("displayableResults", displayableResults)
        .addObject("appSearchEntryState", appSearchEntryState);

  }

  public enum AppSearchEntryState {
    SEARCH,
    LANDING
  }

}
