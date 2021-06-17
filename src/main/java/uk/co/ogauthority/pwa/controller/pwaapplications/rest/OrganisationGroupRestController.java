package uk.co.ogauthority.pwa.controller.pwaapplications.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchResult;
import uk.co.ogauthority.pwa.service.orgs.PwaOrganisationAccessor;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@RestController
@RequestMapping("/api/")
public class OrganisationGroupRestController {

  private final SearchSelectorService searchSelectorService;
  private final PwaOrganisationAccessor pwaOrganisationAccessor;

  @Autowired
  public OrganisationGroupRestController(SearchSelectorService searchSelectorService,
                                         PwaOrganisationAccessor pwaOrganisationAccessor) {
    this.searchSelectorService = searchSelectorService;
    this.pwaOrganisationAccessor = pwaOrganisationAccessor;
  }


  @GetMapping("/organisations")
  @ResponseBody
  public RestSearchResult searchOrganisations(@Nullable @RequestParam("term") String searchTerm) {
    return new RestSearchResult(
        searchSelectorService.search(
            searchTerm,
            pwaOrganisationAccessor.findOrganisationGroupsWhereNameContains(searchTerm)
        )
    );
  }


}