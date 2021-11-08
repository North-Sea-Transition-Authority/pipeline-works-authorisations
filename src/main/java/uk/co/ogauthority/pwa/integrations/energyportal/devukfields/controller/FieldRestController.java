package uk.co.ogauthority.pwa.integrations.energyportal.devukfields.controller;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfields.external.DevukFieldService;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchResult;
import uk.co.ogauthority.pwa.service.searchselector.ManualEntryAttribute;
import uk.co.ogauthority.pwa.service.searchselector.SearchIdValueSelection;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@RestController
@RequestMapping("/api/fields")
@PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
public class FieldRestController {

  private final DevukFieldService devukFieldService;
  private final SearchSelectorService searchSelectorService;


  @Autowired
  public FieldRestController(DevukFieldService devukFieldService,
                             SearchSelectorService searchSelectorService) {
    this.devukFieldService = devukFieldService;
    this.searchSelectorService = searchSelectorService;
  }




  @GetMapping("/{applicationId}/fields/search")
  @ResponseBody
  public RestSearchResult searchFields(@PathVariable("applicationId") Integer applicationId,
                                       PwaApplicationContext applicationContext,
                                       @RequestParam("term") String searchTerm) {

    Set<SearchIdValueSelection> selectionSet = devukFieldService.getByStatusCodes(List.of(500, 600, 700))
        .stream()
        .map(devukField -> new SearchIdValueSelection(devukField.getFieldId().toString(), devukField.getFieldName()))
        .collect(Collectors.toSet());


    // Search and filter out unmatched names
    List<RestSearchItem> results = searchSelectorService.search(searchTerm, selectionSet)
        .stream()
        .sorted(Comparator.comparing(RestSearchItem::getText))
        .collect(Collectors.toList());

    // Add manual entry if no match
    searchSelectorService.addManualEntry(searchTerm, results, ManualEntryAttribute.WITH_FREE_TEXT_PREFIX);
    return new RestSearchResult(results);
  }

}
