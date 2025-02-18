package uk.co.ogauthority.pwa.service.pwaapplications;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnitTestUtil;
import uk.co.ogauthority.pwa.repository.search.consents.PwaHolderOrgUnitRepository;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
class PwaHolderServiceTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;
  @Mock
  private PwaHolderOrgUnitRepository pwaHolderOrgUnitRepository;

  private PwaHolderService pwaHolderService;

  private PwaApplication pwaApplication;
  private MasterPwa masterPwa;

  @BeforeEach
  void setUp() {
    pwaHolderService = new PwaHolderService(portalOrganisationsAccessor, pwaHolderOrgUnitRepository);

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();
    masterPwa = pwaApplication.getMasterPwa();
  }


  @Test
  void getHolderOrgGroupsForMasterPwaIds_correctlyCreatesMultimap() {

    var holderOrgGroup = PortalOrganisationTestUtils.generateOrganisationGroup(1, "O", "O");
    var holderOrgUnit = PortalOrganisationTestUtils.generateOrganisationUnit(1, "OO", holderOrgGroup);
    var pwaHolderOrgUnit = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("h1", masterPwa.getId(), holderOrgUnit);

    when(pwaHolderOrgUnitRepository.findAllByPwaIdIn(Set.of(masterPwa.getId())))
        .thenReturn(Set.of(pwaHolderOrgUnit));
    when(portalOrganisationsAccessor.getOrganisationGroupsWhereIdIn(List.of(pwaHolderOrgUnit.getOrgGrpId())))
        .thenReturn(List.of(holderOrgGroup));
    Multimap<PortalOrganisationGroup, Integer> orgToMasterPwaIdMultimap = ArrayListMultimap.create();
    orgToMasterPwaIdMultimap.put(holderOrgGroup, masterPwa.getId());
    assertThat(pwaHolderService.getHolderOrgGroupsForMasterPwaIds(Set.of(masterPwa.getId()))).isEqualTo(
        orgToMasterPwaIdMultimap);
  }

  @Test
  void getPwaHolderOrgUnits() {

    var ou = PortalOrganisationTestUtils.generateOrganisationUnit(1, "Umbrella");
    var holderOu = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("h1", masterPwa.getId(), ou);
    when(pwaHolderOrgUnitRepository.findAllByPwaId(masterPwa.getId())).thenReturn(Set.of(holderOu));
    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(List.of(ou.getOuId()))).thenReturn(List.of(ou));

    var expectedOus = pwaHolderService.getPwaHolderOrgUnits(masterPwa);

    assertThat(expectedOus).containsExactly(ou);

  }

}
