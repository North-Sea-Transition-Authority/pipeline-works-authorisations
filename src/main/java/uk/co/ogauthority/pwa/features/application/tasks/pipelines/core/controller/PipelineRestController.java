package uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.controller;

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
import uk.co.ogauthority.pwa.auth.HasAnyRoleByGroup;
import uk.co.ogauthority.pwa.auth.RoleGroup;
import uk.co.ogauthority.pwa.externalapi.PipelineDto;
import uk.co.ogauthority.pwa.externalapi.PipelineDtoRepository;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationPermissionCheck;
import uk.co.ogauthority.pwa.features.application.authorisation.permission.PwaApplicationPermission;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchResult;
import uk.co.ogauthority.pwa.service.searchselector.ManualEntryAttribute;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelection;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;

@RestController
@RequestMapping("/api/pipelines")
public class PipelineRestController {

  private final PadPipelineService padPipelineService;
  private final SearchSelectorService searchSelectorService;
  private final PipelineDtoRepository pipelineDtoRepository;

  @Autowired
  public PipelineRestController(
      PadPipelineService padPipelineService,
      SearchSelectorService searchSelectorService,
      PipelineDtoRepository pipelineDtoRepository) {
    this.padPipelineService = padPipelineService;
    this.searchSelectorService = searchSelectorService;
    this.pipelineDtoRepository = pipelineDtoRepository;
  }

  @GetMapping("/{applicationId}/bundles/search")
  @PwaApplicationPermissionCheck(permissions = PwaApplicationPermission.EDIT)
  @ResponseBody
  public RestSearchResult searchBundleNames(@PathVariable("applicationId") Integer applicationId,
                                            PwaApplicationContext applicationContext,
                                            @RequestParam("term") String searchTerm) {

    var detail = applicationContext.getApplicationDetail();

    var bundleNames = padPipelineService.getAvailableBundleNamesForApplication(detail);

    Set<SearchSelection> selectionSet = bundleNames.stream()
        .map(SearchSelection::new)
        .collect(Collectors.toUnmodifiableSet());

    // Search and filter out unmatched names
    List<RestSearchItem> results = searchSelectorService.search(searchTerm, selectionSet)
        .stream()
        .sorted(Comparator.comparing(RestSearchItem::getText))
        .collect(Collectors.toList());

    // Add manual entry if no match
    searchSelectorService.addManualEntry(searchTerm, results, ManualEntryAttribute.NO_FREE_TEXT_PREFIX);
    return new RestSearchResult(results);
  }

  @GetMapping()
  @HasAnyRoleByGroup(roleGroup = RoleGroup.CONSENT_SEARCH)
  public RestSearchResult searchPipelines(@RequestParam("term") String searchTerm) {
    if (searchTerm == null || searchTerm.trim().length() < 2) {
      return new RestSearchResult(List.of());
    }

    var resultSearchItems = pipelineDtoRepository.searchPipelines(null, searchTerm, null)
        .stream()
        .sorted(PipelineDto::compareTo)
        .map(pipeline -> new RestSearchItem(
            pipeline.getId().toString(), pipeline.getPipelineNumber())
        )
        .toList();
    return new RestSearchResult(resultSearchItems);
  }

}
