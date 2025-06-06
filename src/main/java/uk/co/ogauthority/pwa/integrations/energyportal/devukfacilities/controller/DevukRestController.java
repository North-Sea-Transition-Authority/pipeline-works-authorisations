package uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external.DevukFacilityService;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchResult;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@RestController
@RequestMapping("/api/devuk")
public class DevukRestController {

  private final DevukFacilityService devukFacilityService;
  private final SearchSelectorService searchSelectorService;

  @Autowired
  public DevukRestController(DevukFacilityService devukFacilityService,
                             SearchSelectorService searchSelectorService) {
    this.devukFacilityService = devukFacilityService;
    this.searchSelectorService = searchSelectorService;
  }

  @GetMapping("/facilities")
  @ResponseBody
  public RestSearchResult searchFacilities(@RequestParam("term") String searchTerm) {
    var searchableList = devukFacilityService.getFacilities(searchTerm);
    List<RestSearchItem> results = searchSelectorService.search(searchTerm, searchableList)
        .stream()
        .sorted(Comparator.comparing(RestSearchItem::getText))
        .collect(Collectors.toList());
    searchSelectorService.addManualEntry(searchTerm, results);
    return new RestSearchResult(results);
  }

}
