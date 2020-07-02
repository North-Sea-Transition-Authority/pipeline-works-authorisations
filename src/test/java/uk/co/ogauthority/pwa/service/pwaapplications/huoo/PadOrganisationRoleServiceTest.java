package uk.co.ogauthority.pwa.service.pwaapplications.huoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnitDetail;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.OrganisationRolePipelineGroupDto;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.OrganisationRolesSummaryDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.huoo.HuooForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.huoo.PadOrganisationRolesRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadOrganisationRoleServiceTest {

  @Mock
  private PadOrganisationRolesRepository padOrganisationRolesRepository;

  @Mock
  private PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private EntityManager entityManager;

  private PadOrganisationRoleService padOrganisationRoleService;

  private PwaApplicationDetail detail;
  private PadOrganisationRole padOrgUnit1UserRole, padOrgUnit2OwnerRole, padNorwayTreatyRole, padBelgiumTreatyRole;
  private PortalOrganisationUnitDetail org1Detail, org2Detail;

  private PortalOrganisationUnit orgUnit1;
  private PortalOrganisationUnit orgUnit2;

  private PipelineId pipelineId1 = new PipelineId(1);
  private PipelineId pipelineId2 = new PipelineId(2);

  @Captor
  private ArgumentCaptor<PadOrganisationRole> roleCaptor;

  @Captor
  private ArgumentCaptor<List<PadOrganisationRole>> roleListCaptor;

  @Before
  public void setUp() {
    when(entityManager.getReference(eq(Pipeline.class), any())).thenAnswer(invocation -> {
      var p = new Pipeline();
      p.setId(invocation.getArgument(1));
      return p;
    });

    // make sure that save all return the saved list as the repo will do.
    when(padOrganisationRolesRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    padOrganisationRoleService = new PadOrganisationRoleService(
        padOrganisationRolesRepository,
        padPipelineOrganisationRoleLinkRepository,
        portalOrganisationsAccessor,
        entityManager);

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var orgGroup1 = PortalOrganisationTestUtils.generateOrganisationGroup(1, "Group1", "G1");
    var orgGroup2 = PortalOrganisationTestUtils.generateOrganisationGroup(2, "Group2", "G2");

    orgUnit1 = PortalOrganisationTestUtils.generateOrganisationUnit(1, "ZZZ", orgGroup1);
    orgUnit2 = PortalOrganisationTestUtils.generateOrganisationUnit(2, "AAA", orgGroup2);

    org1Detail = PortalOrganisationTestUtils.generateOrganisationUnitDetail(orgUnit1, "add1", "111");
    org2Detail = PortalOrganisationTestUtils.generateOrganisationUnitDetail(orgUnit2, "add2", "222");

    padOrgUnit1UserRole = PadOrganisationRole.fromOrganisationUnit(detail, orgUnit1, HuooRole.USER);
    padOrgUnit2OwnerRole = PadOrganisationRole.fromOrganisationUnit(detail, orgUnit2, HuooRole.OWNER);

    padNorwayTreatyRole = new PadOrganisationRole();
    padNorwayTreatyRole.setAgreement(TreatyAgreement.NORWAY);
    padNorwayTreatyRole.setRole(HuooRole.USER);
    padNorwayTreatyRole.setPwaApplicationDetail(detail);
    padNorwayTreatyRole.setType(HuooType.TREATY_AGREEMENT);

    padBelgiumTreatyRole = new PadOrganisationRole();
    padBelgiumTreatyRole.setAgreement(TreatyAgreement.BELGIUM);
    padBelgiumTreatyRole.setRole(HuooRole.USER);
    padBelgiumTreatyRole.setPwaApplicationDetail(detail);
    padBelgiumTreatyRole.setType(HuooType.TREATY_AGREEMENT);

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(
        List.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole, padNorwayTreatyRole, padBelgiumTreatyRole));


    when(portalOrganisationsAccessor.getOrganisationUnitsByOrganisationUnitIdIn(any()))
        .thenReturn(
            List.of(orgUnit1, orgUnit2)
        );

  }

  @Test
  public void getHuooOrganisationUnitRoleViews() {

    var rolesList = List.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole, padNorwayTreatyRole, padBelgiumTreatyRole);

    padOrgUnit1UserRole.setRole(HuooRole.USER);
    padOrgUnit2OwnerRole.setRole(HuooRole.OWNER);

    when(portalOrganisationsAccessor.getOrganisationUnitDetails(any())).thenReturn(List.of(
        org1Detail,
        org2Detail
    ));

    var viewList = padOrganisationRoleService.getHuooOrganisationUnitRoleViews(detail, rolesList);

    assertThat(viewList.size()).isEqualTo(2);

    var org1View = viewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit1UserRole.getOrganisationUnit().getName())).findFirst().orElseThrow();
    var org2View = viewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit2OwnerRole.getOrganisationUnit().getName())).findFirst().orElseThrow();

    assertThat(org1View.getCompanyAddress()).isEqualTo(org1Detail.getLegalAddress());
    assertThat(org1View.getRegisteredNumber()).isEqualTo(org1Detail.getRegisteredNumber());
    assertThat(org1View.getRoleSet()).containsExactlyInAnyOrderElementsOf(Set.of(padOrgUnit1UserRole.getRole()));
    assertThat(org1View.getRoles()).isEqualTo("User");

    assertThat(org2View.getCompanyAddress()).isEqualTo(org2Detail.getLegalAddress());
    assertThat(org2View.getRegisteredNumber()).isEqualTo(org2Detail.getRegisteredNumber());
    assertThat(org2View.getRoleSet()).containsExactlyInAnyOrderElementsOf(Set.of(padOrgUnit2OwnerRole.getRole()));
    assertThat(org2View.getRoles()).isEqualTo("Owner");

  }

  @Test
  public void getHuooOrganisationUnitRoleViews_sorting_holderTakesPrecedence() {

    when(portalOrganisationsAccessor.getOrganisationUnitDetails(any())).thenReturn(List.of(
        org1Detail,
        org2Detail
    ));

    var orgRoleViewList = padOrganisationRoleService.getHuooOrganisationUnitRoleViews(detail, List.of(
        padOrgUnit1UserRole, padOrgUnit2OwnerRole));

    var org1View = orgRoleViewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit1UserRole.getOrganisationUnit().getName())).findFirst().orElseThrow();
    var org2View = orgRoleViewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit2OwnerRole.getOrganisationUnit().getName())).findFirst().orElseThrow();

    int comparison = org1View.compareTo(org2View);

    // org1 comes before org2 in the role list
    // although org2 wins by alphabetical name, roles are compared first and have higher precedence
    // org1 has Holder role, which is ranked higher
    assertThat(comparison).isEqualTo(-1);

    assertThat(orgRoleViewList.indexOf(org1View)).isEqualTo(0);
    assertThat(orgRoleViewList.indexOf(org2View)).isEqualTo(1);

  }

  @Test
  public void getHuooOrganisationUnitRoleViews_sorting_orgNameBreaksTie() {

    when(portalOrganisationsAccessor.getOrganisationUnitDetails(any())).thenReturn(List.of(
        org1Detail,
        org2Detail
    ));

    padOrgUnit1UserRole.setRole(padOrgUnit2OwnerRole.getRole()); // equalise the roles between the two orgs

    var orgRoleViewList = padOrganisationRoleService.getHuooOrganisationUnitRoleViews(detail, List.of(
        padOrgUnit1UserRole, padOrgUnit2OwnerRole));

    var org1View = orgRoleViewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit1UserRole.getOrganisationUnit().getName())).findFirst().orElseThrow();
    var org2View = orgRoleViewList.stream().filter(
        r -> r.getCompanyName().equals(padOrgUnit2OwnerRole.getOrganisationUnit().getName())).findFirst().orElseThrow();

    int comparison = org1View.compareTo(org2View);

    // org1 comes after org2 in the list
    // as the roles are equal, company name is used, and org2:AAA beats org1:ZZZ
    assertThat(comparison).isEqualTo(1);

    assertThat(orgRoleViewList.indexOf(org2View)).isEqualTo(0);
    assertThat(orgRoleViewList.indexOf(org1View)).isEqualTo(1);

  }

  @Test
  public void getTreatyAgreementViews() {

    var rolesList = List.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole, padNorwayTreatyRole, padBelgiumTreatyRole);

    var viewList = padOrganisationRoleService.getTreatyAgreementViews(detail, rolesList);

    assertThat(viewList.size()).isEqualTo(2);

    var norway = viewList.stream().filter(
        r -> r.getCountry().equals(TreatyAgreement.NORWAY.getCountry())).findFirst().orElseThrow();
    var belgium = viewList.stream().filter(
        r -> r.getCountry().equals(TreatyAgreement.BELGIUM.getCountry())).findFirst().orElseThrow();

    assertThat(norway.getTreatyAgreementText()).isEqualTo(TreatyAgreement.NORWAY.getAgreementText());
    assertThat(norway.getRoles()).isEqualTo(padNorwayTreatyRole.getRole().getDisplayText());
    assertThat(norway.getRoles()).isEqualTo("User");
    assertThat(viewList.indexOf(norway)).isEqualTo(1); // sorted alphabetically

    assertThat(belgium.getTreatyAgreementText()).isEqualTo(TreatyAgreement.BELGIUM.getAgreementText());
    assertThat(belgium.getRoles()).isEqualTo(padBelgiumTreatyRole.getRole().getDisplayText());
    assertThat(belgium.getRoles()).isEqualTo("User");
    assertThat(viewList.indexOf(belgium)).isEqualTo(0); // sorted alphabetically

  }

  @Test
  public void canRemoveOrganisationRole_multipleHolders() {


    padOrgUnit1UserRole.setRole(HuooRole.HOLDER);
    padOrgUnit2OwnerRole.setRole(HuooRole.HOLDER);

    boolean canRemove = padOrganisationRoleService.canRemoveOrgRoleFromUnit(detail,
        padOrgUnit1UserRole.getOrganisationUnit());

    assertThat(canRemove).isTrue();

  }

  @Test
  public void canRemoveOrganisationRole_singleHolder() {

    padOrgUnit1UserRole.setRole(HuooRole.HOLDER);
    padOrgUnit2OwnerRole.setRole(HuooRole.OWNER);

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail,
        padOrgUnit1UserRole.getOrganisationUnit()))
        .thenReturn(List.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole));

    boolean canRemove = padOrganisationRoleService.canRemoveOrgRoleFromUnit(detail,
        padOrgUnit1UserRole.getOrganisationUnit());

    assertThat(canRemove).isFalse();

  }

  @Test
  public void canRemoveOrganisationRole_removingNonHolder() {

    padOrgUnit2OwnerRole.setRole(HuooRole.OWNER);

    boolean canRemove = padOrganisationRoleService.canRemoveOrgRoleFromUnit(detail,
        padOrgUnit2OwnerRole.getOrganisationUnit());

    assertThat(canRemove).isTrue();

  }

  @Test
  public void mapPadOrganisationRoleToForm_org() {

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(eq(detail), any()))
        .thenReturn(List.of(padOrgUnit1UserRole));

    var form = new HuooForm();

    padOrganisationRoleService.mapPortalOrgUnitRoleToForm(detail, padOrgUnit1UserRole.getOrganisationUnit(), form);

    assertThat(form.getHuooType()).isEqualTo(padOrgUnit1UserRole.getType());
    assertThat(form.getHuooRoles()).containsExactlyInAnyOrderElementsOf(Set.of(padOrgUnit1UserRole.getRole()));
    assertThat(form.getOrganisationUnitId()).isEqualTo(padOrgUnit1UserRole.getOrganisationUnit().getOuId());
    assertThat(form.getTreatyAgreement()).isNull();

  }

  @Test
  public void mapPadOrganisationRoleToForm_treaty() {

    var form = new HuooForm();

    padOrganisationRoleService.mapTreatyAgreementToForm(detail, padNorwayTreatyRole, form);

    assertThat(form.getHuooType()).isEqualTo(padNorwayTreatyRole.getType());
    assertThat(form.getHuooRoles()).containsExactlyInAnyOrderElementsOf(Set.of(padNorwayTreatyRole.getRole()));
    assertThat(form.getOrganisationUnitId()).isNull();
    assertThat(form.getTreatyAgreement()).isEqualTo(padNorwayTreatyRole.getAgreement());

  }

  @Test
  public void saveEntityUsingForm_org() {

    var form = new HuooForm();
    var newOrgUnit = PortalOrganisationTestUtils.getOrganisationUnit();

    form.setHuooType(HuooType.PORTAL_ORG);
    form.setOrganisationUnitId(newOrgUnit.getOuId());
    form.setHuooRoles(Set.of(HuooRole.OPERATOR));

    when(portalOrganisationsAccessor.getOrganisationUnitById(newOrgUnit.getOuId()))
        .thenReturn(Optional.of(newOrgUnit));

    padOrganisationRoleService.saveEntityUsingForm(detail, form);

    verify(padOrganisationRolesRepository, times(1)).saveAll(roleListCaptor.capture());

    var newRole = roleListCaptor.getValue().get(0);

    assertThat(newRole.getPwaApplicationDetail()).isEqualTo(detail);
    assertThat(newRole.getType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(newRole.getAgreement()).isNull();
    assertThat(newRole.getOrganisationUnit()).isEqualTo(newOrgUnit);
    assertThat(newRole.getRole()).isEqualTo(HuooRole.OPERATOR);

  }

  @Test
  public void saveEntityUsingForm_treaty() {

    var form = new HuooForm();

    form.setHuooType(HuooType.TREATY_AGREEMENT);
    form.setTreatyAgreement(TreatyAgreement.NETHERLANDS);
    form.setHuooRoles(Set.of(HuooRole.USER));

    padOrganisationRoleService.saveEntityUsingForm(detail, form);

    verify(padOrganisationRolesRepository, times(1)).saveAll(roleListCaptor.capture());

    var newRole = roleListCaptor.getValue().get(0);

    assertThat(newRole.getPwaApplicationDetail()).isEqualTo(detail);
    assertThat(newRole.getType()).isEqualTo(HuooType.TREATY_AGREEMENT);
    assertThat(newRole.getOrganisationUnit()).isNull();
    assertThat(newRole.getAgreement()).isEqualTo(TreatyAgreement.NETHERLANDS);
    assertThat(newRole.getRole()).isEqualTo(HuooRole.USER);

  }

  @Test
  public void updateEntityUsingForm_org() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setHuooRoles(Set.of(HuooRole.OPERATOR));
    form.setOrganisationUnitId(padOrgUnit2OwnerRole.getOrganisationUnit().getOuId());

    when(portalOrganisationsAccessor.getOrganisationUnitById(2))
        .thenReturn(Optional.of(padOrgUnit2OwnerRole.getOrganisationUnit()));

    padOrganisationRoleService.updateEntityUsingForm(detail, padOrgUnit1UserRole.getOrganisationUnit(), form);

    verify(padOrganisationRolesRepository, times(1)).deleteAll(any());
    verify(padOrganisationRolesRepository, times(1)).saveAll(roleListCaptor.capture());

    var capture = roleListCaptor.getValue().get(0);

    assertThat(capture.getType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(capture.getOrganisationUnit()).isEqualTo(padOrgUnit2OwnerRole.getOrganisationUnit());
    assertThat(capture.getAgreement()).isNull();
    assertThat(capture.getRole()).isEqualTo(HuooRole.OPERATOR);
  }

  @Test
  public void updateEntityUsingForm_treaty() {
    var form = new HuooForm();
    form.setTreatyAgreement(TreatyAgreement.NETHERLANDS);

    var org = new PadOrganisationRole();
    org.setType(HuooType.TREATY_AGREEMENT);
    org.setRole(HuooRole.USER);

    padOrganisationRoleService.updateEntityUsingForm(detail, org, form);

    verify(padOrganisationRolesRepository, times(1)).save(org);

    assertThat(org.getType()).isEqualTo(HuooType.TREATY_AGREEMENT);
    assertThat(org.getOrganisationUnit()).isNull();
    assertThat(org.getAgreement()).isEqualTo(TreatyAgreement.NETHERLANDS);
    assertThat(org.getRole()).isEqualTo(HuooRole.USER);
  }

  @Test
  public void addHolder() {

    var newOrgUnit = PortalOrganisationTestUtils.getOrganisationUnit();

    padOrganisationRoleService.addHolder(detail, newOrgUnit);

    verify(padOrganisationRolesRepository, times(1)).save(roleCaptor.capture());

    var newHolderRole = roleCaptor.getValue();

    assertThat(newHolderRole.getPwaApplicationDetail()).isEqualTo(detail);
    assertThat(newHolderRole.getType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(newHolderRole.getRole()).isEqualTo(HuooRole.HOLDER);
    assertThat(newHolderRole.getOrganisationUnit()).isEqualTo(newOrgUnit);
    assertThat(newHolderRole.getAgreement()).isNull();

  }

  @Test
  public void getOrgRolesForDetail() {
    padOrganisationRoleService.getOrgRolesForDetail(detail);
    verify(padOrganisationRolesRepository, times(1)).getAllByPwaApplicationDetail(detail);
  }

  @Test
  public void getRoleCountMap() {
    when(padOrganisationRoleService.getOrgRolesForDetail(detail)).thenReturn(List.of(
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR),
        PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR)
    ));

    var result = padOrganisationRoleService.getRoleCountMap(detail);
    assertThat(result).containsExactlyInAnyOrderEntriesOf(
        Map.ofEntries(
            entry(HuooRole.HOLDER, 1),
            entry(HuooRole.USER, 2),
            entry(HuooRole.OPERATOR, 3),
            entry(HuooRole.OWNER, 0)
        ));
  }

  @Test
  public void removeRolesOfUnit() {
    when(padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(
        detail,
        padOrgUnit1UserRole.getOrganisationUnit())
    ).thenReturn(List.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole));

    padOrganisationRoleService.removeRolesOfUnit(detail, padOrgUnit1UserRole.getOrganisationUnit());
    verify(padOrganisationRolesRepository, times(1)).deleteAll(any());
  }


  @Test
  public void createApplicationOrganisationRolesFromSummary_createsApplicationLevelAndPipelineLinkOrganisationRoles() {
    var summaryDto = mock(OrganisationRolesSummaryDto.class);

    when(summaryDto.getAllOrganisationUnitIdsWithRole())
        .thenReturn(
            Set.of(OrganisationUnitId.from(orgUnit1), OrganisationUnitId.from(orgUnit2))
        );

    var org1UserRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(
        new OrganisationRoleInstanceDto(padOrgUnit1UserRole), Set.of(pipelineId1, pipelineId2));

    when(summaryDto.getUserOrganisationUnitGroups()).thenReturn(Set.of(org1UserRolePipelineGroupDto));
    when(summaryDto.getOrganisationRolePipelineGroupBy(HuooRole.USER, OrganisationUnitId.from(orgUnit1)))
        .thenReturn(Optional.of(org1UserRolePipelineGroupDto));

    ArgumentCaptor<List<PadOrganisationRole>> padOrgRoleArgCapture = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<List<PadPipelineOrganisationRoleLink>> padOrgRolePipelineLinkArgCapture = ArgumentCaptor
        .forClass(List.class);

    padOrganisationRoleService.createApplicationOrganisationRolesFromSummary(detail, summaryDto);

    verify(summaryDto, times(1)).getHolderOrganisationUnitGroups();
    verify(summaryDto, times(1)).getUserOrganisationUnitGroups();
    verify(summaryDto, times(1)).getOperatorOrganisationUnitGroups();
    verify(summaryDto, times(1)).getOwnerOrganisationUnitGroups();

    verify(padOrganisationRolesRepository, times(1)).saveAll(padOrgRoleArgCapture.capture());
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).saveAll(padOrgRolePipelineLinkArgCapture.capture());

    // Assert that overall Role created correctly
    assertThat(padOrgRoleArgCapture.getValue()).hasSize(1);

    assertThat(padOrgRoleArgCapture.getValue()).anySatisfy(padOrgRole -> {
      assertThat(padOrgRole.getOrganisationUnit()).isEqualTo(orgUnit1);
      assertThat(padOrgRole.getAgreement()).isNull();
      assertThat(padOrgRole.getRole()).isEqualTo(HuooRole.USER);
      assertThat(padOrgRole.getType()).isEqualTo(HuooType.PORTAL_ORG);
      assertThat(padOrgRole.getPwaApplicationDetail()).isEqualTo(detail);

    });

    //Assert all pipelines in role group has link correctly created
    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).hasSize(2);

    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).allSatisfy(padOrgRolePipelineLink -> {
      // only one role created so all links must reference it
      assertThat(padOrgRolePipelineLink.getPadOrgRole()).isEqualTo(padOrgRoleArgCapture.getValue().get(0));
    });

    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).anySatisfy(padOrgRolePipelineLink -> {
      assertThat(padOrgRolePipelineLink.getPipeline().getId()).isEqualTo(pipelineId1.asInt());
    });

    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).anySatisfy(padOrgRolePipelineLink -> {
      assertThat(padOrgRolePipelineLink.getPipeline().getId()).isEqualTo(pipelineId2.asInt());
    });

  }

  @Test
  public void createApplicationOrganisationRolesFromSummary_whenOrganisationRoleGroups() {
    var summaryDto = mock(OrganisationRolesSummaryDto.class);

    ArgumentCaptor<List<PadOrganisationRole>> padOrgRoleArgCapture = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<List<PadPipelineOrganisationRoleLink>> padOrgRolePipelineLinkArgCapture = ArgumentCaptor
        .forClass(List.class);

    padOrganisationRoleService.createApplicationOrganisationRolesFromSummary(detail, summaryDto);

    verify(summaryDto, times(1)).getHolderOrganisationUnitGroups();
    verify(summaryDto, times(1)).getUserOrganisationUnitGroups();
    verify(summaryDto, times(1)).getOperatorOrganisationUnitGroups();
    verify(summaryDto, times(1)).getOwnerOrganisationUnitGroups();

    verify(padOrganisationRolesRepository, times(1)).saveAll(padOrgRoleArgCapture.capture());
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).saveAll(padOrgRolePipelineLinkArgCapture.capture());

    assertThat(padOrgRoleArgCapture.getValue()).isEmpty();
    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).isEmpty();


  }

  @Test
  public void getOrgRolesForDetailByOrganisationIdAndRole_whenNoOrgRoleFound() {

    assertThat(padOrganisationRoleService.getOrgRolesForDetailByOrganisationIdAndRole(
        detail,
        Set.of(OrganisationUnitId.from(orgUnit1)),
        HuooRole.HOLDER
    )).isEmpty();

  }

  @Test
  public void getOrgRolesForDetailByOrganisationIdAndRole_whenOrgRolesFound() {

    var org1HolderRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, orgUnit1);
    var org1OwnerRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, orgUnit1);
    var org2HolderRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, orgUnit2);

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(
        List.of(org1HolderRole, org1OwnerRole, org2HolderRole)
    );

    assertThat(padOrganisationRoleService.getOrgRolesForDetailByOrganisationIdAndRole(
        detail,
        Set.of(OrganisationUnitId.from(orgUnit1)),
        HuooRole.HOLDER
    )).containsExactly(org1HolderRole);

  }

  @Test
  public void createPadPipelineOrganisationRoleLink_createsAndSavesExpectedLink() {
    var org1HolderRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, orgUnit1);
    var pipeline = new Pipeline();

    var argCapture = ArgumentCaptor.forClass(PadPipelineOrganisationRoleLink.class);
    padOrganisationRoleService.createPadPipelineOrganisationRoleLink(org1HolderRole, pipeline);

    verify(padPipelineOrganisationRoleLinkRepository, times(1)).save(argCapture.capture());

    assertThat(argCapture.getValue()).satisfies(padPipelineOrganisationRoleLink -> {
      assertThat(padPipelineOrganisationRoleLink.getPipeline()).isEqualTo(pipeline);
      assertThat(padPipelineOrganisationRoleLink.getPadOrgRole()).isEqualTo(org1HolderRole);
    });

  }

  @Test
  public void getOrganisationRoleDtosByRole_doesNotFilterByType() {
    when(padOrganisationRolesRepository.findOrganisationRoleDtoByPwaApplicationDetail(detail))
        .thenReturn(List.of(
            OrganisationRoleDtoTestUtil.createTreatyOrgRoleInstance(HuooRole.USER, TreatyAgreement.BELGIUM),
            OrganisationRoleDtoTestUtil.createOrganisationUnitOrgRoleInstance(HuooRole.USER, 1)
        ));


    assertThat(
        padOrganisationRoleService.getOrganisationRoleInstanceDtosByRole(detail, HuooRole.USER))
        .containsExactlyInAnyOrder(
            OrganisationRoleDtoTestUtil.createTreatyOrgRoleInstance(HuooRole.USER, TreatyAgreement.BELGIUM),
            OrganisationRoleDtoTestUtil.createOrganisationUnitOrgRoleInstance(HuooRole.USER, 1)
        );

  }


}
