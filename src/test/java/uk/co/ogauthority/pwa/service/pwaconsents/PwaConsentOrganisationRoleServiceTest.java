package uk.co.ogauthority.pwa.service.pwaconsents;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentOrganisationRoleRepository;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@RunWith(MockitoJUnitRunner.class)
public class PwaConsentOrganisationRoleServiceTest {

  private static final int ORG_UNIT_ID_1 = 100;
  private static final int ORG_UNIT_ID_2 = 200;

  @Mock
  private PwaConsentOrganisationRoleRepository pwaConsentOrganisationRoleRepository;

  @Mock
  private PwaConsentPipelineOrganisationRoleLinkRepository pwaConsentPipelineOrganisationRoleLinkRepository;

  @Mock
  private PwaConsentRepository pwaConsentRepository;

  @Mock
  private PortalOrganisationUnit organisationUnit1;

  @Mock
  private PortalOrganisationGroup organisationGroup1;

  @Mock
  private PortalOrganisationUnit organisationUnit2;

  @Mock
  private PortalOrganisationGroup organisationGroup2;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PwaConsent pwaConsent;

  @Mock
  private MasterPwa masterPwa;

  private PwaConsentOrganisationRoleService pwaConsentOrganisationRoleService;

  private PwaConsentOrganisationRole pwaConsentHolderOrgRole;

  @Before
  public void setup() {
    when(organisationUnit1.getPortalOrganisationGroup()).thenReturn(organisationGroup1);
    when(organisationUnit1.getOuId()).thenReturn(ORG_UNIT_ID_1);

    when(organisationUnit2.getPortalOrganisationGroup()).thenReturn(organisationGroup2);
    when(organisationUnit2.getOuId()).thenReturn(ORG_UNIT_ID_2);

    when(pwaConsent.getMasterPwa()).thenReturn(masterPwa);

    when(pwaConsentRepository.findByMasterPwa(masterPwa)).thenReturn(List.of(pwaConsent));

    pwaConsentOrganisationRoleService = new PwaConsentOrganisationRoleService(
        pwaConsentOrganisationRoleRepository,
        pwaConsentPipelineOrganisationRoleLinkRepository,
        pwaConsentRepository,
        portalOrganisationsAccessor
    );

    pwaConsentHolderOrgRole = createOrgRole(pwaConsent, HuooRole.HOLDER, organisationUnit1);

  }

  private PwaConsentOrganisationRole createOrgRole(PwaConsent pwaConsent,
                                                   HuooRole huooRole,
                                                   PortalOrganisationUnit portalOrganisationUnit) {
    var orgRole = new PwaConsentOrganisationRole();
    orgRole.setAddedByPwaConsent(pwaConsent);
    orgRole.setRole(huooRole);
    orgRole.setType(HuooType.PORTAL_ORG);
    orgRole.setStartTimestamp(Instant.now());
    orgRole.setOrganisationUnitId(portalOrganisationUnit.getOuId());

    return orgRole;

  }


  @Test
  public void getPwaConsentsWhereCurrentHolderWasAdded_whenGivenOrgUnitIsHolder() {

    when(pwaConsentOrganisationRoleRepository.findByOrganisationUnitIdInAndRoleInAndEndTimestampIsNull(
        Set.of(ORG_UNIT_ID_1), Set.of(HuooRole.HOLDER)))
        .thenReturn(List.of(pwaConsentHolderOrgRole));

    assertThat(
        pwaConsentOrganisationRoleService.getPwaConsentsWhereCurrentHolderWasAdded(Set.of(organisationUnit1))
    ).containsExactly(pwaConsent);
  }

  @Test
  public void getPwaConsentsWhereCurrentHolderWasAdded_whenGivenOrgUnitIsNotAHolder() {

    assertThat(
        pwaConsentOrganisationRoleService.getPwaConsentsWhereCurrentHolderWasAdded(Set.of(organisationUnit1))
    ).isEmpty();
  }

  @Test
  public void getCurrentHoldersOrgRolesForMasterPwa_whenZeroHolders() {
    assertThat(pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(masterPwa)).isEmpty();
  }

  @Test
  public void getCurrentHoldersOrgRolesForMasterPwa_whenOneHolder() {

    when(pwaConsentOrganisationRoleRepository.findByAddedByPwaConsentInAndRoleInAndEndTimestampIsNull(
        List.of(pwaConsent),
        Set.of(HuooRole.HOLDER))
    ).thenReturn(List.of(pwaConsentHolderOrgRole));

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(Set.of(ORG_UNIT_ID_1)))
        .thenReturn(List.of(organisationUnit1));

    assertThat(pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(masterPwa)).hasOnlyOneElementSatisfying(
        masterPwaHolderDto -> {
          assertThat(masterPwaHolderDto.getHolderOrganisationGroup()).containsSame(organisationGroup1);
          assertThat(masterPwaHolderDto.getMasterPwa()).isEqualTo(masterPwa);
        });

  }

  @Test
  public void getCurrentHoldersOrgRolesForMasterPwa_whenMultipleHolders_overMultipleConsents() {
    var secondConsent = mock(PwaConsent.class);
    when(secondConsent.getMasterPwa()).thenReturn(masterPwa);
    when(pwaConsentRepository.findByMasterPwa(masterPwa)).thenReturn(List.of(pwaConsent, secondConsent));

    var secondHolderOrgRole = createOrgRole(secondConsent, HuooRole.HOLDER, organisationUnit2);
    when(pwaConsentOrganisationRoleRepository.findByAddedByPwaConsentInAndRoleInAndEndTimestampIsNull(
        List.of(pwaConsent, secondConsent),
        Set.of(HuooRole.HOLDER))
    ).thenReturn(List.of(pwaConsentHolderOrgRole, secondHolderOrgRole));

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(Set.of(ORG_UNIT_ID_1, ORG_UNIT_ID_2)))
        .thenReturn(List.of(organisationUnit1, organisationUnit2));

    var masterPwaHolders =  pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(masterPwa);

    assertThat(masterPwaHolders).hasSize(2);

    assertThat(masterPwaHolders).anySatisfy(masterPwaHolderDto -> {
          assertThat(masterPwaHolderDto.getHolderOrganisationGroup()).containsSame(organisationGroup1);
          assertThat(masterPwaHolderDto.getMasterPwa()).isEqualTo(masterPwa);
        });

    assertThat(masterPwaHolders).anySatisfy(masterPwaHolderDto -> {
      assertThat(masterPwaHolderDto.getHolderOrganisationGroup()).containsSame(organisationGroup2);
      assertThat(masterPwaHolderDto.getMasterPwa()).isEqualTo(masterPwa);
    });

  }

  @Test
  public void getCurrentHoldersOrgRolesForMasterPwa_whenMultipleHolders_whereOneHolderIsNotCurrentOrgUnit() {
    var secondConsent = mock(PwaConsent.class);
    when(pwaConsentRepository.findByMasterPwa(masterPwa)).thenReturn(List.of(pwaConsent, secondConsent));

    var secondHolderOrgRole = createOrgRole(secondConsent, HuooRole.HOLDER, organisationUnit2);
    when(pwaConsentOrganisationRoleRepository.findByAddedByPwaConsentInAndRoleInAndEndTimestampIsNull(
        List.of(pwaConsent, secondConsent),
        Set.of(HuooRole.HOLDER))
    ).thenReturn(List.of(pwaConsentHolderOrgRole, secondHolderOrgRole));

    when(portalOrganisationsAccessor.getOrganisationUnitsByIdIn(Set.of(ORG_UNIT_ID_1, ORG_UNIT_ID_2)))
        .thenReturn(List.of(organisationUnit1));

    var masterPwaHolders =  pwaConsentOrganisationRoleService.getCurrentHoldersOrgRolesForMasterPwa(masterPwa);


    assertThat(masterPwaHolders).hasOnlyOneElementSatisfying(masterPwaHolderDto -> {
      assertThat(masterPwaHolderDto.getHolderOrganisationGroup()).containsSame(organisationGroup1);
      assertThat(masterPwaHolderDto.getMasterPwa()).isEqualTo(masterPwa);
    });

  }

  @Test
  public void getOrganisationRoleSummary_serviceInteractions(){
    assertThat(pwaConsentOrganisationRoleService.getOrganisationRoleSummary(masterPwa)).isNotNull();
    verify(pwaConsentPipelineOrganisationRoleLinkRepository, times(1)).findActiveOrganisationPipelineRolesByMasterPwa(masterPwa);

  }

  @Test
  public void getNumberOfHolders_initialPwa() {
    var applicationParam = new PwaApplication(masterPwa, PwaApplicationType.INITIAL, 1);
    var detailParam = new PwaApplicationDetail(applicationParam, 1, null, null);
    Long holdersCount = pwaConsentOrganisationRoleService.getNumberOfHolders(masterPwa, detailParam);
    assertThat(holdersCount).isEqualTo(1);
  }

  @Test
  public void getNumberOfHolders_variationPwa() {
    var applicationParam = new PwaApplication(masterPwa, PwaApplicationType.HUOO_VARIATION, 1);
    var detailParam = new PwaApplicationDetail(applicationParam, 1, null, null);
    var consents = List.of(new PwaConsent(), new PwaConsent());

    when(pwaConsentRepository.findByMasterPwa(masterPwa)).thenReturn(consents);
    when(pwaConsentOrganisationRoleRepository.countByAddedByPwaConsentInAndRoleInAndEndTimestampIsNull(
        consents,
        Set.of(HuooRole.HOLDER))).thenReturn(2L);

    Long holdersCount = pwaConsentOrganisationRoleService.getNumberOfHolders(masterPwa, detailParam);
    assertThat(holdersCount).isEqualTo(2);
  }




}