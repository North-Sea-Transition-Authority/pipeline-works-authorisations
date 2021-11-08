package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskInfo;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlock;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlockService;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PickablePearsBlock;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class BlockCrossingService implements ApplicationFormSectionService {

  private final PadCrossedBlockRepository padCrossedBlockRepository;
  private final PadCrossedBlockOwnerRepository padCrossedBlockOwnerRepository;
  private final PearsBlockService pearsBlockService;
  private final PortalOrganisationsAccessor portalOrganisationsAccessor;
  private final BlockCrossingFileService blockCrossingFileService;
  private final Clock clock;
  private final EntityCopyingService entityCopyingService;
  private final PadFileService padFileService;

  @Autowired
  public BlockCrossingService(PadCrossedBlockRepository padCrossedBlockRepository,
                              PadCrossedBlockOwnerRepository padCrossedBlockOwnerRepository,
                              PearsBlockService pearsBlockService,
                              PortalOrganisationsAccessor portalOrganisationsAccessor,
                              BlockCrossingFileService blockCrossingFileService,
                              @Qualifier("utcClock") Clock clock,
                              EntityCopyingService entityCopyingService,
                              PadFileService padFileService) {

    this.padCrossedBlockRepository = padCrossedBlockRepository;
    this.padCrossedBlockOwnerRepository = padCrossedBlockOwnerRepository;
    this.pearsBlockService = pearsBlockService;
    this.portalOrganisationsAccessor = portalOrganisationsAccessor;
    this.blockCrossingFileService = blockCrossingFileService;
    this.clock = clock;
    this.entityCopyingService = entityCopyingService;
    this.padFileService = padFileService;
  }

  public PadCrossedBlock getCrossedBlockByIdAndApplicationDetail(int crossedBlockId,
                                                                 PwaApplicationDetail pwaApplicationDetail) {
    return padCrossedBlockRepository.findById(crossedBlockId)
        .filter(cb -> cb.getPwaApplicationDetail().equals(pwaApplicationDetail))
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Crossed block not found with id:" + crossedBlockId + " for appDetailId:" + pwaApplicationDetail.getId()));
  }

  public void errorWhenCrossedBlockDoesNotExist(int crossedBlockId,
                                              PwaApplicationDetail pwaApplicationDetail) {
    if (!padCrossedBlockRepository.existsByIdAndPwaApplicationDetail(crossedBlockId, pwaApplicationDetail)) {
      throw new PwaEntityNotFoundException(
          "Crossed block not found with id:" + crossedBlockId + " for appDetailId:" + pwaApplicationDetail.getId());
    }
  }


  public BlockCrossingView getCrossedBlockView(PwaApplicationDetail pwaApplicationDetail, Integer crossingId) {
    return getCrossedBlockViews(pwaApplicationDetail).stream()
        .filter(blockCrossingView -> crossingId.equals(blockCrossingView.getId()))
        .findFirst()
        .orElseThrow(() -> new PwaEntityNotFoundException(
            "Failed to find crossing view with ID: " + crossingId + " and detail ID of: " + pwaApplicationDetail.getId()));
  }

  public List<BlockCrossingView> getCrossedBlockViews(PwaApplicationDetail pwaApplicationDetail) {

    var crossedBlocks = padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail);

    Map<PadCrossedBlock, List<PadCrossedBlockOwner>> allCrossedBlockOwnersMap = padCrossedBlockOwnerRepository.findByPadCrossedBlockIn(
        crossedBlocks
    )
        .stream()
        .collect(Collectors.groupingBy(PadCrossedBlockOwner::getPadCrossedBlock));

    // O(N) loop over owners
    var ownerOrgUnitIds = allCrossedBlockOwnersMap.values()
        .stream()
        .flatMap(List::stream)
        .filter(owner -> !Objects.isNull(owner.getOwnerOuId()))
        .map(PadCrossedBlockOwner::getOwnerOuId)
        .collect(Collectors.toSet());

    Map<Integer, String> orgUnitIdToNameMap = portalOrganisationsAccessor.getOrganisationUnitsByIdIn(ownerOrgUnitIds)
        .stream()
        .collect(Collectors.toMap(PortalOrganisationUnit::getOuId, PortalOrganisationUnit::getName));


    var crossedBlockViewList = new ArrayList<BlockCrossingView>();
    crossedBlocks.forEach((crossedBlock) -> {
      var ownerNameList = allCrossedBlockOwnersMap.getOrDefault(crossedBlock, Collections.emptyList())
          .stream()
          .map(o -> orgUnitIdToNameMap.getOrDefault(o.getOwnerOuId(), o.getOwnerName()))
          .collect(Collectors.toList());

      var view = new BlockCrossingView(
          crossedBlock.getId(),
          crossedBlock.getBlockReference(),
          crossedBlock.getLicence() != null ? crossedBlock.getLicence().getLicenceName() : "Unlicensed",
          ownerNameList,
          CrossedBlockOwner.HOLDER.equals(crossedBlock.getBlockOwner())
      );
      crossedBlockViewList.add(view);
    });

    crossedBlockViewList.sort(Comparator.comparing(BlockCrossingView::getBlockReference));
    return crossedBlockViewList;

  }

  @Transactional
  public PadCrossedBlock updateAndSaveBlockCrossingAndOwnersFromForm(PadCrossedBlock padCrossedBlock,
                                                                     EditBlockCrossingForm form) {
    // replace linked owners by deleting all existing linked once and inserting new records
    deleteAllCrossedBlockOwners(padCrossedBlock);
    mapEditFormBlockToEntity(padCrossedBlock, form);

    var newCrossedBlockOwners = createBlockCrossingOwnerEntitiesFromForm(padCrossedBlock, form);
    padCrossedBlockOwnerRepository.saveAll(newCrossedBlockOwners);
    return padCrossedBlockRepository.save(padCrossedBlock);
  }


  public void mapBlockCrossingToEditForm(PadCrossedBlock padCrossedBlock,
                                         EditBlockCrossingForm form) {

    var ownerList = padCrossedBlockOwnerRepository.findByPadCrossedBlock(padCrossedBlock);

    if (CrossedBlockOwner.UNLICENSED.equals(padCrossedBlock.getBlockOwner())) {

      form.setBlockOwnersOuIdList(Collections.emptyList());

    } else if (CrossedBlockOwner.PORTAL_ORGANISATION.equals(padCrossedBlock.getBlockOwner())) {

      var ownerOuIdList = ownerList.stream()
          .filter(o -> o.getOwnerOuId() != null)
          .map(PadCrossedBlockOwner::getOwnerOuId)
          .collect(Collectors.toList());

      form.setBlockOwnersOuIdList(ownerOuIdList);

    }

    form.setCrossedBlockOwner(padCrossedBlock.getBlockOwner());

  }

  @Transactional
  public PadCrossedBlock createAndSaveBlockCrossingAndOwnersFromForm(PwaApplicationDetail pwaApplicationDetail,
                                                                     AddBlockCrossingForm form) {

    var crossedBlock = createBlockCrossingEntityFromForm(pwaApplicationDetail, form);
    var crossedBlockOwners = createBlockCrossingOwnerEntitiesFromForm(crossedBlock, form);

    crossedBlock = padCrossedBlockRepository.save(crossedBlock);
    padCrossedBlockOwnerRepository.saveAll(crossedBlockOwners);
    return crossedBlock;
  }

  private PadCrossedBlock createBlockCrossingEntityFromForm(PwaApplicationDetail pwaApplicationDetail,
                                                            AddBlockCrossingForm form) {
    var crossedBlock = new PadCrossedBlock();
    crossedBlock.setPwaApplicationDetail(pwaApplicationDetail);
    mapAddFormBlockToEntity(crossedBlock, form);
    return crossedBlock;
  }

  private void mapAddFormBlockToEntity(PadCrossedBlock padCrossedBlock, AddBlockCrossingForm form) {
    var pearsBlock = pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKeyOrError(
        form.getPickedBlock());
    padCrossedBlock.setQuadrantNumber(pearsBlock.getQuadrantNumber());
    padCrossedBlock.setBlockNumber(pearsBlock.getBlockNumber());
    padCrossedBlock.setSuffix(pearsBlock.getSuffix());
    padCrossedBlock.setBlockReference(pearsBlock.getBlockReference());
    padCrossedBlock.setLicence(pearsBlock.getPearsLicence());
    padCrossedBlock.setLocation(pearsBlock.getBlockLocation());
    padCrossedBlock.setCreatedInstant(clock.instant());
    mapEditFormBlockToEntity(padCrossedBlock, form);
  }

  private void mapEditFormBlockToEntity(PadCrossedBlock padCrossedBlock, EditBlockCrossingForm form) {
    padCrossedBlock.setBlockOwner(form.getCrossedBlockOwner());
  }


  private List<PadCrossedBlockOwner> createBlockCrossingOwnerEntitiesFromForm(PadCrossedBlock padCrossedBlock,
                                                                              EditBlockCrossingForm form) {
    var createdBlockOwners = new ArrayList<PadCrossedBlockOwner>();
    // Each ouId will have been validated so we can assume everything is good for db persistence
    // only create owners when the owner type is not holder
    if (CrossedBlockOwner.PORTAL_ORGANISATION.equals(form.getCrossedBlockOwner())) {
      form.getBlockOwnersOuIdList().forEach(ouId ->
          createdBlockOwners.add(new PadCrossedBlockOwner(padCrossedBlock, ouId, null))
      );
    }

    return createdBlockOwners;

  }

  @Transactional
  public void removeBlockCrossing(PadCrossedBlock padCrossedBlock) {
    deleteAllCrossedBlockOwners(padCrossedBlock);
    padCrossedBlockRepository.delete(padCrossedBlock);
  }


  private void deleteAllCrossedBlockOwners(PadCrossedBlock padCrossedBlock) {
    var existingBlockCrossingOwners = padCrossedBlockOwnerRepository.findByPadCrossedBlock(padCrossedBlock);
    padCrossedBlockOwnerRepository.deleteAll(existingBlockCrossingOwners);
  }

  public Optional<PickablePearsBlock> getPickablePearsBlockFromForm(AddBlockCrossingForm form) {
    return pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKey(form.getPickedBlock())
        .map(PickablePearsBlock::new);
  }

  @Override
  public List<TaskInfo> getTaskInfoList(PwaApplicationDetail pwaApplicationDetail) {
    var blockCount = padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetail(pwaApplicationDetail);
    return List.of(
        new TaskInfo("BLOCK", (long) blockCount)
    );
  }

  public boolean isDocumentsRequired(PwaApplicationDetail pwaApplicationDetail) {
    return !pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.DEPOSIT_CONSENT)
        && padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerNot(
            pwaApplicationDetail, CrossedBlockOwner.HOLDER) > 0;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetail(detail) > 0
        && (!isDocumentsRequired(detail) || blockCrossingFileService.isComplete(detail));
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    // TODO: PWA-502 - Ensure validation works on this service.
    throw new ActionNotAllowedException("This service shouldn't be validated against yet");
  }


  public boolean doesBlockExistOnApp(PwaApplicationDetail pwaApplicationDetail, PearsBlock pearsBlock) {
    return padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockReference(
        pwaApplicationDetail, pearsBlock.getBlockReference()) > 0;
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {
    if (!isDocumentsRequired(detail)) {
      padFileService.cleanupFiles(detail, ApplicationDetailFilePurpose.BLOCK_CROSSINGS, List.of());
    }
  }

  @Transactional
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    var copiedCrossedBlockEntityIds = entityCopyingService.duplicateEntitiesAndSetParent(
        () -> padCrossedBlockRepository.getAllByPwaApplicationDetail(fromDetail),
        toDetail,
        PadCrossedBlock.class
    );

    var copiedCrossedBlockOwnersEntityIds = entityCopyingService.duplicateEntitiesAndSetParentFromCopiedEntities(
        () -> padCrossedBlockOwnerRepository.findByPadCrossedBlock_PwaApplicationDetail(fromDetail),
        copiedCrossedBlockEntityIds,
        PadCrossedBlockOwner.class
    );

    var copiedCrossedBlockFiles = padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail,
        toDetail,
        ApplicationDetailFilePurpose.BLOCK_CROSSINGS,
        ApplicationFileLinkStatus.FULL);
  }
}
