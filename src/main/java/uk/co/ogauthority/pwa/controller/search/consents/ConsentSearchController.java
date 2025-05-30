package uk.co.ogauthority.pwa.controller.search.consents;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.Comparator;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.HasAnyRoleByGroup;
import uk.co.ogauthority.pwa.auth.RoleGroup;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsEventCategory;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsService;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationSearchUnit;
import uk.co.ogauthority.pwa.model.form.search.consents.ConsentSearchForm;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.model.view.search.SearchScreenView;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.objects.FormObjectMapper;
import uk.co.ogauthority.pwa.service.orgs.PwaOrganisationAccessor;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchContextCreator;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Controller
@RequestMapping("/consents/search")
@HasAnyRoleByGroup(roleGroup = RoleGroup.CONSENT_SEARCH)
public class ConsentSearchController {

  private final ConsentSearchService consentSearchService;
  private final ConsentSearchContextCreator consentSearchContextCreator;
  private final PwaOrganisationAccessor pwaOrganisationAccessor;
  private final AnalyticsService analyticsService;

  @Autowired
  public ConsentSearchController(ConsentSearchService consentSearchService,
                                 ConsentSearchContextCreator consentSearchContextCreator,
                                 PwaOrganisationAccessor pwaOrganisationAccessor,
                                 AnalyticsService analyticsService) {
    this.consentSearchService = consentSearchService;
    this.consentSearchContextCreator = consentSearchContextCreator;
    this.pwaOrganisationAccessor = pwaOrganisationAccessor;
    this.analyticsService = analyticsService;
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
        .sorted(Comparator.comparing(o -> o.getOrgSearchableUnitName().toLowerCase()))
        .collect(StreamUtils.toLinkedHashMap(o ->
            String.valueOf(o.getOrgUnitId()), PortalOrganisationSearchUnit::getOrgSearchableUnitName));

    boolean doSearch = consentSearchParams.isSearch();

    SearchScreenView<ConsentSearchResultView> searchScreenView = null;
    if (doSearch) {
      var searchContext = consentSearchContextCreator.createContext(user);
      searchScreenView = consentSearchService.search(consentSearchParams, searchContext);
    }

    return new ModelAndView("search/consents/consentSearch")
        .addObject("searchScreenView", searchScreenView)
        .addObject("searched", doSearch)
        .addObject("orgUnitFilterOptions", sortedOrganisationUnits)
        .addObject("form", ConsentSearchForm.fromSearchParams(consentSearchParams))
        .addObject("consentSearchUrlFactory", new ConsentSearchUrlFactory());

  }

  @PostMapping
  public ModelAndView postSearch(@ModelAttribute("form") ConsentSearchForm form,
                                 AuthenticatedUserAccount user,
                                 @CookieValue(name = AnalyticsUtils.GA_CLIENT_ID_COOKIE_NAME, required = false)
                                 Optional<String> analyticsClientId) {

    // create new search params from form posted by user, pass them via redirect
    var searchParams = ConsentSearchParams.from(form);
    searchParams.setSearch(true);

    var analyticsParamMap = AnalyticsUtils.getFiltersUsedParamMap(searchParams);
    analyticsParamMap.remove("search");
    analyticsService.sendAnalyticsEvent(analyticsClientId, AnalyticsEventCategory.CONSENT_SEARCH, analyticsParamMap);

    var paramMap = new LinkedMultiValueMap<String, String>();
    paramMap.setAll(FormObjectMapper.toMap(searchParams));

    return ReverseRouter.redirectWithQueryParamMap(on(ConsentSearchController.class)
        .renderSearch(null, null), paramMap);

  }

}