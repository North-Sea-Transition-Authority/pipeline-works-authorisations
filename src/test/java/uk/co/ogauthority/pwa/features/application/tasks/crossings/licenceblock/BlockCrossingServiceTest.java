package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.BlockLocation;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.LicenceStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlock;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlockService;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsLicence;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BlockCrossingServiceTest {

  private int APP_ID;

  private final int CROSSED_BLOCK_ID = 90;
  private final int OU_ID = 99;

  private final String BLOCK_REF = "1/2/3";

  @Mock
  private PadCrossedBlockRepository padCrossedBlockRepository;

  @Mock
  private PadCrossedBlockOwnerRepository padCrossedBlockOwnerRepository;

  @Mock
  private PearsBlockService pearsBlockService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private BlockCrossingFileService blockCrossingFileService;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Mock
  private PadFileManagementService padFileManagementService;

  private Clock clock = Clock.systemDefaultZone();

  private BlockCrossingService blockCrossingService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PadCrossedBlock padCrossedBlock;

  private AddBlockCrossingForm addBlockForm;
  private EditBlockCrossingForm editBlockForm;

  private PearsLicence licence;

  private PearsBlock licensedBlock;

  private PortalOrganisationUnit organisationUnit;

  @BeforeEach
  void setUp() throws Exception {
    blockCrossingService = new BlockCrossingService(
        padCrossedBlockRepository,
        padCrossedBlockOwnerRepository,
        pearsBlockService,
        portalOrganisationsAccessor,
        blockCrossingFileService,
        clock,
        entityCopyingService,
        padFileManagementService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    APP_ID = pwaApplicationDetail.getMasterPwaApplicationId();
    padCrossedBlock = new PadCrossedBlock();
    padCrossedBlock.setId(CROSSED_BLOCK_ID);
    padCrossedBlock.setPwaApplicationDetail(pwaApplicationDetail);
    padCrossedBlock.setBlockOwner(CrossingOwner.HOLDER);
    padCrossedBlock.setBlockReference(BLOCK_REF);

    when(padCrossedBlockRepository.findById(CROSSED_BLOCK_ID)).thenReturn(Optional.of(padCrossedBlock));

    licence = new PearsLicence(1, "P", 1, "P1", LicenceStatus.EXTANT);

    licensedBlock = new PearsBlock(
        "licenceKey1",
        licence,
        "1/2/3",
        "3",
        "2",
        "1",
        BlockLocation.OFFSHORE);

    addBlockForm = new AddBlockCrossingForm();
    editBlockForm = new EditBlockCrossingForm();

    organisationUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
  }

  @Test
  void getCrossedBlockByIdAndApplicationDetail_whenBlockFound_andHasMatchingDetail() {
    assertThat(blockCrossingService.getCrossedBlockByIdAndApplicationDetail(CROSSED_BLOCK_ID, pwaApplicationDetail))
        .isEqualTo(padCrossedBlock);

  }

  @Test
  void getCrossedBlockByIdAndApplicationDetail_whenBlockFound_andHasNonMatchingDetail() {
    var otherDetail = new PwaApplicationDetail();
    otherDetail.setId(9999);
    padCrossedBlock.setPwaApplicationDetail(otherDetail);
    assertThrows(PwaEntityNotFoundException.class, () ->
      blockCrossingService.getCrossedBlockByIdAndApplicationDetail(CROSSED_BLOCK_ID, pwaApplicationDetail));

  }

  @Test
  void errorWhenCrossedBlockDoesNotExist_whenBlockFound_andHasNonMatchingDetail() {
    var otherDetail = new PwaApplicationDetail();
    otherDetail.setId(9999);
    padCrossedBlock.setPwaApplicationDetail(otherDetail);
    assertThrows(PwaEntityNotFoundException.class, () ->
      blockCrossingService.errorWhenCrossedBlockDoesNotExist(CROSSED_BLOCK_ID, pwaApplicationDetail));

  }

  @Test
  void getCrossedBlockViews_whenHolderIsOwner_andUnlicensedBlock() {

    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    var views = blockCrossingService.getCrossedBlockViews(pwaApplicationDetail);

    assertThat(views).isNotEmpty().allSatisfy(bcv -> {
      assertThat(bcv.getBlockOperatorList()).isEmpty();
      assertThat(bcv.getBlockOwnedCompletelyByHolder()).isTrue();
      assertThat(bcv.getBlockReference()).isEqualTo(BLOCK_REF);
      assertThat(bcv.getId()).isEqualTo(padCrossedBlock.getId());
      assertThat(bcv.getLicenceReference()).isEqualTo("Unlicensed");

    });
  }

  @Test
  void getCrossedBlockViews_whenHolderIsOwner_andLicensedBlock() {

    var licence = new PearsLicence(1, "P", 1, "P1", LicenceStatus.EXTANT);
    padCrossedBlock.setLicence(licence);
    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    var views = blockCrossingService.getCrossedBlockViews(pwaApplicationDetail);

    assertThat(views).isNotEmpty().allSatisfy(bcv -> {
      assertThat(bcv.getBlockOperatorList()).isEmpty();
      assertThat(bcv.getBlockOwnedCompletelyByHolder()).isTrue();
      assertThat(bcv.getBlockReference()).isEqualTo(BLOCK_REF);
      assertThat(bcv.getId()).isEqualTo(padCrossedBlock.getId());
      assertThat(bcv.getLicenceReference()).isEqualTo(licence.getLicenceName());

    });

  }

  @Test
  void getCrossedBlockViews_whenOrgUnitIsOwner_andUnlicensedBlock() {
    var orgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    var owner = new PadCrossedBlockOwner(padCrossedBlock, PortalOrganisationTestUtils.DEFAULT_UNIT_ID, null);
    padCrossedBlock.setBlockOwner(CrossingOwner.PORTAL_ORGANISATION);

    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    when(padCrossedBlockOwnerRepository.findByPadCrossedBlockIn(eq(List.of(padCrossedBlock))))
        .thenReturn(List.of(owner));

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(any()))
        .thenReturn(List.of(orgUnit));

    var views = blockCrossingService.getCrossedBlockViews(pwaApplicationDetail);

    assertThat(views).isNotEmpty().allSatisfy(bcv -> {
      assertThat(bcv.getBlockOperatorList().get(0)).isEqualTo(orgUnit.getName());
      assertThat(bcv.getBlockOwnedCompletelyByHolder()).isFalse();
      assertThat(bcv.getBlockReference()).isEqualTo(BLOCK_REF);
      assertThat(bcv.getId()).isEqualTo(padCrossedBlock.getId());
    });

  }

  @Test
  void getCrossedBlockViews_whenManualOrgIsOwner_andUnlicensedBlock() {

    padCrossedBlock.setBlockOwner(CrossingOwner.UNLICENSED);
    var owner = new PadCrossedBlockOwner(padCrossedBlock, null, "MANUAL");
    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    when(padCrossedBlockOwnerRepository.findByPadCrossedBlockIn(eq(List.of(padCrossedBlock))))
        .thenReturn(List.of(owner));


    var views = blockCrossingService.getCrossedBlockViews(pwaApplicationDetail);

    assertThat(views).isNotEmpty().allSatisfy(bcv -> {
      assertThat(bcv.getBlockOperatorList().get(0)).isEqualTo(owner.getOwnerName());
      assertThat(bcv.getBlockOwnedCompletelyByHolder()).isFalse();
      assertThat(bcv.getBlockReference()).isEqualTo(BLOCK_REF);
      assertThat(bcv.getId()).isEqualTo(padCrossedBlock.getId());
    });

  }


  @Test
  void updateAndSaveBlockCrossingAndOwnersFromForm_verifyRepositoryInteractionIsInCorrectOrder() {

    editBlockForm.setBlockOwnersOuIdList(List.of(PortalOrganisationTestUtils.DEFAULT_UNIT_ID));
    editBlockForm.setCrossingOwner(CrossingOwner.PORTAL_ORGANISATION);

    blockCrossingService.updateAndSaveBlockCrossingAndOwnersFromForm(padCrossedBlock, editBlockForm);

    InOrder verifyOrder = Mockito.inOrder(padCrossedBlockOwnerRepository, padCrossedBlockRepository);
    verifyOrder.verify(padCrossedBlockOwnerRepository).findByPadCrossedBlock(padCrossedBlock);
    verifyOrder.verify(padCrossedBlockOwnerRepository).deleteAll(any());
    verifyOrder.verify(padCrossedBlockOwnerRepository, times(1)).saveAll(any());
    verifyOrder.verify(padCrossedBlockRepository, times(1)).save(any());
    verifyOrder.verifyNoMoreInteractions();
  }

  @Test
  void updateAndSaveBlockCrossingAndOwnersFromForm_whenPortalOrgOwner() {

    editBlockForm.setBlockOwnersOuIdList(List.of(PortalOrganisationTestUtils.DEFAULT_UNIT_ID));
    editBlockForm.setCrossingOwner(CrossingOwner.PORTAL_ORGANISATION);

    ArgumentCaptor<List<PadCrossedBlockOwner>> ownerCapture = ArgumentCaptor.forClass(List.class);

    ArgumentCaptor<PadCrossedBlock> blockCapture = ArgumentCaptor.forClass(PadCrossedBlock.class);

    var spyBlock = spy(PadCrossedBlock.class);

    blockCrossingService.updateAndSaveBlockCrossingAndOwnersFromForm(spyBlock, editBlockForm);

    // want to make sure only the simple owner value is interacted with
    InOrder padCrossedBlockVerifier = Mockito.inOrder(spyBlock);
    padCrossedBlockVerifier.verify(spyBlock, times(1)).setBlockOwner(any());
    padCrossedBlockVerifier.verifyNoMoreInteractions();

    verify(padCrossedBlockOwnerRepository, times(1)).saveAll(ownerCapture.capture());
    verify(padCrossedBlockRepository, times(1)).save(blockCapture.capture());

    assertThat(ownerCapture.getValue().size()).isEqualTo(1);
    assertThat(ownerCapture.getValue().get(0)).satisfies(o -> {
      assertThat(o.getOwnerOuId()).isEqualTo(PortalOrganisationTestUtils.DEFAULT_UNIT_ID);
      assertThat(o.getOwnerName()).isNull();
      assertThat(o.getPadCrossedBlock()).isEqualTo(spyBlock);
    });

    assertThat(blockCapture.getValue()).satisfies(padBlock ->
      assertThat(padBlock.getBlockOwner()).isEqualTo(CrossingOwner.PORTAL_ORGANISATION));

  }

  @Test
  void updateAndSaveBlockCrossingAndOwnersFromForm_whenUnlicenced() {

    editBlockForm.setCrossingOwner(CrossingOwner.UNLICENSED);

    ArgumentCaptor<List<PadCrossedBlockOwner>> ownerCapture = ArgumentCaptor.forClass(List.class);

    ArgumentCaptor<PadCrossedBlock> blockCapture = ArgumentCaptor.forClass(PadCrossedBlock.class);

    var spyBlock = spy(PadCrossedBlock.class);

    blockCrossingService.updateAndSaveBlockCrossingAndOwnersFromForm(spyBlock, editBlockForm);

    // want to make sure only the simple owner value is interacted with
    InOrder padCrossedBlockVerifier = Mockito.inOrder(spyBlock);
    padCrossedBlockVerifier.verify(spyBlock, times(1)).setBlockOwner(any());
    padCrossedBlockVerifier.verifyNoMoreInteractions();

    verify(padCrossedBlockOwnerRepository, times(1)).saveAll(ownerCapture.capture());
    verify(padCrossedBlockRepository, times(1)).save(blockCapture.capture());

    assertThat(ownerCapture.getValue().size()).isEqualTo(0);

    assertThat(blockCapture.getValue()).satisfies(padBlock ->
      assertThat(padBlock.getBlockOwner()).isEqualTo(CrossingOwner.UNLICENSED));
  }

  @Test
  void mapBlockCrossingToEditForm_whenOrganisationUnitOwner() {

    var orgUnitOwner = new PadCrossedBlockOwner(padCrossedBlock, PortalOrganisationTestUtils.DEFAULT_UNIT_ID, null);
    when(padCrossedBlockOwnerRepository.findByPadCrossedBlock(padCrossedBlock)).thenReturn(List.of(orgUnitOwner));
    padCrossedBlock.setBlockOwner(CrossingOwner.PORTAL_ORGANISATION);

    blockCrossingService.mapBlockCrossingToEditForm(padCrossedBlock, editBlockForm);

    assertThat(editBlockForm.getBlockOwnersOuIdList()).isEqualTo(List.of(PortalOrganisationTestUtils.DEFAULT_UNIT_ID));
    assertThat(editBlockForm.getCrossingOwner()).isEqualTo(CrossingOwner.PORTAL_ORGANISATION);

  }

  @Test
  void mapBlockCrossingToEditForm_whenOtherOwner() {

    var orgUnitOwner = new PadCrossedBlockOwner(padCrossedBlock, null, "OTHER");
    when(padCrossedBlockOwnerRepository.findByPadCrossedBlock(padCrossedBlock)).thenReturn(List.of(orgUnitOwner));
    padCrossedBlock.setBlockOwner(CrossingOwner.UNLICENSED);

    blockCrossingService.mapBlockCrossingToEditForm(padCrossedBlock, editBlockForm);

    assertThat(editBlockForm.getBlockOwnersOuIdList()).isEmpty();
    assertThat(editBlockForm.getCrossingOwner()).isEqualTo(CrossingOwner.UNLICENSED);

  }

  @Test
  void mapBlockCrossingToEditForm_whenHolderOwner() {
    when(padCrossedBlockOwnerRepository.findByPadCrossedBlock(padCrossedBlock)).thenReturn(List.of());
    padCrossedBlock.setBlockOwner(CrossingOwner.HOLDER);

    blockCrossingService.mapBlockCrossingToEditForm(padCrossedBlock, editBlockForm);

    assertThat(editBlockForm.getBlockOwnersOuIdList()).isEmpty();
    assertThat(editBlockForm.getCrossingOwner()).isEqualTo(CrossingOwner.HOLDER);

  }

  @Test
  void getCrossedBlockView_Valid() {
    padCrossedBlock.setBlockOwner(CrossingOwner.UNLICENSED);
    padCrossedBlock.setId(1);
    var owner = new PadCrossedBlockOwner(padCrossedBlock, null, "MANUAL");
    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    when(padCrossedBlockOwnerRepository.findByPadCrossedBlockIn(eq(List.of(padCrossedBlock))))
        .thenReturn(List.of(owner));

    var result = blockCrossingService.getCrossedBlockView(pwaApplicationDetail, 1);
    assertThat(result.getId()).isEqualTo(1);
  }

  @Test
  void getCrossedBlockView_Invalid() {
    padCrossedBlock.setBlockOwner(CrossingOwner.UNLICENSED);
    padCrossedBlock.setId(1);
    var owner = new PadCrossedBlockOwner(padCrossedBlock, null, "MANUAL");
    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
          .thenReturn(List.of(padCrossedBlock));
    when(padCrossedBlockOwnerRepository.findByPadCrossedBlockIn(eq(List.of(padCrossedBlock))))
          .thenReturn(List.of(owner));
    assertThrows(PwaEntityNotFoundException.class, () ->

      blockCrossingService.getCrossedBlockView(pwaApplicationDetail, 2));
  }

  @Test
  void isDocumentsRequired_notRequiredHolderOwned() {
    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerNot(
        pwaApplicationDetail, CrossingOwner.HOLDER)).thenReturn(0);
    assertThat(blockCrossingService.isDocumentsRequired(pwaApplicationDetail)).isFalse();
  }

  @Test
  void isDocumentsRequired_notRequiredDepConType() {
    var pwaApplication = new PwaApplication(null, PwaApplicationType.DEPOSIT_CONSENT, null);
    pwaApplicationDetail.setPwaApplication(pwaApplication);
    assertThat(blockCrossingService.isDocumentsRequired(pwaApplicationDetail)).isFalse();
  }

  @Test
  void isDocumentsRequired_required() {
    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerNot(
        pwaApplicationDetail, CrossingOwner.HOLDER)).thenReturn(1);
    assertThat(blockCrossingService.isDocumentsRequired(pwaApplicationDetail)).isTrue();
  }

  @Test
  void isComplete_noDocsRequired_valid() {
    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(1);
    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerNot(
        pwaApplicationDetail, CrossingOwner.HOLDER)).thenReturn(0);
    assertThat(blockCrossingService.isComplete(pwaApplicationDetail)).isTrue();
  }

  @Test
  void isComplete_docsRequired_valid() {
    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(1);
    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerNot(
        pwaApplicationDetail, CrossingOwner.HOLDER)).thenReturn(1);
    when(blockCrossingFileService.isComplete(pwaApplicationDetail)).thenReturn(true);
    assertThat(blockCrossingService.isComplete(pwaApplicationDetail)).isTrue();
  }

  @Test
  void isComplete_noBlocks_invalid() {
    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(0);
    assertThat(blockCrossingService.isComplete(pwaApplicationDetail)).isFalse();
  }

  @Test
  void isComplete_docsRequiredAndNotProvided_invalid() {
    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(1);
    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockOwnerNot(
        pwaApplicationDetail, CrossingOwner.HOLDER)).thenReturn(1);
    assertThat(blockCrossingService.isComplete(pwaApplicationDetail)).isFalse();
  }

  @Test
  void doesBlockExistOnApp_exists() {
    var pearsBlock = new PearsBlock(null, null, "ref", null, null, null, null);

    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockReference(
        pwaApplicationDetail, pearsBlock.getBlockReference())).thenReturn(1);

    var doesBlockExistOnApp = blockCrossingService.doesBlockExistOnApp(pwaApplicationDetail, pearsBlock);
    assertThat(doesBlockExistOnApp).isTrue();
  }

  @Test
  void doesBlockExistOnApp_doesNotExist() {
    var pearsBlock = new PearsBlock(null, null, "ref", null, null, null, null);

    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockReference(
        pwaApplicationDetail, pearsBlock.getBlockReference())).thenReturn(0);

    var doesBlockExistOnApp = blockCrossingService.doesBlockExistOnApp(pwaApplicationDetail, pearsBlock);
    assertThat(doesBlockExistOnApp).isFalse();
  }

}
