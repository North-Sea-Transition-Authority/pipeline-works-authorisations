package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PadPipelineCrossingServiceTest {

  @Mock
  private PadPipelineCrossingRepository padPipelineCrossingRepository;

  @Mock
  private PipelineCrossingFileService pipelineCrossingFileService;

  @Mock
  private PadPipelineCrossingOwnerService padPipelineCrossingOwnerService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Mock
  private PadFileService padFileService;

  @Spy
  private SearchSelectorService searchSelectorService;

  private PadPipelineCrossingService padPipelineCrossingService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PadPipelineCrossing padPipelineCrossing;

  @BeforeEach
  void setUp() {
    padPipelineCrossingService = new PadPipelineCrossingService(
        padPipelineCrossingRepository,
        pipelineCrossingFileService,
        padPipelineCrossingOwnerService,
        portalOrganisationsAccessor,
        searchSelectorService,
        entityCopyingService,
        padFileService
    );

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    padPipelineCrossing = new PadPipelineCrossing();
  }

  @Test
  void getPipelineCrossing_Found() {
    when(padPipelineCrossingRepository.getByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.of(padPipelineCrossing));
    var result = padPipelineCrossingService.getPipelineCrossing(pwaApplicationDetail, 1);
    assertThat(result).isEqualTo(padPipelineCrossing);
  }

  @Test
  void getPipelineCrossing_NotFound() {
    when(padPipelineCrossingRepository.getByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
          .thenReturn(Optional.empty());
    assertThrows(PwaEntityNotFoundException.class, () ->
      padPipelineCrossingService.getPipelineCrossing(pwaApplicationDetail, 1));
  }

  @Test
  void deleteCascade() {
    padPipelineCrossingService.deleteCascade(padPipelineCrossing);
    verify(padPipelineCrossingOwnerService, times(1)).removeAllForCrossing(padPipelineCrossing);
    verify(padPipelineCrossingRepository, times(1)).delete(padPipelineCrossing);
  }

  @Test
  void getPipelineCrossingViews_LinkedOwner() {
    var CROSSED_TEXT = "Pipeline Crossed";
    padPipelineCrossing.setPipelineCrossed(CROSSED_TEXT);
    padPipelineCrossing.setId(1);
    when(padPipelineCrossingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padPipelineCrossing));

    var UNIT_NAME = "UNIT";
    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, UNIT_NAME);

    var owner = new PadPipelineCrossingOwner();
    owner.setOrganisationUnit(orgUnit);
    when(padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing))
        .thenReturn(List.of(owner));

    var result = padPipelineCrossingService.getPipelineCrossingViews(pwaApplicationDetail);
    assertThat(result).extracting(PipelineCrossingView::getId, PipelineCrossingView::getOwners,
        PipelineCrossingView::getReference).containsExactly(
        Tuple.tuple(padPipelineCrossing.getId(), UNIT_NAME, CROSSED_TEXT)
    );
  }

  @Test
  void getPipelineCrossingViews_ManualOwner() {
    var CROSSED_TEXT = "Pipeline Crossed";
    padPipelineCrossing.setPipelineCrossed(CROSSED_TEXT);
    padPipelineCrossing.setId(1);
    when(padPipelineCrossingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padPipelineCrossing));

    var UNIT_NAME = "UNIT";

    var owner = new PadPipelineCrossingOwner();
    owner.setManualOrganisationEntry(UNIT_NAME);
    when(padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing))
        .thenReturn(List.of(owner));

    var result = padPipelineCrossingService.getPipelineCrossingViews(pwaApplicationDetail);
    assertThat(result).extracting(PipelineCrossingView::getId, PipelineCrossingView::getOwners,
        PipelineCrossingView::getReference).containsExactly(
        Tuple.tuple(padPipelineCrossing.getId(), UNIT_NAME, CROSSED_TEXT)
    );
  }

  @Test
  void getPipelineCrossingViews_OrganisationIsFullOwner() {
    var CROSSED_TEXT = "Pipeline Crossed";
    padPipelineCrossing.setPipelineCrossed(CROSSED_TEXT);
    padPipelineCrossing.setId(1);
    when(padPipelineCrossingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padPipelineCrossing));

    when(padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing))
        .thenReturn(List.of());

    var result = padPipelineCrossingService.getPipelineCrossingViews(pwaApplicationDetail);
    assertThat(result).extracting(PipelineCrossingView::getId, PipelineCrossingView::getOwners,
        PipelineCrossingView::getReference).containsExactly(
            Tuple.tuple(padPipelineCrossing.getId(), "Pipeline belongs to the holder", CROSSED_TEXT)
    );
  }

  @Test
  void getPipelineCrossingView() {
    var CROSSED_TEXT = "Pipeline Crossed";
    padPipelineCrossing.setPipelineCrossed(CROSSED_TEXT);
    padPipelineCrossing.setId(1);

    var UNIT_NAME = "UNIT";
    var orgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, UNIT_NAME);

    var owner = new PadPipelineCrossingOwner();
    owner.setOrganisationUnit(orgUnit);
    when(padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing))
        .thenReturn(List.of(owner));

    var result = padPipelineCrossingService.getPipelineCrossingView(padPipelineCrossing);
    assertThat(result.getId()).isEqualTo(padPipelineCrossing.getId());
    assertThat(result.getOwners()).isEqualTo(UNIT_NAME);
    assertThat(result.getReference()).isEqualTo(CROSSED_TEXT);
  }

  @Test
  void createPipelineCrossings() {
    var form = new PipelineCrossingForm();
    form.setPipelineCrossed("Crossed");
    form.setPipelineFullyOwnedByOrganisation(false);
    padPipelineCrossingService.createPipelineCrossings(pwaApplicationDetail, form);
    verify(padPipelineCrossingOwnerService, times(1)).createOwners(any(), eq(form));
    verify(padPipelineCrossingRepository, times(1)).save(any());
  }

  @Test
  void updatePipelineCrossing() {
    var form = new PipelineCrossingForm();
    form.setPipelineCrossed("Crossed");
    form.setPipelineFullyOwnedByOrganisation(false);
    var crossing = new PadPipelineCrossing();
    padPipelineCrossingService.updatePipelineCrossing(crossing, form);
    verify(padPipelineCrossingOwnerService, times(1)).createOwners(crossing, form);
    verify(padPipelineCrossingRepository, times(1)).save(crossing);
  }

  @Test
  void getPrepopulatedSearchSelectorItems_Empty() {
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(any())).thenReturn(List.of());
    var result = padPipelineCrossingService.getPrepopulatedSearchSelectorItems(List.of());
    assertThat(result).isEmpty();
  }

  @Test
  void getPrepopulatedSearchSelectorItems_NotEmpty() {
    var selectionIds = List.of("1", SearchSelectable.FREE_TEXT_PREFIX + "Test");
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(any())).thenReturn(List.of(
        PortalOrganisationTestUtils.generateOrganisationUnit(1, "Test")
    ));
    var result = padPipelineCrossingService.getPrepopulatedSearchSelectorItems(selectionIds);
    assertThat(result).containsExactly(
        entry("1", "Test"),
        entry(SearchSelectable.FREE_TEXT_PREFIX + "Test", "Test")
    );
  }
}