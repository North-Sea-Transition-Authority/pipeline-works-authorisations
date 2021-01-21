package uk.co.ogauthority.pwa.controller.search.consents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.form.search.consents.ConsentSearchForm;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.objects.FormObjectMapper;
import uk.co.ogauthority.pwa.service.orgs.PwaOrganisationAccessor;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/consents/search")
public class ConsentSearchController {

  private final ConsentSearchService consentSearchService;
  private final ConsentSearchContextCreator consentSearchContextCreator;
  private final PwaOrganisationAccessor pwaOrganisationAccessor;

  @Autowired
  public ConsentSearchController(ConsentSearchService consentSearchService,
                                 ConsentSearchContextCreator consentSearchContextCreator,
                                 PwaOrganisationAccessor pwaOrganisationAccessor) {
    this.consentSearchService = consentSearchService;
    this.consentSearchContextCreator = consentSearchContextCreator;
    this.pwaOrganisationAccessor = pwaOrganisationAccessor;
  }

  @GetMapping
  public ModelAndView renderSearch(@ModelAttribute("searchParams") ConsentSearchParams consentSearchParams,
                                   AuthenticatedUserAccount user) {
    return getSearchModelAndView(consentSearchParams, user);
  }

  private ModelAndView getSearchModelAndView(ConsentSearchParams consentSearchParams,
                                             AuthenticatedUserAccount user) {

    var sortedOrganisationUnits = pwaOrganisationAccessor.getOrgUnitsUserCanAccess(user)
        .stream()
        .sorted(Comparator.comparing(o -> o.getName().toLowerCase()))
        .collect(StreamUtils.toLinkedHashMap(o -> String.valueOf(o.getOuId()), PortalOrganisationUnit::getName));

    boolean doSearch = consentSearchParams.isSearch();

    List<ConsentSearchResultView> searchResults = List.of();
    if (doSearch) {
      var searchContext = consentSearchContextCreator.createContext(user);
      searchResults = consentSearchService.search(consentSearchParams, searchContext);
    }

    return new ModelAndView("search/consents/consentSearch")
        .addObject("searchResults", searchResults)
        .addObject("maxResultsSize", ConsentSearchService.MAX_RESULTS_SIZE)
        .addObject("resultsHaveBeenLimited", consentSearchService.haveResultsBeenLimited(searchResults))
        .addObject("searched", doSearch)
        .addObject("orgUnitFilterOptions", sortedOrganisationUnits)
        .addObject("form", ConsentSearchForm.fromSearchParams(consentSearchParams));

  }

  @PostMapping
  public ModelAndView postSearch(ConsentSearchParams consentSearchParams,
                                 ConsentSearchForm form,
                                 AuthenticatedUserAccount user) {

    // create new search params from form posted by user, pass them via redirect
    var searchParams = ConsentSearchParams.from(form);
    searchParams.setSearch(true);

    var paramMap = new LinkedMultiValueMap<String, String>();
    paramMap.setAll(FormObjectMapper.toMap(searchParams));

    return ReverseRouter.redirectWithQueryParamMap(on(ConsentSearchController.class)
        .renderSearch(null, null), paramMap);

  }

}