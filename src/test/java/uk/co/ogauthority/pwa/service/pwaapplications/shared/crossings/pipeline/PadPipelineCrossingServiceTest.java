package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossingOwner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineCrossingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineCrossingServiceTest {

  @Mock
  private PadPipelineCrossingRepository padPipelineCrossingRepository;

  @Mock
  private PipelineCrossingFileService pipelineCrossingFileService;

  @Mock
  private PadPipelineCrossingOwnerService padPipelineCrossingOwnerService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  private PadPipelineCrossingService padPipelineCrossingService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PadPipelineCrossing padPipelineCrossing;

  @Before
  public void setUp() {
    padPipelineCrossingService = new PadPipelineCrossingService(padPipelineCrossingRepository,
        pipelineCrossingFileService, padPipelineCrossingOwnerService, portalOrganisationsAccessor);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    padPipelineCrossing = new PadPipelineCrossing();
  }

  @Test
  public void getPipelineCrossing_Found() {
    when(padPipelineCrossingRepository.getByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.of(padPipelineCrossing));
    var result = padPipelineCrossingService.getPipelineCrossing(pwaApplicationDetail, 1);
    assertThat(result).isEqualTo(padPipelineCrossing);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getPipelineCrossing_NotFound() {
    when(padPipelineCrossingRepository.getByPwaApplicationDetailAndId(pwaApplicationDetail, 1))
        .thenReturn(Optional.empty());
    padPipelineCrossingService.getPipelineCrossing(pwaApplicationDetail, 1);
  }

  @Test
  public void deleteCascade() {
    padPipelineCrossingService.deleteCascade(padPipelineCrossing);
    verify(padPipelineCrossingOwnerService, times(1)).removeAllForCrossing(padPipelineCrossing);
    verify(padPipelineCrossingRepository, times(1)).delete(padPipelineCrossing);
  }

  @Test
  public void getPipelineCrossingViews_LinkedOwner() {
    var CROSSED_TEXT = "Pipeline Crossed";
    padPipelineCrossing.setPipelineCrossed(CROSSED_TEXT);
    padPipelineCrossing.setId(1);
    when(padPipelineCrossingRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padPipelineCrossing));

    var UNIT_NAME = "UNIT";
    var orgUnit = new PortalOrganisationUnit(1, UNIT_NAME);

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
  public void getPipelineCrossingViews_ManualOwner() {
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
  public void getPipelineCrossingViews_OrganisationIsFullOwner() {
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
  public void getPipelineCrossingView() {
    var CROSSED_TEXT = "Pipeline Crossed";
    padPipelineCrossing.setPipelineCrossed(CROSSED_TEXT);
    padPipelineCrossing.setId(1);

    var UNIT_NAME = "UNIT";
    var orgUnit = new PortalOrganisationUnit(1, UNIT_NAME);

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
  public void createPipelineCrossings() {
    var form = new PipelineCrossingForm();
    form.setPipelineCrossed("Crossed");
    form.setPipelineFullyOwnedByOrganisation(false);
    padPipelineCrossingService.createPipelineCrossings(pwaApplicationDetail, form);
    verify(padPipelineCrossingOwnerService, times(1)).createOwners(any(), eq(form));
    verify(padPipelineCrossingRepository, times(1)).save(any());
  }

  @Test
  public void updatePipelineCrossing() {
    var form = new PipelineCrossingForm();
    form.setPipelineCrossed("Crossed");
    form.setPipelineFullyOwnedByOrganisation(false);
    var crossing = new PadPipelineCrossing();
    padPipelineCrossingService.updatePipelineCrossing(crossing, form);
    verify(padPipelineCrossingOwnerService, times(1)).createOwners(crossing, form);
    verify(padPipelineCrossingRepository, times(1)).save(crossing);
  }

  @Test
  public void getPreselectedItems_Empty() {
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(any())).thenReturn(List.of());
    var result = padPipelineCrossingService.getPreselectedItems(List.of());
    assertThat(result).isEmpty();
  }

  @Test
  public void getPreselectedItems_NotEmpty() {
    var selectionIds = List.of("1", SearchSelectable.FREE_TEXT_PREFIX + "Test");
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(any())).thenReturn(List.of(
        new PortalOrganisationUnit(1, "Test")
    ));
    var result = padPipelineCrossingService.getPreselectedItems(selectionIds);
    assertThat(result).containsExactly(
        entry("1", "Test"),
        entry(SearchSelectable.FREE_TEXT_PREFIX + "Test", "Test")
    );
  }
}