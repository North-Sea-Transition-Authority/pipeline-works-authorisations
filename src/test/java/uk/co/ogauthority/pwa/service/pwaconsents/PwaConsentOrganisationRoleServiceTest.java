package uk.co.ogauthority.pwa.service.pwaconsents;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifier;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRole;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentOrganisationRoleRepository;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PipelineNumberAndSplitsService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

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
  private PipelineNumberAndSplitsService pipelineNumberAndSplitsService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PwaConsent pwaConsent;

  @Mock
  private MasterPwa masterPwa;

  @Captor
  private ArgumentCaptor<List<PwaConsentOrganisationRole>> consentRolesCaptor;

  private Clock clock = Clock.fixed(Clock.systemUTC().instant(), ZoneId.systemDefault());

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
        pipelineNumberAndSplitsService, pipelineDetailService, pwaConsentRepository,
        portalOrganisationsAccessor, clock
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
  public void getOrganisationRoleSummary_findByMasterPwa_serviceInteractions(){
    assertThat(pwaConsentOrganisationRoleService.getOrganisationRoleSummary(masterPwa)).isNotNull();
    verify(pwaConsentPipelineOrganisationRoleLinkRepository, times(1)).findActiveOrganisationPipelineRolesByMasterPwa(masterPwa);

  }

  @Test
  public void getOrganisationRoleSummary_findByPipelineDetail_serviceInteractions() {
    var pipelineDetail = new PipelineDetail();
    assertThat(pwaConsentOrganisationRoleService.getOrganisationRoleSummary(pipelineDetail)).isNotNull();
    verify(pwaConsentPipelineOrganisationRoleLinkRepository, times(1)).findActiveOrganisationPipelineRolesByPipelineDetail(pipelineDetail);

  }


  @Test
  public void getNumberOfHolders_variationPwa() {
    var consents = List.of(new PwaConsent(), new PwaConsent());
    when(pwaConsentRepository.findByMasterPwa(masterPwa)).thenReturn(consents);
    when(pwaConsentOrganisationRoleRepository.countByAddedByPwaConsentInAndRoleInAndEndTimestampIsNull(
        consents,
        Set.of(HuooRole.HOLDER))).thenReturn(2L);

    Long holdersCount = pwaConsentOrganisationRoleService.getNumberOfHolders(masterPwa);
    assertThat(holdersCount).isEqualTo(2);
  }


  @Test
  public void getAllOrganisationRolePipelineGroupView_includesPortalOrgsAndTreaty() {

    var masterPwa = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getMasterPwa();

    //Organisation Roles Summary DTO
    var orgPipelineRoleInstanceDto1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER,
        1,
        1
    );


    var orgPipelineRoleInstanceDto2 = OrganisationRoleDtoTestUtil.createTreatyOrgUnitPipelineRoleInstance(
        HuooRole.USER,
        TreatyAgreement.ANY_TREATY_COUNTRY,
        1);

    var orgPipelineRoleInstanceDto3 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.OPERATOR,
        3,
        1
    );

    var orgPipelineRoleInstanceDto4 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.OWNER,
        4,
        1
    );

    when(pwaConsentPipelineOrganisationRoleLinkRepository.findActiveOrganisationPipelineRolesByMasterPwa(masterPwa))
        .thenReturn(List.of(orgPipelineRoleInstanceDto1, orgPipelineRoleInstanceDto2, orgPipelineRoleInstanceDto3, orgPipelineRoleInstanceDto4));

    //Portal org units
    var portalOrgUnitDetail1 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        new PortalOrganisationUnit(1, "company"), "address", "123");
    var organisationUnitDetailDto1 = OrganisationUnitDetailDto.from(portalOrgUnitDetail1);

    var portalOrgUnitDetail3 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        new PortalOrganisationUnit(3, "company3"), "address3", "1234");
    var organisationUnitDetailDto3 = OrganisationUnitDetailDto.from(portalOrgUnitDetail3);

    var portalOrgUnitDetail4 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        new PortalOrganisationUnit(4, "company4"), "address4", "12345");
    var organisationUnitDetailDto4 = OrganisationUnitDetailDto.from(portalOrgUnitDetail4);

    when(portalOrganisationsAccessor.getOrganisationUnitDetailDtosByOrganisationUnitId(
        Set.of(new OrganisationUnitId(1), new OrganisationUnitId(3), new OrganisationUnitId(4))))
        .thenReturn(List.of(organisationUnitDetailDto1, organisationUnitDetailDto3, organisationUnitDetailDto4));

    //Pipeline numbers and splits
    var padPipeline = new PadPipeline();
    padPipeline.setId(1);
    Pipeline pipeline = new Pipeline();
    pipeline.setId(1);
    padPipeline.setPipeline(pipeline);
    var pipelineOverview = new PadPipelineOverview(padPipeline);
    Map<PipelineIdentifier, PipelineNumbersAndSplits> allPipelineNumbersAndSplitsRole = new HashMap<>();
    allPipelineNumbersAndSplitsRole.put(new PipelineId(1), new PipelineNumbersAndSplits(
        new PipelineId(1), pipelineOverview.getPipelineNumber(), null));
    when(pipelineNumberAndSplitsService.getAllPipelineNumbersAndSplitsRole(any(), any()))
        .thenReturn(allPipelineNumbersAndSplitsRole);


    //asserts
    var actualView = pwaConsentOrganisationRoleService.getAllOrganisationRolePipelineGroupView(masterPwa);

    var holderPortalOrgRolePipelineGroup = actualView.getHolderOrgRolePipelineGroups().get(0);
    assertThat(holderPortalOrgRolePipelineGroup.getHuooType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(holderPortalOrgRolePipelineGroup.getCompanyName()).isEqualTo("company");
    assertThat(holderPortalOrgRolePipelineGroup.getTreatyAgreement()).isNull();
    assertThat(holderPortalOrgRolePipelineGroup.getRegisteredNumber()).isEqualTo("123");
    assertThat(holderPortalOrgRolePipelineGroup.getCompanyAddress()).isEqualTo("address");
    assertThat(holderPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getPipelineIdentifier()).isEqualTo(new PipelineId(1));
    assertThat(holderPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getSplitInfo()).isNull();

    var userTreatyOrgRolePipelineGroup = actualView.getUserOrgRolePipelineGroups().get(0);
    assertThat(userTreatyOrgRolePipelineGroup.getHuooType()).isEqualTo(HuooType.TREATY_AGREEMENT);
    assertThat(userTreatyOrgRolePipelineGroup.getCompanyName()).isNull();
    assertThat(userTreatyOrgRolePipelineGroup.getTreatyAgreement()).isEqualTo(TreatyAgreement.ANY_TREATY_COUNTRY);
    assertThat(userTreatyOrgRolePipelineGroup.getRegisteredNumber()).isNull();
    assertThat(userTreatyOrgRolePipelineGroup.getCompanyAddress()).isNull();
    assertThat(userTreatyOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getPipelineIdentifier()).isEqualTo(new PipelineId(1));
    assertThat(userTreatyOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getSplitInfo()).isNull();

    var operatorPortalOrgRolePipelineGroup = actualView.getOperatorOrgRolePipelineGroups().get(0);
    assertThat(operatorPortalOrgRolePipelineGroup.getHuooType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(operatorPortalOrgRolePipelineGroup.getCompanyName()).isEqualTo("company3");
    assertThat(operatorPortalOrgRolePipelineGroup.getTreatyAgreement()).isNull();
    assertThat(operatorPortalOrgRolePipelineGroup.getRegisteredNumber()).isEqualTo("1234");
    assertThat(operatorPortalOrgRolePipelineGroup.getCompanyAddress()).isEqualTo("address3");
    assertThat(operatorPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getPipelineIdentifier()).isEqualTo(new PipelineId(1));
    assertThat(operatorPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getSplitInfo()).isNull();
    assertThat(userTreatyOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getSplitInfo()).isNull();

    var ownerPortalOrgRolePipelineGroup = actualView.getOwnerOrgRolePipelineGroups().get(0);
    assertThat(ownerPortalOrgRolePipelineGroup.getHuooType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(ownerPortalOrgRolePipelineGroup.getCompanyName()).isEqualTo("company4");
    assertThat(ownerPortalOrgRolePipelineGroup.getTreatyAgreement()).isNull();
    assertThat(ownerPortalOrgRolePipelineGroup.getRegisteredNumber()).isEqualTo("12345");
    assertThat(ownerPortalOrgRolePipelineGroup.getCompanyAddress()).isEqualTo("address4");
    assertThat(ownerPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getPipelineIdentifier()).isEqualTo(new PipelineId(1));
    assertThat(ownerPortalOrgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getSplitInfo()).isNull();

  }

  @Test
  public void getActiveOrgRolesAddedByConsents() {

    var consents = List.of(new PwaConsent());

    pwaConsentOrganisationRoleService.getActiveOrgRolesAddedByConsents(consents);

    verify(pwaConsentOrganisationRoleRepository, times(1)).findByAddedByPwaConsentInAndEndTimestampIsNull(consents);

  }

  @Test
  public void endConsentOrgRoles() {

    var orgRole1 = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(pwaConsent, new OrganisationUnitId(organisationUnit1.getOuId()), HuooRole.HOLDER);
    var orgRole2 = PwaConsentOrganisationRoleTestUtil
        .createOrganisationRole(pwaConsent, new OrganisationUnitId(organisationUnit2.getOuId()), HuooRole.OWNER);

    var endingConsent = new PwaConsent();

    pwaConsentOrganisationRoleService.endConsentOrgRoles(endingConsent, List.of(orgRole1, orgRole2));

    verify(pwaConsentOrganisationRoleRepository, times(1)).saveAll(consentRolesCaptor.capture());

    assertThat(consentRolesCaptor.getValue()).allSatisfy(role -> {
      assertThat(role.getEndedByPwaConsent()).isEqualTo(endingConsent);
      assertThat(role.getEndTimestamp()).isEqualTo(clock.instant());
    });

  }

  @Test
  public void createNewConsentOrgUnitRoles() {

    Multimap<OrganisationUnitId, HuooRole> newRoleMultiMap = HashMultimap.create();
    newRoleMultiMap.putAll(new OrganisationUnitId(1), Set.of(HuooRole.HOLDER, HuooRole.USER));
    newRoleMultiMap.putAll(new OrganisationUnitId(2), Set.of(HuooRole.OWNER, HuooRole.OPERATOR));

    pwaConsentOrganisationRoleService.createNewConsentOrgUnitRoles(pwaConsent, newRoleMultiMap);

    verify(pwaConsentOrganisationRoleRepository, times(1)).saveAll(consentRolesCaptor.capture());

    assertThat(consentRolesCaptor.getValue()).allSatisfy(role -> {
      assertThat(role.getAddedByPwaConsent()).isEqualTo(pwaConsent);
      assertThat(role.getStartTimestamp()).isEqualTo(clock.instant());
      assertThat(role.getAgreement()).isNull();
      assertThat(role.getEndedByPwaConsent()).isNull();
      assertThat(role.getEndTimestamp()).isNull();
      assertThat(role.getMigratedOrganisationName()).isNull();
      assertThat(role.getType()).isEqualTo(HuooType.PORTAL_ORG);
    });

    assertThat(consentRolesCaptor.getValue())
        .anySatisfy(role -> {
          assertThat(role.getOrganisationUnitId()).isEqualTo(1);
          assertThat(role.getRole()).isEqualTo(HuooRole.HOLDER);
        })
        .anySatisfy(role -> {
          assertThat(role.getOrganisationUnitId()).isEqualTo(1);
          assertThat(role.getRole()).isEqualTo(HuooRole.USER);
        })
        .anySatisfy(role -> {
          assertThat(role.getOrganisationUnitId()).isEqualTo(2);
          assertThat(role.getRole()).isEqualTo(HuooRole.OWNER);
        })
        .anySatisfy(role -> {
          assertThat(role.getOrganisationUnitId()).isEqualTo(2);
          assertThat(role.getRole()).isEqualTo(HuooRole.OPERATOR);
        });

  }

  @Test
  public void createNewConsentTreatyRoles() {

    Multimap<TreatyAgreement, HuooRole> newRoleMultiMap = HashMultimap.create();
    newRoleMultiMap.put(TreatyAgreement.ANY_TREATY_COUNTRY, HuooRole.USER);

    pwaConsentOrganisationRoleService.createNewConsentTreatyRoles(pwaConsent, newRoleMultiMap);

    verify(pwaConsentOrganisationRoleRepository, times(1)).saveAll(consentRolesCaptor.capture());

    assertThat(consentRolesCaptor.getValue()).hasOnlyOneElementSatisfying(role -> {
      assertThat(role.getAddedByPwaConsent()).isEqualTo(pwaConsent);
      assertThat(role.getStartTimestamp()).isEqualTo(clock.instant());
      assertThat(role.getAgreement()).isEqualTo(TreatyAgreement.ANY_TREATY_COUNTRY);
      assertThat(role.getType()).isEqualTo(HuooType.TREATY_AGREEMENT);
      assertThat(role.getEndedByPwaConsent()).isNull();
      assertThat(role.getEndTimestamp()).isNull();
      assertThat(role.getMigratedOrganisationName()).isNull();
      assertThat(role.getOrganisationUnitId()).isNull();
    });

  }

}