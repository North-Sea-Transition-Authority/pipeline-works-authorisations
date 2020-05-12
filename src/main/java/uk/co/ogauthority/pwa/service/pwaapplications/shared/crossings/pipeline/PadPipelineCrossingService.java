package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.model.search.SearchSelectionView;
import uk.co.ogauthority.pwa.model.tasklist.TagColour;
import uk.co.ogauthority.pwa.model.tasklist.TaskListLabel;
import uk.co.ogauthority.pwa.model.tasklist.TaskListSection;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineCrossingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.service.search.SearchSelectorService;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.StringDisplayUtils;

@Service
public class PadPipelineCrossingService implements ApplicationFormSectionService, TaskListSection {

  private final PadPipelineCrossingRepository padPipelineCrossingRepository;
  private final PipelineCrossingFileService pipelineCrossingFileService;
  private final PadPipelineCrossingOwnerService padPipelineCrossingOwnerService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final SearchSelectorService searchSelectorService;

  @Autowired
  public PadPipelineCrossingService(
      PadPipelineCrossingRepository padPipelineCrossingRepository,
      PipelineCrossingFileService pipelineCrossingFileService,
      PadPipelineCrossingOwnerService padPipelineCrossingOwnerService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      SearchSelectorService searchSelectorService) {
    this.padPipelineCrossingRepository = padPipelineCrossingRepository;
    this.pipelineCrossingFileService = pipelineCrossingFileService;
    this.padPipelineCrossingOwnerService = padPipelineCrossingOwnerService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.searchSelectorService = searchSelectorService;
  }

  public PadPipelineCrossing getPipelineCrossing(PwaApplicationDetail detail, Integer id) {
    return padPipelineCrossingRepository.getByPwaApplicationDetailAndId(detail, id)
        .orElseThrow(() -> new PwaEntityNotFoundException("Unable to find pipeline crossing with ID: " + id));
  }

  public void deleteCascade(PadPipelineCrossing padPipelineCrossing) {
    padPipelineCrossingOwnerService.removeAllForCrossing(padPipelineCrossing);
    padPipelineCrossingRepository.delete(padPipelineCrossing);
  }

  public List<PipelineCrossingView> getPipelineCrossingViews(PwaApplicationDetail pwaApplicationDetail) {
    var crossings = padPipelineCrossingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);
    return crossings.stream()
        .map(padPipelineCrossing -> {
          var owners = padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing);
          return new PipelineCrossingView(padPipelineCrossing, owners);
        })
        .collect(Collectors.toList());
  }

  public PipelineCrossingView getPipelineCrossingView(PadPipelineCrossing padPipelineCrossing) {
    var owners = padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing);
    return new PipelineCrossingView(padPipelineCrossing, owners);
  }

  public void createPipelineCrossings(PwaApplicationDetail pwaApplicationDetail, PipelineCrossingForm form) {
    var crossing = new PadPipelineCrossing();
    crossing.setPwaApplicationDetail(pwaApplicationDetail);
    crossing.setPipelineCrossed(form.getPipelineCrossed());
    crossing.setPipelineFullyOwnedByOrganisation(form.getPipelineFullyOwnedByOrganisation());
    padPipelineCrossingRepository.save(crossing);
    padPipelineCrossingOwnerService.createOwners(crossing, form);
  }

  public void updatePipelineCrossing(PadPipelineCrossing crossing, PipelineCrossingForm form) {
    crossing.setPipelineCrossed(form.getPipelineCrossed());
    crossing.setPipelineFullyOwnedByOrganisation(form.getPipelineFullyOwnedByOrganisation());
    padPipelineCrossingRepository.save(crossing);
    padPipelineCrossingOwnerService.createOwners(crossing, form);

  }

  public Map<String, String> getPrepopulatedSearchSelectorItems(List<String> selection) {

    var selectedItems = ListUtils.emptyIfNull(selection);
    var selectionView = new SearchSelectionView<>(selectedItems, Integer::parseInt);

    var orgMap = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(selectionView.getLinkedEntries())
        .stream()
        .collect(StreamUtils.toLinkedHashMap(orgUnit -> String.valueOf(orgUnit.getOuId()),
            PortalOrganisationUnit::getName));

    return searchSelectorService.buildPrepopulatedSelections(selectedItems, orgMap);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var pipelineCount = padPipelineCrossingRepository.countAllByPwaApplicationDetail(detail);
    return pipelineCrossingFileService.isComplete(detail) && pipelineCount > 0;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new AssertionError("validate doesnt make sense.");
  }

  @Override
  public boolean isTaskListEntryCompleted(PwaApplicationDetail pwaApplicationDetail) {
    return isComplete(pwaApplicationDetail);
  }

  @Override
  public boolean getCanShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return BooleanUtils.isTrue(pwaApplicationDetail.getPipelinesCrossed());
  }

  @Override
  public List<TaskListLabel> getTaskListLabels(PwaApplicationDetail pwaApplicationDetail) {
    var crossingLabelColour = TagColour.BLUE;
    var crossingCount = padPipelineCrossingRepository.countAllByPwaApplicationDetail(pwaApplicationDetail);
    var crossingPluralised = StringDisplayUtils.pluralise("crossing", crossingCount);
    return List.of(
        new TaskListLabel(String.format("%d %s", crossingCount, crossingPluralised), crossingLabelColour)
    );
  }
}
