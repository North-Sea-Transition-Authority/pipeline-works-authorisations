package uk.co.ogauthority.pwa.controller.search.consents;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.form.search.consents.ConsentSearchForm;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;

@Controller
@RequestMapping("/consents/search")
public class ConsentSearchController {

  private final ConsentSearchService consentSearchService;

  @Autowired
  public ConsentSearchController(ConsentSearchService consentSearchService) {
    this.consentSearchService = consentSearchService;
  }

  @GetMapping
  public ModelAndView renderSearch(@ModelAttribute("form") ConsentSearchForm form) {
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
  public ModelAndView postSearch(@ModelAttribute("form") ConsentSearchForm form) {
    var results = consentSearchService.search();
    return getSearchModelAndView(results)
        .addObject("searched", true);
  }

}