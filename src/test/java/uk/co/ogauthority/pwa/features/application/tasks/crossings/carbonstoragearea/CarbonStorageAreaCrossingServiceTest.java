package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.CrossingOwner;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarbonStorageAreaCrossingServiceTest {

  @Mock
  private PadCrossedStorageAreaRepository crossedStorageAreaRepository;

  @Mock
  private PadCrossedStorageAreaOwnerRepository crossedStorageAreaOwnerRepository;

  @Mock
  private CarbonStorageAreaCrossingFileService fileService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  private CarbonStorageAreaCrossingService carbonStorageAreaCrossingService;

  private PadCrossedStorageArea padCrossedStorageArea;

  private List<PadCrossedStorageAreaOwner> padCrossedStorageAreaOwners;

  private PwaApplicationDetail pwaApplicationDetail;

  private int APP_ID;
  private final int CROSSED_AREA_ID = 90;
  private final String AREA_REF = "1/2/3";

  @BeforeEach
  void setup() {
    carbonStorageAreaCrossingService = new CarbonStorageAreaCrossingService(
        crossedStorageAreaRepository,
        crossedStorageAreaOwnerRepository,
        portalOrganisationsAccessor,
        fileService);

    var pwa = new PwaApplication();
    pwa.setResourceType(PwaResourceType.CCUS);
    pwa.setApplicationType(PwaApplicationType.INITIAL);

    pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setPwaApplication(pwa);

    padCrossedStorageArea = new PadCrossedStorageArea();
    padCrossedStorageArea.setId(CROSSED_AREA_ID);
    padCrossedStorageArea.setPwaApplicationDetail(pwaApplicationDetail);
    padCrossedStorageArea.setCrossingOwnerType(CrossingOwner.HOLDER);
    padCrossedStorageArea.setStorageAreaReference(AREA_REF);

    var crossedAreaOwner = new PadCrossedStorageAreaOwner();
    crossedAreaOwner.setId(1);
    crossedAreaOwner.setPadCrossedStorageArea(padCrossedStorageArea);
    crossedAreaOwner.setOwnerOuId(700);
    padCrossedStorageAreaOwners = List.of(crossedAreaOwner);

    when(crossedStorageAreaOwnerRepository.findAllByPadCrossedStorageArea(padCrossedStorageArea)).thenReturn(padCrossedStorageAreaOwners);
  }

  @Test
  void canShowInTaskList_nonCCUS_noCrossings() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.PETROLEUM);

    var applicationDetail = new PwaApplicationDetail();
    applicationDetail.setPwaApplication(application);
    applicationDetail.setCsaCrossed(false);

    assertFalse(carbonStorageAreaCrossingService.canShowInTaskList(applicationDetail));
  }

  @Test
  void canShowInTaskList_nonCCUS_crossingsNull() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.PETROLEUM);

    var applicationDetail = new PwaApplicationDetail();
    applicationDetail.setPwaApplication(application);
    applicationDetail.setCsaCrossed(null);

    assertFalse(carbonStorageAreaCrossingService.canShowInTaskList(applicationDetail));
  }

  @Test
  void canShowInTaskList_CCUS_noCrossings() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.CCUS);

    var applicationDetail = new PwaApplicationDetail();
    applicationDetail.setPwaApplication(application);
    applicationDetail.setCsaCrossed(false);

    assertTrue(carbonStorageAreaCrossingService.canShowInTaskList(applicationDetail));
  }

  @Test
  void canShowInTaskList_nonCCUS_Crossings() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.PETROLEUM);

    var applicationDetail = new PwaApplicationDetail();
    applicationDetail.setPwaApplication(application);
    applicationDetail.setCsaCrossed(true);

    assertTrue(carbonStorageAreaCrossingService.canShowInTaskList(applicationDetail));
  }

  @Test
  void canShowInTaskList_CCUS_Crossings() {
    var application = new PwaApplication();
    application.setResourceType(PwaResourceType.CCUS);

    var applicationDetail = new PwaApplicationDetail();
    applicationDetail.setPwaApplication(application);
    applicationDetail.setCsaCrossed(true);

    assertTrue(carbonStorageAreaCrossingService.canShowInTaskList(applicationDetail));
  }

  @Test
  void getCrossedAreaViews_whenHolderIsOwner_andUnlicensedBlock() {
    when(crossedStorageAreaRepository.findAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedStorageArea));
    var views = carbonStorageAreaCrossingService.getCrossedAreaViews(pwaApplicationDetail);

    assertThat(views).isNotEmpty().allSatisfy(cav -> {
      assertThat(cav.getOperatorList()).isEmpty();
      assertThat(cav.getOwnedCompletelyByHolder()).isTrue();
      assertThat(cav.getStorageAreaReference()).isEqualTo(AREA_REF);
      assertThat(cav.getId()).isEqualTo(padCrossedStorageArea.getId());
    });
  }

  @Test
  void getCrossedAreaViews_whenOrgUnitIsOwner_andUnlicensedBlock() {
    var orgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    var owner = new PadCrossedStorageAreaOwner(
        padCrossedStorageArea,
        PortalOrganisationTestUtils.DEFAULT_UNIT_ID,
        null);
    padCrossedStorageArea.setCrossingOwnerType(CrossingOwner.PORTAL_ORGANISATION);

    when(crossedStorageAreaRepository.findAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedStorageArea));

    when(crossedStorageAreaOwnerRepository.findAllByPadCrossedStorageAreaIn(eq(List.of(padCrossedStorageArea))))
        .thenReturn(List.of(owner));

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(any()))
        .thenReturn(List.of(orgUnit));

    var views = carbonStorageAreaCrossingService.getCrossedAreaViews(pwaApplicationDetail);

    assertThat(views).isNotEmpty().allSatisfy(cav -> {
      assertThat(cav.getOperatorList().get(0)).isEqualTo(orgUnit.getName());
      assertThat(cav.getOwnedCompletelyByHolder()).isFalse();
      assertThat(cav.getStorageAreaReference()).isEqualTo(AREA_REF);
      assertThat(cav.getId()).isEqualTo(padCrossedStorageArea.getId());
    });
  }

  @Test
  void getCrossedAreaViews_whenManualOrgIsOwner_andUnlicensedBlock() {

    padCrossedStorageArea.setCrossingOwnerType(CrossingOwner.UNLICENSED);
    var owner = new PadCrossedStorageAreaOwner(padCrossedStorageArea, null, "MANUAL");
    when(crossedStorageAreaRepository.findAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padCrossedStorageArea));

    when(crossedStorageAreaOwnerRepository.findAllByPadCrossedStorageAreaIn(eq(List.of(padCrossedStorageArea))))
        .thenReturn(List.of(owner));

    var views = carbonStorageAreaCrossingService.getCrossedAreaViews(pwaApplicationDetail);

    assertThat(views).isNotEmpty().allSatisfy(cav -> {
      assertThat(cav.getOperatorList().get(0)).isEqualTo(owner.getOwnerName());
      assertThat(cav.getOwnedCompletelyByHolder()).isFalse();
      assertThat(cav.getStorageAreaReference()).isEqualTo(AREA_REF);
      assertThat(cav.getId()).isEqualTo(padCrossedStorageArea.getId());
    });
  }

  @Test
  void mapEntityToEditForm() {
    var expectedOwners = padCrossedStorageAreaOwners
        .stream()
        .map(PadCrossedStorageAreaOwner::getOwnerOuId)
        .collect(Collectors.toList());
    var actualEditForm = carbonStorageAreaCrossingService.mapToEditForm(padCrossedStorageArea, new EditCarbonStorageAreaCrossingForm());
    assertThat(actualEditForm.getCrossingOwner()).isEqualTo(padCrossedStorageArea.getCrossingOwnerType());
    assertThat(actualEditForm.getOwnersOuIdList()).isEqualTo(expectedOwners);
  }

  @Test
  void isComplete_noDocsRequired_valid() {
    when(crossedStorageAreaRepository.countPadCrossedStorageAreaByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(1);
    when(crossedStorageAreaRepository.countPadCrossedStorageAreaByPwaApplicationDetailAndCrossingOwnerTypeNot(
        pwaApplicationDetail, CrossingOwner.HOLDER)).thenReturn(0);
    assertThat(carbonStorageAreaCrossingService.isComplete(pwaApplicationDetail)).isTrue();
  }

  @Test
  void isComplete_noAreas_invalid() {
    when(crossedStorageAreaRepository.countPadCrossedStorageAreaByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(0);
    assertThat(carbonStorageAreaCrossingService.isComplete(pwaApplicationDetail)).isFalse();
  }

  @Test
  void isComplete_docsRequiredAndNotProvided_invalid() {
    when(crossedStorageAreaRepository.countPadCrossedStorageAreaByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(1);
    when(crossedStorageAreaRepository.countPadCrossedStorageAreaByPwaApplicationDetailAndCrossingOwnerTypeNot(
        pwaApplicationDetail, CrossingOwner.HOLDER)).thenReturn(1);
    assertThat(carbonStorageAreaCrossingService.isComplete(pwaApplicationDetail)).isFalse();
  }
}
