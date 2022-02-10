package uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.controller;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlockService;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchResult;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@RestController
@RequestMapping("/api/pears")
public class PearsRestController {

  private final PearsBlockService pearsBlockService;
  private final SearchSelectorService searchSelectorService;

  public PearsRestController(PearsBlockService pearsBlockService,
                             SearchSelectorService searchSelectorService) {
    this.pearsBlockService = pearsBlockService;
    this.searchSelectorService = searchSelectorService;
  }

  @GetMapping("/blocks")
  @ResponseBody
  public RestSearchResult searchBlocks(@RequestParam("term") String searchTerm) {
    if (StringUtils.length(searchTerm) >= 3) {
      var searchableList = pearsBlockService.findOffshorePickablePearsBlocks(searchTerm, PageRequest.of(0, Integer.MAX_VALUE));
      List<RestSearchItem> results = searchSelectorService.search(searchTerm, searchableList);
      return new RestSearchResult(results);
    }
    return null;
  }

}
