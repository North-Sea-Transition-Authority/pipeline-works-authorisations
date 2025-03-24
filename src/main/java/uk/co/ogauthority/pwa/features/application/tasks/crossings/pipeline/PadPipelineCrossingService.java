package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskInfo;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectionView;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.util.StreamUtils;

@Service
public class PadPipelineCrossingService implements ApplicationFormSectionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PadPipelineCrossingService.class);

  private final PadPipelineCrossingRepository padPipelineCrossingRepository;
  private final PipelineCrossingFileService pipelineCrossingFileService;
  private final PadPipelineCrossingOwnerService padPipelineCrossingOwnerService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final SearchSelectorService searchSelectorService;
  private final EntityCopyingService entityCopyingService;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public PadPipelineCrossingService(
      PadPipelineCrossingRepository padPipelineCrossingRepository,
      PipelineCrossingFileService pipelineCrossingFileService,
      PadPipelineCrossingOwnerService padPipelineCrossingOwnerService,
      PortalOrganisationsAccessor portalOrganisationsAccessor,
      SearchSelectorService searchSelectorService,
      EntityCopyingService entityCopyingService,
      PadFileManagementService padFileManagementService) {
    this.padPipelineCrossingRepository = padPipelineCrossingRepository;
    this.pipelineCrossingFileService = pipelineCrossingFileService;
    this.padPipelineCrossingOwnerService = padPipelineCrossingOwnerService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.searchSelectorService = searchSelectorService;
    this.entityCopyingService = entityCopyingService;
    this.padFileManagementService = padFileManagementService;
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

  public int getPipelineCrossingCount(PwaApplicationDetail pwaApplicationDetail) {
    return padPipelineCrossingRepository.countAllByPwaApplicationDetail(pwaApplicationDetail);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var pipelineCount = getPipelineCrossingCount(detail);
    return pipelineCrossingFileService.isComplete(detail) && pipelineCount > 0;
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    throw new AssertionError("validate doesnt make sense.");
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return BooleanUtils.isTrue(pwaApplicationDetail.getPipelinesCrossed());
  }

  @Override
  public List<TaskInfo> getTaskInfoList(PwaApplicationDetail pwaApplicationDetail) {
    var pipelineCrossingCount = padPipelineCrossingRepository.countAllByPwaApplicationDetail(pwaApplicationDetail);
    return List.of(
        new TaskInfo("PIPELINE", (long) pipelineCrossingCount)
    );
  }

  @Transactional
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    padFileManagementService.copyUploadedFiles(fromDetail, toDetail, FileDocumentType.PIPELINE_CROSSINGS);

    var copiedPadPipelineCrossingEntityIds = entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padPipelineCrossingRepository.getAllByPwaApplicationDetail(fromDetail),
        toDetail,
        PadPipelineCrossing.class
    );

    var copiedPadPipelineCrossingOwnerEntityIds = entityCopyingService.duplicateEntitiesAndSetParentFromCopiedEntities(
        () -> padPipelineCrossingOwnerService.getAllPipelineCrossingOwners(fromDetail),
        copiedPadPipelineCrossingEntityIds,
        PadPipelineCrossingOwner.class
    );
  }
}
