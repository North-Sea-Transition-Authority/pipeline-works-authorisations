package uk.co.ogauthority.pwa.controller.search.applicationsearch;

import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationDetailSearchService;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchDisplayItemCreator;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;

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


  @GetMapping
  public ModelAndView renderApplicationSearch(AuthenticatedUserAccount authenticatedUserAccount) {

    return getSearchModelAndView(List.of());

  }

  @PostMapping
  public ModelAndView doApplicationSearch(AuthenticatedUserAccount authenticatedUserAccount) {

    var context = applicationSearchContextCreator.createContext(authenticatedUserAccount);
    var searchParams = new ApplicationSearchParameters();
    var results = applicationDetailSearchService.search(searchParams, context);

    return getSearchModelAndView(results);

  }


  private ModelAndView getSearchModelAndView(List<ApplicationDetailItemView> applicationDetailItemViewList) {

    var displayableResults = applicationDetailItemViewList.stream()
        .sorted(Comparator.comparing(ApplicationDetailItemView::getPadReference, String.CASE_INSENSITIVE_ORDER).reversed())
        .limit(MAX_RESULTS)
        .map(applicationSearchDisplayItemCreator::createDisplayItem)
        .collect(toList());

    return new ModelAndView("search/applicationSearch/applicationSearch")
        .addObject("showMaxResultsExceededMessage", applicationDetailItemViewList.size() > MAX_RESULTS)
        .addObject("maxResults", MAX_RESULTS)
        .addObject("displayableResults", displayableResults);

  }

}
