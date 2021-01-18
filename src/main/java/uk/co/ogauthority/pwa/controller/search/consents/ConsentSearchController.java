package uk.co.ogauthority.pwa.controller.search.consents;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.form.search.consents.ConsentSearchForm;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;

@Controller
@RequestMapping("/consents/search")
public class ConsentSearchController {

  private final ConsentSearchService consentSearchService;
  private final ConsentSearchContextCreator consentSearchContextCreator;

  @Autowired
  public ConsentSearchController(ConsentSearchService consentSearchService,
                                 ConsentSearchContextCreator consentSearchContextCreator) {
    this.consentSearchService = consentSearchService;
    this.consentSearchContextCreator = consentSearchContextCreator;
  }

  @GetMapping
  public ModelAndView renderSearch(@ModelAttribute("form") ConsentSearchForm form,
                                   AuthenticatedUserAccount user) {
    return getSearchModelAndView(List.of());
  }

  private ModelAndView getSearchModelAndView(List<ConsentSearchResultView> searchResults) {

    return new ModelAndView("search/consents/consentSearch")
        .addObject("searchResults", searchResults)
        .addObject("maxResultsSize", ConsentSearchService.MAX_RESULTS_SIZE)
        .addObject("resultsHaveBeenLimited", consentSearchService.haveResultsBeenLimited(searchResults))
        .addObject("searched", false);

  }

  @PostMapping
  public ModelAndView postSearch(@ModelAttribute("form") ConsentSearchForm form,
                                 AuthenticatedUserAccount user) {

    var searchParams = ConsentSearchParams.from(form);
    var searchContext = consentSearchContextCreator.createContext(user);

    var results = consentSearchService.search(searchParams, searchContext);
    return getSearchModelAndView(results)
        .addObject("searched", true);
  }

}