package uk.co.ogauthority.pwa.service.pwaapplications;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnitTestUtil;
import uk.co.ogauthority.pwa.repository.search.consents.PwaHolderOrgUnitRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaHolderServiceTest {

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;
  @Mock
  private PwaHolderOrgUnitRepository pwaHolderOrgUnitRepository;

  private PwaHolderService pwaHolderService;

  private PwaApplication pwaApplication;
  private MasterPwa masterPwa;

  @Before
  public void setUp() {
    pwaHolderService = new PwaHolderService(portalOrganisationsAccessor, pwaHolderOrgUnitRepository);

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();
    masterPwa = pwaApplication.getMasterPwa();
  }


  @Test
  public void getHolderOrgGroupsForMasterPwaIds_correctlyCreatesMultimap() {

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

}
