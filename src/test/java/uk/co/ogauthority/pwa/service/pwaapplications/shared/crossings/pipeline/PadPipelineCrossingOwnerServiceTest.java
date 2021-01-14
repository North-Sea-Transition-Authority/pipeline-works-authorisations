package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossing;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.crossings.pipelines.PadPipelineCrossingOwner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.PipelineCrossingForm;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineCrossingOwnerRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineCrossingOwnerServiceTest {

  @Mock
  private PadPipelineCrossingOwnerRepository padPipelineCrossingOwnerRepository;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Spy
  private SearchSelectorService searchSelectorService;

  private PadPipelineCrossingOwnerService padPipelineCrossingOwnerService;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadPipelineCrossing padPipelineCrossing;
  private PadPipelineCrossingOwner padPipelineCrossingOwner;

  private PortalOrganisationUnit portalOrganisationUnit;
  private static int ORG_ID = 1;
  private static String ORG_NAME = "ORG";

  @Before
  public void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    padPipelineCrossing = new PadPipelineCrossing();
    padPipelineCrossingOwnerService = new PadPipelineCrossingOwnerService(padPipelineCrossingOwnerRepository,
        portalOrganisationsAccessor, searchSelectorService);

    padPipelineCrossingOwner = new PadPipelineCrossingOwner();
    when(padPipelineCrossingOwnerRepository.findAllByPadPipelineCrossing(padPipelineCrossing))
        .thenReturn(List.of(padPipelineCrossingOwner));

    portalOrganisationUnit = new PortalOrganisationUnit(ORG_ID, ORG_NAME);
  }

  @Test
  public void getOwnersForCrossing() {
    var result = padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing);
    assertThat(result).containsExactly(padPipelineCrossingOwner);
  }

  @Test
  public void getOwnerPrepopulationFormAttribute_LinkedEntry() {
    padPipelineCrossingOwner.setOrganisationUnit(portalOrganisationUnit);
    var result = padPipelineCrossingOwnerService.getOwnerPrepopulationFormAttribute(padPipelineCrossing);
    assertThat(result).containsExactly(
        entry(String.valueOf(ORG_ID), ORG_NAME)
    );
  }

  @Test
  public void getOwnerPrepopulationFormAttribute_ManualEntry() {
    var owner = new PadPipelineCrossingOwner();
    when(padPipelineCrossingOwnerRepository.findAllByPadPipelineCrossing(padPipelineCrossing))
        .thenReturn(List.of(owner));
    var result = padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing);
    assertThat(result).containsExactly(owner);
  }

  @Test
  public void createOwners_FullyOwned() {
    var form = new PipelineCrossingForm();
    form.setPipelineFullyOwnedByOrganisation(true);
    padPipelineCrossingOwnerService.createOwners(padPipelineCrossing, form);
    verify(padPipelineCrossingOwnerRepository, times(1)).deleteAll(any());
    verify(padPipelineCrossingOwnerRepository, never()).save(any());
  }

  @Test
  public void createOwners_NotFullyOwned_LinkedEntry() {
    var form = new PipelineCrossingForm();
    form.setPipelineFullyOwnedByOrganisation(false);
    form.setPipelineOwners(List.of(String.valueOf(ORG_ID)));
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(any()))
        .thenReturn(List.of(portalOrganisationUnit));

    padPipelineCrossingOwnerService.createOwners(padPipelineCrossing, form);
    verify(padPipelineCrossingOwnerRepository, times(1)).deleteAll(any());

    var captor = ArgumentCaptor.forClass(PadPipelineCrossingOwner.class);
    verify(padPipelineCrossingOwnerRepository, times(1)).save(captor.capture());
    assertThat(captor.getValue()).extracting(PadPipelineCrossingOwner::getOrganisationUnit)
        .isEqualTo(portalOrganisationUnit);
  }

  @Test
  public void createOwners_NotFullyOwned_ManualEntry() {
    var form = new PipelineCrossingForm();
    form.setPipelineFullyOwnedByOrganisation(false);
    form.setPipelineOwners(List.of(SearchSelectable.FREE_TEXT_PREFIX + ORG_NAME));

    padPipelineCrossingOwnerService.createOwners(padPipelineCrossing, form);
    verify(padPipelineCrossingOwnerRepository, times(1)).deleteAll(any());

    var captor = ArgumentCaptor.forClass(PadPipelineCrossingOwner.class);
    verify(padPipelineCrossingOwnerRepository, times(1)).save(captor.capture());
    assertThat(captor.getValue()).extracting(PadPipelineCrossingOwner::getManualOrganisationEntry)
        .isEqualTo(ORG_NAME);
  }

  @Test
  public void removeAllForCrossing() {
    padPipelineCrossingOwnerService.removeAllForCrossing(padPipelineCrossing);
    verify(padPipelineCrossingOwnerRepository, times(1)).deleteAll(any());
  }
}