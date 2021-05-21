package uk.co.ogauthority.pwa.controller.pwaapplications.rest;

import java.util.Comparator;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchResult;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@RestController
@RequestMapping("/api/portal")
public class PortalOrganisationUnitRestController {

  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final SearchSelectorService searchSelectorService;

  @Autowired
  public PortalOrganisationUnitRestController(
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      SearchSelectorService searchSelectorService) {
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.searchSelectorService = searchSelectorService;
  }

  /**
   * Return active org units where search term is contained in the name and adds search term as manual entry to result.
   */
  @GetMapping("/orgs/lax/units")
  @ResponseBody
  public RestSearchResult searchPortalOrgUnits(@RequestParam("term") String searchTerm) {
    var queryList = portalOrganisationsAccessor.findActiveOrganisationUnitsWhereNameContains(
        searchTerm, PageRequest.of(0, 15)
    );
    var searchResults = searchSelectorService.search(searchTerm, queryList)
        .stream()
        .sorted(Comparator.comparing(RestSearchItem::getText))
        .collect(Collectors.toList());
    searchSelectorService.addManualEntry(searchTerm, searchResults);
    return new RestSearchResult(searchResults);
  }

  /**
   * Return active org units where search term is contained in the name.
   */
  @GetMapping("/orgs/strict/units/")
  @ResponseBody
  public RestSearchResult searchPortalOrgUnitsNoManualEntry(@RequestParam("term") String searchTerm) {
    var queryList = portalOrganisationsAccessor.findActiveOrganisationUnitsWhereNameContains(
        searchTerm, PageRequest.of(0, 15)
    );
    var searchResults = searchSelectorService.search(searchTerm, queryList)
        .stream()
        .sorted(Comparator.comparing(RestSearchItem::getText))
        .collect(Collectors.toList());
    return new RestSearchResult(searchResults);
  }

}
