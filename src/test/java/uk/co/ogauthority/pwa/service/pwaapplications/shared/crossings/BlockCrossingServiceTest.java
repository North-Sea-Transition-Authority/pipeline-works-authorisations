package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.BlockLocation;
import uk.co.ogauthority.pwa.model.entity.enums.LicenceStatus;
import uk.co.ogauthority.pwa.model.entity.licence.PearsBlock;
import uk.co.ogauthority.pwa.model.entity.licence.PearsLicence;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.CrossedBlockOwner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlock;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.PadCrossedBlockOwner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.AddBlockCrossingForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.EditBlockCrossingForm;
import uk.co.ogauthority.pwa.repository.licence.PadCrossedBlockOwnerRepository;
import uk.co.ogauthority.pwa.repository.licence.PadCrossedBlockRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.licence.PearsBlockService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class BlockCrossingServiceTest {

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
  private PadFileService padFileService;

  private Clock clock = Clock.systemDefaultZone();

  private BlockCrossingService blockCrossingService;

  private PwaApplicationDetail pwaApplicationDetail;

  private PadCrossedBlock padCrossedBlock;

  private AddBlockCrossingForm addBlockForm;
  private EditBlockCrossingForm editBlockForm;

  private PearsLicence licence;

  private PearsBlock licensedBlock;

  private PortalOrganisationUnit organisationUnit;

  @Before
  public void setUp() throws Exception {
    blockCrossingService = new BlockCrossingService(
        padCrossedBlockRepository,
        padCrossedBlockOwnerRepository,
        pearsBlockService,
        portalOrganisationsAccessor,
        blockCrossingFileService,
        clock,
        entityCopyingService,
        padFileService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    APP_ID = pwaApplicationDetail.getMasterPwaApplicationId();
    padCrossedBlock = new PadCrossedBlock();
    padCrossedBlock.setId(CROSSED_BLOCK_ID);
    padCrossedBlock.setPwaApplicationDetail(pwaApplicationDetail);
    padCrossedBlock.setBlockOwner(CrossedBlockOwner.HOLDER);
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

    organisationUnit = PortalOrganisationTestUtils.getOrganisationUnit();
  }

  @Test
  public void getCrossedBlockByIdAndApplicationDetail_whenBlockFound_andHasMatchingDetail() {
    assertThat(blockCrossingService.getCrossedBlockByIdAndApplicationDetail(CROSSED_BLOCK_ID, pwaApplicationDetail))
        .isEqualTo(padCrossedBlock);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getCrossedBlockByIdAndApplicationDetail_whenBlockFound_andHasNonMatchingDetail() {
    var otherDetail = new PwaApplicationDetail();
    otherDetail.setId(9999);
    padCrossedBlock.setPwaApplicationDetail(otherDetail);
    blockCrossingService.getCrossedBlockByIdAndApplicationDetail(CROSSED_BLOCK_ID, pwaApplicationDetail);

  }

  @Test
  public void getCrossedBlockViews_whenHolderIsOwner_andUnlicensedBlock() {

    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    var views = blockCrossingService.getCrossedBlockViews(pwaApplicationDetail);

    assertThat(views).allSatisfy(bcv -> {
      assertThat(bcv.getBlockOperatorList()).isEmpty();
      assertThat(bcv.getBlockOwnedCompletelyByHolder()).isTrue();
      assertThat(bcv.getBlockReference()).isEqualTo(BLOCK_REF);
      assertThat(bcv.getId()).isEqualTo(padCrossedBlock.getId());
      assertThat(bcv.getLicenceReference()).isEqualTo("Unlicensed");

    });
  }

  @Test
  public void getCrossedBlockViews_whenHolderIsOwner_andLicensedBlock() {

    var licence = new PearsLicence(1, "P", 1, "P1", LicenceStatus.EXTANT);
    padCrossedBlock.setLicence(licence);
    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    var views = blockCrossingService.getCrossedBlockViews(pwaApplicationDetail);

    assertThat(views).allSatisfy(bcv -> {
      assertThat(bcv.getBlockOperatorList()).isEmpty();
      assertThat(bcv.getBlockOwnedCompletelyByHolder()).isTrue();
      assertThat(bcv.getBlockReference()).isEqualTo(BLOCK_REF);
      assertThat(bcv.getId()).isEqualTo(padCrossedBlock.getId());
      assertThat(bcv.getLicenceReference()).isEqualTo(licence.getLicenceName());

    });

  }

  @Test
  public void getCrossedBlockViews_whenOrgUnitIsOwner_andUnlicensedBlock() {
    var orgUnit = PortalOrganisationTestUtils.getOrganisationUnit();
    var owner = new PadCrossedBlockOwner(padCrossedBlock, PortalOrganisationTestUtils.DEFAULT_UNIT_ID, null);
    padCrossedBlock.setBlockOwner(CrossedBlockOwner.PORTAL_ORGANISATION);

    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    when(padCrossedBlockOwnerRepository.findByPadCrossedBlockIn(eq(List.of(padCrossedBlock))))
        .thenReturn(List.of(owner));

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(any()))
        .thenReturn(List.of(orgUnit));

    var views = blockCrossingService.getCrossedBlockViews(pwaApplicationDetail);

    assertThat(views).allSatisfy(bcv -> {
      assertThat(bcv.getBlockOperatorList().get(0)).isEqualTo(orgUnit.getName());
      assertThat(bcv.getBlockOwnedCompletelyByHolder()).isFalse();
      assertThat(bcv.getBlockReference()).isEqualTo(BLOCK_REF);
      assertThat(bcv.getId()).isEqualTo(padCrossedBlock.getId());
    });

  }

  @Test
  public void getCrossedBlockViews_whenManualOrgIsOwner_andUnlicensedBlock() {

    padCrossedBlock.setBlockOwner(CrossedBlockOwner.UNLICENSED);
    var owner = new PadCrossedBlockOwner(padCrossedBlock, null, "MANUAL");
    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    when(padCrossedBlockOwnerRepository.findByPadCrossedBlockIn(eq(List.of(padCrossedBlock))))
        .thenReturn(List.of(owner));


    var views = blockCrossingService.getCrossedBlockViews(pwaApplicationDetail);

    assertThat(views).allSatisfy(bcv -> {
      assertThat(bcv.getBlockOperatorList().get(0)).isEqualTo(owner.getOwnerName());
      assertThat(bcv.getBlockOwnedCompletelyByHolder()).isFalse();
      assertThat(bcv.getBlockReference()).isEqualTo(BLOCK_REF);
      assertThat(bcv.getId()).isEqualTo(padCrossedBlock.getId());
    });

  }


  @Test
  public void updateAndSaveBlockCrossingAndOwnersFromForm_verifyRepositoryInteractionIsInCorrectOrder() {

    editBlockForm.setBlockOwnersOuIdList(List.of(PortalOrganisationTestUtils.DEFAULT_UNIT_ID));
    editBlockForm.setCrossedBlockOwner(CrossedBlockOwner.PORTAL_ORGANISATION);

    blockCrossingService.updateAndSaveBlockCrossingAndOwnersFromForm(padCrossedBlock, editBlockForm);

    InOrder verifyOrder = Mockito.inOrder(padCrossedBlockOwnerRepository, padCrossedBlockRepository);
    verifyOrder.verify(padCrossedBlockOwnerRepository).findByPadCrossedBlock(padCrossedBlock);
    verifyOrder.verify(padCrossedBlockOwnerRepository).deleteAll(any());
    verifyOrder.verify(padCrossedBlockOwnerRepository, times(1)).saveAll(any());
    verifyOrder.verify(padCrossedBlockRepository, times(1)).save(any());
    verifyOrder.verifyNoMoreInteractions();
  }

  @Test
  public void updateAndSaveBlockCrossingAndOwnersFromForm_whenPortalOrgOwner() {

    editBlockForm.setBlockOwnersOuIdList(List.of(PortalOrganisationTestUtils.DEFAULT_UNIT_ID));
    editBlockForm.setCrossedBlockOwner(CrossedBlockOwner.PORTAL_ORGANISATION);

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

    assertThat(blockCapture.getValue()).satisfies(padBlock -> {
      assertThat(padBlock.getBlockOwner()).isEqualTo(CrossedBlockOwner.PORTAL_ORGANISATION);
    });

  }

  @Test
  public void updateAndSaveBlockCrossingAndOwnersFromForm_whenUnlicenced() {

    editBlockForm.setCrossedBlockOwner(CrossedBlockOwner.UNLICENSED);

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

    assertThat(blockCapture.getValue()).satisfies(padBlock -> {
      assertThat(padBlock.getBlockOwner()).isEqualTo(CrossedBlockOwner.UNLICENSED);
    });
  }

  @Test
  public void mapBlockCrossingToEditForm_whenOrganisationUnitOwner() {

    var orgUnitOwner = new PadCrossedBlockOwner(padCrossedBlock, PortalOrganisationTestUtils.DEFAULT_UNIT_ID, null);
    when(padCrossedBlockOwnerRepository.findByPadCrossedBlock(padCrossedBlock)).thenReturn(List.of(orgUnitOwner));
    padCrossedBlock.setBlockOwner(CrossedBlockOwner.PORTAL_ORGANISATION);

    blockCrossingService.mapBlockCrossingToEditForm(padCrossedBlock, editBlockForm);

    assertThat(editBlockForm.getBlockOwnersOuIdList()).isEqualTo(List.of(PortalOrganisationTestUtils.DEFAULT_UNIT_ID));
    assertThat(editBlockForm.getCrossedBlockOwner()).isEqualTo(CrossedBlockOwner.PORTAL_ORGANISATION);

  }

  @Test
  public void mapBlockCrossingToEditForm_whenOtherOwner() {

    var orgUnitOwner = new PadCrossedBlockOwner(padCrossedBlock, null, "OTHER");
    when(padCrossedBlockOwnerRepository.findByPadCrossedBlock(padCrossedBlock)).thenReturn(List.of(orgUnitOwner));
    padCrossedBlock.setBlockOwner(CrossedBlockOwner.UNLICENSED);

    blockCrossingService.mapBlockCrossingToEditForm(padCrossedBlock, editBlockForm);

    assertThat(editBlockForm.getBlockOwnersOuIdList()).isEmpty();
    assertThat(editBlockForm.getCrossedBlockOwner()).isEqualTo(CrossedBlockOwner.UNLICENSED);

  }

  @Test
  public void mapBlockCrossingToEditForm_whenHolderOwner() {
    when(padCrossedBlockOwnerRepository.findByPadCrossedBlock(padCrossedBlock)).thenReturn(List.of());
    padCrossedBlock.setBlockOwner(CrossedBlockOwner.HOLDER);

    blockCrossingService.mapBlockCrossingToEditForm(padCrossedBlock, editBlockForm);

    assertThat(editBlockForm.getBlockOwnersOuIdList()).isEmpty();
    assertThat(editBlockForm.getCrossedBlockOwner()).isEqualTo(CrossedBlockOwner.HOLDER);

  }

  @Test
  public void getCrossedBlockView_Valid() {
    padCrossedBlock.setBlockOwner(CrossedBlockOwner.UNLICENSED);
    padCrossedBlock.setId(1);
    var owner = new PadCrossedBlockOwner(padCrossedBlock, null, "MANUAL");
    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    when(padCrossedBlockOwnerRepository.findByPadCrossedBlockIn(eq(List.of(padCrossedBlock))))
        .thenReturn(List.of(owner));

    var result = blockCrossingService.getCrossedBlockView(pwaApplicationDetail, 1);
    assertThat(result.getId()).isEqualTo(1);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getCrossedBlockView_Invalid() {
    padCrossedBlock.setBlockOwner(CrossedBlockOwner.UNLICENSED);
    padCrossedBlock.setId(1);
    var owner = new PadCrossedBlockOwner(padCrossedBlock, null, "MANUAL");
    when(padCrossedBlockRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedBlock));

    when(padCrossedBlockOwnerRepository.findByPadCrossedBlockIn(eq(List.of(padCrossedBlock))))
        .thenReturn(List.of(owner));

    blockCrossingService.getCrossedBlockView(pwaApplicationDetail, 2);
  }

  @Test
  public void doesBlockExistOnApp_exists() {
    var pearsBlock = new PearsBlock(null, null, "ref", null, null, null, null);

    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockReference(
        pwaApplicationDetail, pearsBlock.getBlockReference())).thenReturn(1);

    var doesBlockExistOnApp = blockCrossingService.doesBlockExistOnApp(pwaApplicationDetail, pearsBlock);
    assertThat(doesBlockExistOnApp).isTrue();
  }

  @Test
  public void doesBlockExistOnApp_doesNotExist() {
    var pearsBlock = new PearsBlock(null, null, "ref", null, null, null, null);

    when(padCrossedBlockRepository.countPadCrossedBlockByPwaApplicationDetailAndBlockReference(
        pwaApplicationDetail, pearsBlock.getBlockReference())).thenReturn(0);

    var doesBlockExistOnApp = blockCrossingService.doesBlockExistOnApp(pwaApplicationDetail, pearsBlock);
    assertThat(doesBlockExistOnApp).isFalse();
  }

}