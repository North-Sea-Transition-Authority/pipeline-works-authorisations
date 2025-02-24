package uk.co.ogauthority.pwa.features.application.tasks.crossings.pipeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;
import uk.co.ogauthority.pwa.service.searchselector.SearchSelectorService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PadPipelineCrossingOwnerServiceTest {

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

  @BeforeEach
  void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    padPipelineCrossing = new PadPipelineCrossing();
    padPipelineCrossingOwnerService = new PadPipelineCrossingOwnerService(padPipelineCrossingOwnerRepository,
        portalOrganisationsAccessor, searchSelectorService);

    padPipelineCrossingOwner = new PadPipelineCrossingOwner();
    when(padPipelineCrossingOwnerRepository.findAllByPadPipelineCrossing(padPipelineCrossing))
        .thenReturn(List.of(padPipelineCrossingOwner));

    portalOrganisationUnit = PortalOrganisationTestUtils.generateOrganisationUnit(ORG_ID, ORG_NAME);
  }

  @Test
  void getOwnersForCrossing() {
    var result = padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing);
    assertThat(result).containsExactly(padPipelineCrossingOwner);
  }

  @Test
  void getOwnerPrepopulationFormAttribute_LinkedEntry() {
    padPipelineCrossingOwner.setOrganisationUnit(portalOrganisationUnit);
    var result = padPipelineCrossingOwnerService.getOwnerPrepopulationFormAttribute(padPipelineCrossing);
    assertThat(result).containsExactly(
        entry(String.valueOf(ORG_ID), ORG_NAME)
    );
  }

  @Test
  void getOwnerPrepopulationFormAttribute_ManualEntry() {
    var owner = new PadPipelineCrossingOwner();
    when(padPipelineCrossingOwnerRepository.findAllByPadPipelineCrossing(padPipelineCrossing))
        .thenReturn(List.of(owner));
    var result = padPipelineCrossingOwnerService.getOwnersForCrossing(padPipelineCrossing);
    assertThat(result).containsExactly(owner);
  }

  @Test
  void createOwners_FullyOwned() {
    var form = new PipelineCrossingForm();
    form.setPipelineFullyOwnedByOrganisation(true);
    padPipelineCrossingOwnerService.createOwners(padPipelineCrossing, form);
    verify(padPipelineCrossingOwnerRepository, times(1)).deleteAll(any());
    verify(padPipelineCrossingOwnerRepository, never()).save(any());
  }

  @Test
  void createOwners_NotFullyOwned_LinkedEntry() {
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
  void createOwners_NotFullyOwned_ManualEntry() {
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
  void removeAllForCrossing() {
    padPipelineCrossingOwnerService.removeAllForCrossing(padPipelineCrossing);
    verify(padPipelineCrossingOwnerRepository, times(1)).deleteAll(any());
  }
}