package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskInfo;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class CarbonStorageAreaCrossingService implements ApplicationFormSectionService {

  private final PadCrossedStorageAreaRepository crossedStorageAreaRepository;
  private final PadCrossedStorageAreaOwnerRepository crossedStorageAreaOwnerRepository;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;

  private final CarbonStorageAreaCrossingFileService fileService;

  public CarbonStorageAreaCrossingService(PadCrossedStorageAreaRepository crossedStorageAreaRepository,
                                          PadCrossedStorageAreaOwnerRepository crossedStorageAreaOwnerRepository,
                                          PortalOrganisationsAccessor portalOrganisationsAccessor,
                                          CarbonStorageAreaCrossingFileService fileService) {
    this.crossedStorageAreaRepository = crossedStorageAreaRepository;
    this.crossedStorageAreaOwnerRepository = crossedStorageAreaOwnerRepository;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.fileService = fileService;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    return  pwaApplicationDetail.getResourceType().equals(PwaResourceType.CCUS)
        || BooleanUtils.isTrue(pwaApplicationDetail.getCsaCrossed());
  }

  public boolean isDocumentsRequired(PwaApplicationDetail pwaApplicationDetail) {
    return crossedStorageAreaRepository.countPadCrossedStorageAreaByPwaApplicationDetailAndCrossingOwnerTypeNot(
        pwaApplicationDetail, CrossingOwner.HOLDER) > 0;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return crossedStorageAreaRepository.countPadCrossedStorageAreaByPwaApplicationDetail(detail) > 0
        && (!isDocumentsRequired(detail) || fileService.isComplete(detail));
  }

  @Override
  public List<TaskInfo> getTaskInfoList(PwaApplicationDetail pwaApplicationDetail) {
    var blockCount = crossedStorageAreaRepository.countPadCrossedStorageAreaByPwaApplicationDetail(pwaApplicationDetail);
    return List.of(
        new TaskInfo("AREA", (long) blockCount)
    );
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    return null;
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {

  }

  public List<CarbonStorageCrossingView> getCrossedAreaViews(PwaApplicationDetail pwaApplicationDetail) {
    var crossedStorageAreas = crossedStorageAreaRepository.findAllByPwaApplicationDetail(pwaApplicationDetail);
    var allCrossedAreaOwnersMap = crossedStorageAreaOwnerRepository.findAllByPadCrossedStorageAreaIn(crossedStorageAreas)
        .stream()
        .collect(Collectors.groupingBy(PadCrossedStorageAreaOwner::getPadCrossedStorageArea));

    // O(N) loop over owners
    var ownerOrgUnitIds = allCrossedAreaOwnersMap.values()
        .stream()
        .flatMap(List::stream)
        .filter(owner -> !Objects.isNull(owner.getOwnerOuId()))
        .map(PadCrossedStorageAreaOwner::getOwnerOuId)
        .collect(Collectors.toSet());
    Map<Integer, String> orgUnitIdToNameMap = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(ownerOrgUnitIds)
        .stream()
        .collect(Collectors.toMap(PortalOrganisationUnit::getOuId, PortalOrganisationUnit::getName));

    var crossedAreaViewList = new ArrayList<CarbonStorageCrossingView>();
    crossedStorageAreas.forEach((crossedStorageArea) -> {
      var ownerNameList = allCrossedAreaOwnersMap.getOrDefault(crossedStorageArea, Collections.emptyList())
          .stream()
          .map(o -> orgUnitIdToNameMap.getOrDefault(o.getOwnerOuId(), o.getOwnerName()))
          .collect(Collectors.toList());
      var view = new CarbonStorageCrossingView(
          crossedStorageArea.getId(),
          crossedStorageArea.getStorageAreaReference(),
          ownerNameList,
          CrossingOwner.HOLDER.equals(crossedStorageArea.getCrossingOwnerType())
      );
      crossedAreaViewList.add(view);
    });
    crossedAreaViewList.sort(Comparator.comparing(CarbonStorageCrossingView::getStorageAreaReference));
    return crossedAreaViewList;
  }

  public boolean doesAreaExistOnApp(PwaApplicationDetail pwaApplicationDetail, String areaRef) {
    return crossedStorageAreaRepository.countPadCrossedStorageAreaByPwaApplicationDetailAndStorageAreaReferenceIgnoreCase(
        pwaApplicationDetail,
        areaRef) > 0;
  }

  @Transactional
  public PadCrossedStorageArea saveStorageAreaCrossings(PwaApplicationDetail pwaApplicationDetail,
                                                        AddCarbonStorageAreaCrossingForm form) {

    var storageArea = mapFormToEntity(pwaApplicationDetail, form);
    var owners = mapFormToChildren(storageArea, form);

    storageArea = crossedStorageAreaRepository.save(storageArea);
    crossedStorageAreaOwnerRepository.saveAll(owners);
    return storageArea;
  }

  @Transactional
  public PadCrossedStorageArea updateStorageAreaCrossings(EditCarbonStorageAreaCrossingForm form,
                                                          Integer crossingId) {

    var storageArea = getById(crossingId);
    var oldOwners = crossedStorageAreaOwnerRepository.findAllByPadCrossedStorageArea(storageArea);
    crossedStorageAreaOwnerRepository.deleteAll(oldOwners);

    storageArea.setCrossingOwnerType(form.getCrossingOwner());
    crossedStorageAreaRepository.save(storageArea);

    var newOwners = mapFormToChildren(storageArea, form);
    crossedStorageAreaOwnerRepository.saveAll(newOwners);

    return storageArea;
  }

  private PadCrossedStorageArea mapFormToEntity(PwaApplicationDetail pwaApplicationDetail,
                                                AddCarbonStorageAreaCrossingForm form) {

    var storageArea = new PadCrossedStorageArea();
    storageArea.setPwaApplicationDetail(pwaApplicationDetail);
    storageArea.setStorageAreaReference(form.getStorageAreaRef());
    storageArea.setCrossingOwnerType(form.getCrossingOwner());
    return storageArea;
  }

  private List<PadCrossedStorageAreaOwner> mapFormToChildren(PadCrossedStorageArea padCrossedArea,
                                                             EditCarbonStorageAreaCrossingForm form) {
    var owners = new ArrayList<PadCrossedStorageAreaOwner>();
    form.getOwnersOuIdList().forEach(ouId ->
        owners.add(new PadCrossedStorageAreaOwner(padCrossedArea, ouId, null))
    );
    return owners;
  }

  public EditCarbonStorageAreaCrossingForm mapToEditForm(PadCrossedStorageArea crossedStorageArea,
                                                         EditCarbonStorageAreaCrossingForm editForm) {
    var areaOwners = crossedStorageAreaOwnerRepository.findAllByPadCrossedStorageArea(crossedStorageArea)
        .stream()
        .map(PadCrossedStorageAreaOwner::getOwnerOuId)
        .collect(Collectors.toList());

    editForm.setCrossingOwner(crossedStorageArea.getCrossingOwnerType());
    editForm.setOwnersOuIdList(areaOwners);
    return editForm;
  }

  public PadCrossedStorageArea getById(Integer storageAreaCrossingId) {
    return crossedStorageAreaRepository.findById(storageAreaCrossingId)
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Crossed storage area not found with id:" + storageAreaCrossingId));
  }

  @Transactional
  public void removeCrossing(PadCrossedStorageArea crossedStorageArea) {
    deleteAllCrossingOwners(crossedStorageArea);
    crossedStorageAreaRepository.delete(crossedStorageArea);
  }

  private void deleteAllCrossingOwners(PadCrossedStorageArea crossedArea) {
    var existingBlockCrossingOwners = crossedStorageAreaOwnerRepository.findAllByPadCrossedStorageArea(crossedArea);
    crossedStorageAreaOwnerRepository.deleteAll(existingBlockCrossingOwners);
  }
}
