package uk.co.ogauthority.pwa.features.application.tasks.huoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.OrganisationRolePipelineGroupDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.OrganisationRolesSummaryDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentPoint;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.model.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.service.PipelineNumberAndSplitsService;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelineOrganisationRoleLink;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipelineTestUtil;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactory;
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
  private PipelineAndIdentViewFactory pipelineAndIdentViewFactory;

  @Mock
  private PadPipelineService padPipelineService;;

  @Mock
  private EntityManager entityManager;

  @Mock
  private PipelineNumberAndSplitsService pipelineNumberAndSplitsService;

  @Mock
  private PadHuooRoleMetadataProvider padHuooRoleMetadataProvider;

  @Mock
  private PadHuooValidationService padHuooValidationService;

  @Mock
  private PwaApplicationService pwaApplicationService;

  private PadOrganisationRoleService padOrganisationRoleService;

  private PwaApplicationDetail detail;
  private PadOrganisationRole padOrgUnit1UserRole, padOrgUnit2OwnerRole, padAnyTreatyCountryRole;

  private PortalOrganisationUnit orgUnit1;
  private PortalOrganisationUnit orgUnit2;

  private Pipeline pipeline1;
  private PipelineId pipelineId1 = new PipelineId(1);
  private PipelineId pipelineId2 = new PipelineId(2);

  private PipelineSection pipeline2Section1;
  private PipelineSection pipeline2Section2;

  private static final Set<PipelineStatus> PIPELINE_INACTIVE_STATUSES = EnumSet.complementOf(EnumSet.copyOf(
      PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED)));

  @Captor
  private ArgumentCaptor<PadOrganisationRole> roleCaptor;

  @Captor
  private ArgumentCaptor<List<PadOrganisationRole>> roleListCaptor;

  @Captor
  private ArgumentCaptor<List<PadOrganisationRole>> deletedRoleListCaptor;

  @Before
  public void setUp() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    pipeline1 = new Pipeline(detail.getPwaApplication());
    pipeline1.setId(pipelineId1.asInt());

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
        pipelineAndIdentViewFactory,
        pipelineNumberAndSplitsService,
        padPipelineService,
        entityManager,
        padHuooValidationService,
        padHuooRoleMetadataProvider,
        pwaApplicationService);


    var orgGroup1 = PortalOrganisationTestUtils.generateOrganisationGroup(1, "Group1", "G1");
    var orgGroup2 = PortalOrganisationTestUtils.generateOrganisationGroup(2, "Group2", "G2");

    orgUnit1 = PortalOrganisationTestUtils.generateOrganisationUnit(1, "ZZZ", orgGroup1);
    orgUnit2 = PortalOrganisationTestUtils.generateOrganisationUnit(2, "AAA", orgGroup2);

    padOrgUnit1UserRole = PadOrganisationRole.fromOrganisationUnit(detail, orgUnit1, HuooRole.USER);
    padOrgUnit2OwnerRole = PadOrganisationRole.fromOrganisationUnit(detail, orgUnit2, HuooRole.OWNER);

    padAnyTreatyCountryRole = new PadOrganisationRole();
    padAnyTreatyCountryRole.setAgreement(TreatyAgreement.ANY_TREATY_COUNTRY);
    padAnyTreatyCountryRole.setRole(HuooRole.USER);
    padAnyTreatyCountryRole.setPwaApplicationDetail(detail);
    padAnyTreatyCountryRole.setType(HuooType.TREATY_AGREEMENT);

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(
        List.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole, padAnyTreatyCountryRole));


    when(portalOrganisationsAccessor.getOrganisationUnitsByOrganisationUnitIdIn(any()))
        .thenReturn(
            List.of(orgUnit1, orgUnit2)
        );

    when(padOrganisationRolesRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    pipeline2Section1 = PipelineSection.from(
        pipelineId2,
        1,
        PipelineIdentPoint.inclusivePoint("A"),
        PipelineIdentPoint.inclusivePoint("A")
    );
    pipeline2Section2 = PipelineSection.from(
        pipelineId2,
        2,
        PipelineIdentPoint.exclusivePoint("A"),
        PipelineIdentPoint.inclusivePoint("B")
    );

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

    var roleCountMap = Map.of(HuooRole.HOLDER, 1);
    when(padHuooRoleMetadataProvider.getRoleCountMap(detail)).thenReturn(roleCountMap);

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
  }


  @Test
  public void updateOrgRolesUsingForm_updateAndAddRole_linksUnchanged_newPwa_applicantOrgUpdated() {

    var form = new HuooForm();
    form.setOrganisationUnitId(2);
    form.setHuooRoles(Set.of(HuooRole.HOLDER, HuooRole.OPERATOR));

    var existingOrgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    var existingOrgRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, existingOrgUnit);
    when(padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail, existingOrgUnit))
        .thenReturn(List.of(existingOrgRole));

    var orgUnitToAdd = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    when(portalOrganisationsAccessor.getOrganisationUnitById(form.getOrganisationUnitId())).thenReturn(Optional.of(orgUnitToAdd));

    padOrganisationRoleService.updateOrgRolesUsingForm(detail, form, existingOrgUnit);

    //verify org role has been updated
    verify(padOrganisationRolesRepository, times(1)).saveAll(roleListCaptor.capture());
    assertThat(roleListCaptor.getValue()).anySatisfy(updatedRole -> assertThat(updatedRole.getRole()).isEqualTo(HuooRole.HOLDER));
    assertThat(roleListCaptor.getValue()).anySatisfy(updatedRole -> assertThat(updatedRole.getRole()).isEqualTo(HuooRole.OPERATOR));
    roleListCaptor.getValue().forEach(orgRole -> assertThat(orgRole.getOrganisationUnit()).isEqualTo(orgUnitToAdd));

    //verify pipeline links deletion
    var pipelineOrgRoleLinkDeleteCaptor = ArgumentCaptor.forClass(List.class);
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(pipelineOrgRoleLinkDeleteCaptor.capture());
    assertThat(pipelineOrgRoleLinkDeleteCaptor.getValue()).isEmpty();

    //verify pipeline split links updated
    var pipelineOrgRoleLinkSaveCaptor = ArgumentCaptor.forClass(List.class);
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).saveAll(pipelineOrgRoleLinkSaveCaptor.capture());
    assertThat(pipelineOrgRoleLinkSaveCaptor.getValue()).isEmpty();

    //verify existing org role deletion
    verify(padOrganisationRolesRepository, times(1)).deleteAll(deletedRoleListCaptor.capture());
    assertThat(deletedRoleListCaptor.getValue()).isEmpty();

    // verify applicant organisation updated
    verify(pwaApplicationService, times(1)).updateApplicantOrganisationUnitId(detail.getPwaApplication(), orgUnitToAdd);

  }

  @Test
  public void updateOrgRolesUsingForm_updateAndAddRole_linksUnchanged_variation_applicantOrgNotUpdated() {

    var form = new HuooForm();
    form.setOrganisationUnitId(2);
    form.setHuooRoles(Set.of(HuooRole.HOLDER, HuooRole.OPERATOR));

    detail.getPwaApplication().setApplicationType(PwaApplicationType.CAT_1_VARIATION);

    var existingOrgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    var existingOrgRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, existingOrgUnit);
    when(padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail, existingOrgUnit))
        .thenReturn(List.of(existingOrgRole));

    var orgUnitToAdd = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    when(portalOrganisationsAccessor.getOrganisationUnitById(form.getOrganisationUnitId())).thenReturn(Optional.of(orgUnitToAdd));

    padOrganisationRoleService.updateOrgRolesUsingForm(detail, form, existingOrgUnit);

    // verify applicant organisation not updated
    verifyNoInteractions(pwaApplicationService);

  }

  @Test
  public void updateOrgRolesUsingForm_updatingRolesAndRemovingRoleAndLink() {

    var form = new HuooForm();
    form.setOrganisationUnitId(2);
    form.setHuooRoles(EnumSet.complementOf(EnumSet.of(HuooRole.OWNER)));

    var existingOrgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    var existingOrgHolder = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, existingOrgUnit);
    var existingOrgUser = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, existingOrgUnit);
    var existingOrgOperator = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, existingOrgUnit);
    var existingOrgOwner = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, existingOrgUnit);
    when(padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail, existingOrgUnit))
        .thenReturn(List.of(existingOrgHolder, existingOrgUser, existingOrgOperator, existingOrgOwner));

    var orgUnitToAdd = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    when(portalOrganisationsAccessor.getOrganisationUnitById(form.getOrganisationUnitId())).thenReturn(Optional.of(orgUnitToAdd));

    var pipeline = new Pipeline();
    var ownerOrgRoleLink = new PadPipelineOrganisationRoleLink(existingOrgOwner, pipeline);
    List<PadPipelineOrganisationRoleLink> orgRolePipelineLinks = new ArrayList<>();
    orgRolePipelineLinks.add(ownerOrgRoleLink);
    when(padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(List.of(existingOrgOwner), detail))
        .thenReturn(orgRolePipelineLinks);

    padOrganisationRoleService.updateOrgRolesUsingForm(detail, form, existingOrgUnit);

    //verify org role has been updated
    verify(padOrganisationRolesRepository, times(1)).saveAll(roleListCaptor.capture());
    assertThat(roleListCaptor.getValue()).anySatisfy(updatedRole -> assertThat(updatedRole.getRole()).isEqualTo(HuooRole.HOLDER));
    assertThat(roleListCaptor.getValue()).anySatisfy(updatedRole -> assertThat(updatedRole.getRole()).isEqualTo(HuooRole.OPERATOR));
    roleListCaptor.getValue().forEach(orgRole -> assertThat(orgRole.getOrganisationUnit()).isEqualTo(orgUnitToAdd));

    //verify pipeline links deletion
    var pipelineOrgRoleLinkDeleteCaptor = ArgumentCaptor.forClass(List.class);
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(pipelineOrgRoleLinkDeleteCaptor.capture());
    assertThat(pipelineOrgRoleLinkDeleteCaptor.getValue()).isEqualTo(orgRolePipelineLinks);

    //verify pipeline split links updated
    var pipelineOrgRoleLinkSaveCaptor = ArgumentCaptor.forClass(List.class);
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).saveAll(pipelineOrgRoleLinkSaveCaptor.capture());
    assertThat(pipelineOrgRoleLinkSaveCaptor.getValue()).isEmpty();

    //verify existing org role deletion
    verify(padOrganisationRolesRepository, times(1)).deleteAll(deletedRoleListCaptor.capture());
    assertThat(deletedRoleListCaptor.getValue()).isEqualTo(List.of(existingOrgOwner));

  }


  @Test
  public void updateOrgRolesUsingForm_editingHuooWhenLinkedToSplitPipeline() {

    var form = new HuooForm();
    form.setOrganisationUnitId(2);
    form.setHuooRoles(EnumSet.complementOf(EnumSet.of(HuooRole.OWNER)));

    var existingOrgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    var existingOrgHolder = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, existingOrgUnit);
    var existingOrgUser = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, existingOrgUnit);
    var existingOrgOperator = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, existingOrgUnit);
    var existingOrgOwner = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, existingOrgUnit);
    when(padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail, existingOrgUnit))
        .thenReturn(List.of(existingOrgHolder, existingOrgUser, existingOrgOperator, existingOrgOwner));

    var orgUnitToAdd = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    when(portalOrganisationsAccessor.getOrganisationUnitById(form.getOrganisationUnitId())).thenReturn(Optional.of(orgUnitToAdd));

    var ownerOrgRoleSplitLink = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        existingOrgOwner.getRole(), existingOrgOwner.getOrganisationUnit(), new Pipeline(), "from", "to", 1);
    List<PadPipelineOrganisationRoleLink> orgRolePipelineLinks = new ArrayList<>();
    orgRolePipelineLinks.add(ownerOrgRoleSplitLink);
    when(padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(List.of(existingOrgOwner), detail))
        .thenReturn(orgRolePipelineLinks);

    padOrganisationRoleService.updateOrgRolesUsingForm(detail, form, existingOrgUnit);

    //verify org role has been updated
    verify(padOrganisationRolesRepository, times(1)).saveAll(roleListCaptor.capture());
    assertThat(roleListCaptor.getValue()).anySatisfy(updatedRole -> assertThat(updatedRole.getRole()).isEqualTo(HuooRole.HOLDER));
    assertThat(roleListCaptor.getValue()).anySatisfy(updatedRole -> assertThat(updatedRole.getRole()).isEqualTo(HuooRole.USER));
    assertThat(roleListCaptor.getValue()).anySatisfy(updatedRole -> assertThat(updatedRole.getRole()).isEqualTo(HuooRole.OPERATOR));
    roleListCaptor.getValue().forEach(orgRole -> assertThat(orgRole.getOrganisationUnit()).isEqualTo(orgUnitToAdd));

    //verify org linked to split pipeline is now set as unassigned
    verify(padOrganisationRolesRepository, times(1)).save(roleCaptor.capture());
    assertThat(roleCaptor.getValue().getType()).isEqualTo(HuooType.UNASSIGNED_PIPELINE_SPLIT);

    //verify pipeline links deletion
    var pipelineOrgRoleLinkDeleteCaptor = ArgumentCaptor.forClass(List.class);
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(pipelineOrgRoleLinkDeleteCaptor.capture());
    assertThat(pipelineOrgRoleLinkDeleteCaptor.getValue()).isEmpty();

    //verify pipeline split links updated
    var pipelineOrgRoleLinkSaveCaptor = ArgumentCaptor.forClass(List.class);
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).saveAll(pipelineOrgRoleLinkSaveCaptor.capture());
    assertThat(pipelineOrgRoleLinkSaveCaptor.getValue()).isEqualTo(orgRolePipelineLinks);

    //verify existing org role deletion
    verify(padOrganisationRolesRepository, times(1)).deleteAll(deletedRoleListCaptor.capture());
    assertThat(deletedRoleListCaptor.getValue()).isEqualTo(List.of(existingOrgOwner));

  }



  @Test
  public void removePipelineLinksForOrgsWithRoles_fullPipeline_splitPipelinesWithSingleAndDuplicateLinks() {

    var existingOrgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();
    var existingOrgHolder = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, existingOrgUnit);
    var existingOrgUser = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.USER, existingOrgUnit);
    var existingOrgOperator = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OPERATOR, existingOrgUnit);

    var holderOrgRoleSplitLink = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        existingOrgHolder.getRole(), existingOrgHolder.getOrganisationUnit(), new Pipeline(), "from", "to", 1);
    var userOrgRoleSplitLink = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        existingOrgUser.getRole(), existingOrgUser.getOrganisationUnit(), new Pipeline(), "from", "to", 1);
    var operatorOrgRoleFullLink = new PadPipelineOrganisationRoleLink(existingOrgOperator, new Pipeline());
    when(padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
        List.of(existingOrgHolder, existingOrgUser, existingOrgOperator), detail))
        .thenReturn(List.of(holderOrgRoleSplitLink, userOrgRoleSplitLink, operatorOrgRoleFullLink));

    when(padPipelineOrganisationRoleLinkRepository.countByPadOrgRole_PwaApplicationDetailAndPadOrgRole_RoleAndPipelineAndSectionNumber(
        detail, holderOrgRoleSplitLink.getPadOrgRole().getRole(),
        holderOrgRoleSplitLink.getPipeline(), holderOrgRoleSplitLink.getSectionNumber())).thenReturn(2L);

    when(padPipelineOrganisationRoleLinkRepository.countByPadOrgRole_PwaApplicationDetailAndPadOrgRole_RoleAndPipelineAndSectionNumber(
        detail, userOrgRoleSplitLink.getPadOrgRole().getRole(),
        userOrgRoleSplitLink.getPipeline(), userOrgRoleSplitLink.getSectionNumber())).thenReturn(1L);

    padOrganisationRoleService.removePipelineLinksForOrgsWithRoles(detail, List.of(existingOrgHolder, existingOrgUser, existingOrgOperator));


    var pipelineOrgRoleLinkSaveCaptor = ArgumentCaptor.forClass(List.class);
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).saveAll(pipelineOrgRoleLinkSaveCaptor.capture());
    assertThat(pipelineOrgRoleLinkSaveCaptor.getValue()).isEqualTo(List.of(userOrgRoleSplitLink));

    var pipelineOrgRoleLinkDeleteCaptor = ArgumentCaptor.forClass(List.class);
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(pipelineOrgRoleLinkDeleteCaptor.capture());
    assertThat(pipelineOrgRoleLinkDeleteCaptor.getValue()).isEqualTo(List.of(holderOrgRoleSplitLink, operatorOrgRoleFullLink));

    verify(padOrganisationRolesRepository, times(1)).save(roleCaptor.capture());
    assertThat(roleCaptor.getValue().getType()).isEqualTo(HuooType.UNASSIGNED_PIPELINE_SPLIT);
  }

  @Test
  public void saveEntityUsingForm_org() {

    var form = new HuooForm();
    var newOrgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();

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
    form.setHuooRoles(Set.of(HuooRole.USER));

    padOrganisationRoleService.saveEntityUsingForm(detail, form);

    verify(padOrganisationRolesRepository, times(1)).saveAll(roleListCaptor.capture());

    var newRole = roleListCaptor.getValue().get(0);

    assertThat(newRole.getPwaApplicationDetail()).isEqualTo(detail);
    assertThat(newRole.getType()).isEqualTo(HuooType.TREATY_AGREEMENT);
    assertThat(newRole.getOrganisationUnit()).isNull();
    assertThat(newRole.getAgreement()).isEqualTo(TreatyAgreement.ANY_TREATY_COUNTRY);
    assertThat(newRole.getRole()).isEqualTo(HuooRole.USER);

  }

  @Test
  public void saveEntityUsingForm_orgUpdate() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setHuooRoles(Set.of(HuooRole.OPERATOR));
    form.setOrganisationUnitId(padOrgUnit2OwnerRole.getOrganisationUnit().getOuId());

    when(portalOrganisationsAccessor.getOrganisationUnitById(2))
        .thenReturn(Optional.of(padOrgUnit2OwnerRole.getOrganisationUnit()));

    padOrganisationRoleService.saveEntityUsingForm(detail, form);

    verify(padOrganisationRolesRepository, times(1)).deleteAll(any());
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(any());
    verify(padOrganisationRolesRepository, times(1)).saveAll(roleListCaptor.capture());

    var capture = roleListCaptor.getValue().get(0);

    assertThat(capture.getType()).isEqualTo(HuooType.PORTAL_ORG);
    assertThat(capture.getOrganisationUnit()).isEqualTo(padOrgUnit2OwnerRole.getOrganisationUnit());
    assertThat(capture.getAgreement()).isNull();
    assertThat(capture.getRole()).isEqualTo(HuooRole.OPERATOR);
  }

  @Test
  public void saveEntityUsingForm_orgUpdate_linksDeletedWhereRoleRemoved() {
    var form = new HuooForm();
    form.setHuooType(HuooType.PORTAL_ORG);
    form.setHuooRoles(Set.of(HuooRole.OPERATOR));
    form.setOrganisationUnitId(padOrgUnit2OwnerRole.getOrganisationUnit().getOuId());

    when(portalOrganisationsAccessor.getOrganisationUnitById(2))
        .thenReturn(Optional.of(padOrgUnit2OwnerRole.getOrganisationUnit()));

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(detail, orgUnit2))
        .thenReturn(List.of(new PadOrganisationRole(HuooRole.USER), new PadOrganisationRole(HuooRole.OPERATOR)));

    var roleLinkList = List.of(
        new PadPipelineOrganisationRoleLink(new PadOrganisationRole(HuooRole.USER), new Pipeline()));

    when(padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
        any(),
        eq(detail))
    ).thenReturn(roleLinkList);

    padOrganisationRoleService.saveEntityUsingForm(detail, form);

    verify(padOrganisationRolesRepository, times(1)).deleteAll(deletedRoleListCaptor.capture());
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(roleLinkList);
    verify(padOrganisationRolesRepository, times(1)).saveAll(roleListCaptor.capture());

    assertThat(deletedRoleListCaptor.getValue()).extracting(PadOrganisationRole::getRole)
        .containsExactly(HuooRole.USER);
  }

   @Test
  public void addHolder() {

    var newOrgUnit = PortalOrganisationTestUtils.getOrganisationUnitInOrgGroup();

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
  public void removeRolesOfUnit() {
    when(padOrganisationRolesRepository.getAllByPwaApplicationDetailAndOrganisationUnit(
        detail,
        padOrgUnit1UserRole.getOrganisationUnit())
    ).thenReturn(List.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole));

    var pipeline = new Pipeline();
    var roleLink = new PadPipelineOrganisationRoleLink(padOrgUnit1UserRole, pipeline);
    when(padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
        List.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole), detail)).thenReturn(List.of(roleLink));

    padOrganisationRoleService.removeRolesOfUnit(detail, padOrgUnit1UserRole.getOrganisationUnit());
    verify(padOrganisationRolesRepository, times(1)).deleteAll(any());
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(List.of(roleLink));
  }

  @Test
  public void removePipelineLinksForOrgsWithRoles_exactMatch() {

    var pipeline = new Pipeline();
    var roleLink1 = new PadPipelineOrganisationRoleLink(padOrgUnit1UserRole, pipeline);
    var roleLink2 = new PadPipelineOrganisationRoleLink(padOrgUnit2OwnerRole, pipeline);
    when(padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
        Set.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole), detail)).thenReturn(List.of(roleLink1, roleLink2));

    padOrganisationRoleService.removePipelineLinksForOrgsWithRoles(detail,
        Set.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole));

    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(List.of(roleLink1, roleLink2));
  }

  @Test
  public void removePipelineLinksForOrgsWithRoles_fewerLinked() {

    var pipeline = new Pipeline();
    var roleLink1 = new PadPipelineOrganisationRoleLink(padOrgUnit1UserRole, pipeline);
    when(padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
        Set.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole), detail)).thenReturn(List.of(roleLink1));

    padOrganisationRoleService.removePipelineLinksForOrgsWithRoles(detail,
        Set.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole));

    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(List.of(roleLink1));
  }

  @Test
  public void removePipelineLinksForOrgsWithRoles_noneLinked() {

    when(padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
        Set.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole), detail)).thenReturn(List.of());

    padOrganisationRoleService.removePipelineLinksForOrgsWithRoles(detail,
        Set.of(padOrgUnit1UserRole, padOrgUnit2OwnerRole));

    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(List.of());
  }

  @Test
  public void removeRoleOfTreatyAgreement_roleLinkedtoMultiplePipelines(){

    var pipeline1 = new Pipeline();
    pipeline1.setId(1);
    var pipeline2 = new Pipeline();
    pipeline1.setId(2);

    var roleLink1 = new PadPipelineOrganisationRoleLink(padAnyTreatyCountryRole, pipeline1);
    var roleLink2 = new PadPipelineOrganisationRoleLink(padAnyTreatyCountryRole, pipeline2);

    when(padPipelineOrganisationRoleLinkRepository.findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(
        List.of(padAnyTreatyCountryRole), detail)).thenReturn(List.of(roleLink1, roleLink2));

    padOrganisationRoleService.removeRoleOfTreatyAgreement(padAnyTreatyCountryRole);
    InOrder verifyOrder = Mockito.inOrder(padPipelineOrganisationRoleLinkRepository, padOrganisationRolesRepository);
    verifyOrder.verify(padPipelineOrganisationRoleLinkRepository)
        .findAllByPadOrgRoleInAndPadOrgRole_PwaApplicationDetail(List.of(padAnyTreatyCountryRole), detail);
    verifyOrder.verify(padPipelineOrganisationRoleLinkRepository)
        .deleteAll(List.of(roleLink1, roleLink2));
    verifyOrder.verify(padOrganisationRolesRepository).delete(padAnyTreatyCountryRole);
    verifyOrder.verifyNoMoreInteractions();
  }

  @Test
  public void createApplicationOrganisationRolesFromSummary_createsApplicationLevelAndPipelineLinkOrganisationRoles() {
    var summaryDto = mock(OrganisationRolesSummaryDto.class);

    when(summaryDto.getAllOrganisationUnitIdsWithRole())
        .thenReturn(
            Set.of(OrganisationUnitId.from(orgUnit1), OrganisationUnitId.from(orgUnit2))
        );

    var pipelineIdentifierSet = Set.of(pipelineId1, pipelineId2, pipeline2Section1, pipeline2Section2);

    var org1UserRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(
        new OrganisationRoleInstanceDto(padOrgUnit1UserRole), pipelineIdentifierSet);

    when(summaryDto.getUserOrganisationUnitGroups()).thenReturn(Set.of(org1UserRolePipelineGroupDto));
    when(summaryDto.getOrganisationRolePipelineGroupBy(HuooRole.USER, OrganisationUnitId.from(orgUnit1)))
        .thenReturn(Optional.of(org1UserRolePipelineGroupDto));

    ArgumentCaptor<List<PadOrganisationRole>> padOrgRoleArgCapture = ArgumentCaptor.forClass(List.class);
    ArgumentCaptor<List<PadPipelineOrganisationRoleLink>> padOrgRolePipelineLinkArgCapture = ArgumentCaptor.forClass(List.class);

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
    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).hasSize(pipelineIdentifierSet.size());

    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).allSatisfy(padOrgRolePipelineLink -> {
      // only one role created so all links must reference it
      assertThat(padOrgRolePipelineLink.getPadOrgRole()).isEqualTo(padOrgRoleArgCapture.getValue().get(0));
    });

    // check that there's a pad org role pipeline link for the pipeline id pipeline identifiers
    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).anySatisfy(padOrgRolePipelineLink -> {
      assertThat(padOrgRolePipelineLink.getPipeline().getId()).isEqualTo(pipelineId1.asInt());
    });

    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).anySatisfy(padOrgRolePipelineLink -> {
      assertThat(padOrgRolePipelineLink.getPipeline().getId()).isEqualTo(pipelineId2.asInt());
    });

    // check that theres a pad org role pipeline link for the pipeline section pipeline identifiers
    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).anySatisfy(padOrgRolePipelineLink -> {
      assertThat(padOrgRolePipelineLink.getPipelineIdentifier()).isEqualTo(pipeline2Section1);
    });

    assertThat(padOrgRolePipelineLinkArgCapture.getValue()).anySatisfy(padOrgRolePipelineLink -> {
      assertThat(padOrgRolePipelineLink.getPipelineIdentifier()).isEqualTo(pipeline2Section2);
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
  public void getAssignableOrgRolesForDetailByRole_whenNoOrgRoleFound() {

    assertThat(padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(
        detail,
        HuooRole.HOLDER
    )).isEmpty();

  }

  @Test
  public void getAssignableOrgRolesForDetailByRole_whenOrgRolesFound() {

    var org1HolderRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, orgUnit1);
    var org1OwnerRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, orgUnit1);
    var org2HolderRole = PadOrganisationRole.fromTreatyAgreement(detail, TreatyAgreement.ANY_TREATY_COUNTRY, HuooRole.HOLDER);

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(
        List.of(org1HolderRole, org1OwnerRole, org2HolderRole)
    );

    assertThat(padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(
        detail,
        HuooRole.HOLDER
    )).containsExactly(org1HolderRole, org2HolderRole);

  }

  @Test
  public void createPadPipelineOrganisationRoleLink_createsAndSavesExpectedLink() {
    var org1HolderRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, orgUnit1);
    var pipeline = new Pipeline();
    pipeline.setId(pipelineId1.asInt());

    var argCapture = ArgumentCaptor.forClass(PadPipelineOrganisationRoleLink.class);
    padOrganisationRoleService.createPadPipelineOrganisationRoleLink(org1HolderRole, pipeline.getPipelineId());

    verify(padPipelineOrganisationRoleLinkRepository, times(1)).save(argCapture.capture());

    assertThat(argCapture.getValue()).satisfies(padPipelineOrganisationRoleLink -> {
      assertThat(padPipelineOrganisationRoleLink.getPipeline().getPipelineId()).isEqualTo(pipelineId1);
      assertThat(padPipelineOrganisationRoleLink.getPadOrgRole()).isEqualTo(org1HolderRole);
    });

  }

  @Test
  public void getAssignableOrgRolesForDetailByRole_onlyFilterOutTypeUnassignedSplitPipelineType() {
    when(padOrganisationRolesRepository.findOrganisationRoleDtoByPwaApplicationDetail(detail))
        .thenReturn(List.of(
            OrganisationRoleDtoTestUtil.createTreatyOrgRoleInstance(HuooRole.USER, TreatyAgreement.ANY_TREATY_COUNTRY),
            OrganisationRoleDtoTestUtil.createOrganisationUnitOrgRoleInstance(HuooRole.USER, 1),
            OrganisationRoleDtoTestUtil.createUnassignedPipelineSectionRoleInstance(HuooRole.USER)
        ));


    assertThat(
        padOrganisationRoleService.getAssignableOrganisationRoleInstanceDtosByRole(detail, HuooRole.USER))
        .containsExactlyInAnyOrder(
            OrganisationRoleDtoTestUtil.createTreatyOrgRoleInstance(HuooRole.USER, TreatyAgreement.ANY_TREATY_COUNTRY),
            OrganisationRoleDtoTestUtil.createOrganisationUnitOrgRoleInstance(HuooRole.USER, 1)
        );

  }


  @Test
  public void getOrganisationRoleSummary_pipelinesAssignedToRoleAndActive() {

    var orgPipelineRole = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER, orgUnit1.getOuId(), pipelineId1.asInt());

    when(padPipelineService.getPadPipelineInactiveStatuses()).thenReturn(PIPELINE_INACTIVE_STATUSES);
    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(PadPipelineTestUtil.createActivePadPipeline(detail, pipeline1)));

    when(padOrganisationRolesRepository.findActiveOrganisationPipelineRolesByPwaApplicationDetail(detail))
        .thenReturn(List.of(orgPipelineRole));

    when(pipelineNumberAndSplitsService.getAllPipelineNumbersAndSplitsRole(any(), any()))
        .thenReturn(Map.of(orgPipelineRole.getPipelineIdentifier(), new PipelineNumbersAndSplits(
            orgPipelineRole.getPipelineIdentifier(), String.valueOf(orgPipelineRole.getPipelineIdentifier()), null)));


    var allOrgRolePipelineGroupView = padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(detail);

    assertThat(allOrgRolePipelineGroupView.getOrgRolePipelineGroupView(orgPipelineRole.getHuooRole())).isNotEmpty();
    var orgRolePipelineGroup = allOrgRolePipelineGroupView.getOrgRolePipelineGroupView(orgPipelineRole.getHuooRole()).get(0);
    assertThat(orgRolePipelineGroup.getOrganisationRoleOwner().getOrganisationUnitId())
        .isEqualTo(orgPipelineRole.getOrganisationUnitId());

    assertThat(orgRolePipelineGroup.getPipelineNumbersAndSplits()).isNotEmpty();
    assertThat(orgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getPipelineIdentifier())
        .isEqualTo(orgPipelineRole.getPipelineIdentifier());
  }

  @Test
  public void getOrganisationRoleSummary_pipelinesAssignedToRoleAndInactive_roleInstancesCreatedWithoutPipeline() {

    var orgPipelineRole = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER, orgUnit1.getOuId(), pipelineId1.asInt());

    when(padPipelineService.getPadPipelineInactiveStatuses()).thenReturn(PIPELINE_INACTIVE_STATUSES);
    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(PadPipelineTestUtil.createInActivePadPipeline(detail, pipeline1)));

    when(padOrganisationRolesRepository.findActiveOrganisationPipelineRolesByPwaApplicationDetail(detail))
        .thenReturn(List.of(orgPipelineRole));

    var allOrgRolePipelineGroupView = padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(detail);

    assertThat(allOrgRolePipelineGroupView.getOrgRolePipelineGroupView(orgPipelineRole.getHuooRole())).isNotEmpty();
    var orgRolePipelineGroup = allOrgRolePipelineGroupView.getOrgRolePipelineGroupView(orgPipelineRole.getHuooRole()).get(0);
    assertThat(orgRolePipelineGroup.getOrganisationRoleOwner().getOrganisationUnitId())
        .isEqualTo(orgPipelineRole.getOrganisationUnitId());

    assertThat(orgRolePipelineGroup.getPipelineNumbersAndSplits()).isEmpty();
  }

  @Test
  public void getOrganisationRoleSummary_orgRoleExistsWithoutAssignedPipelines() {

    var orgPipelineRole = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER, orgUnit1.getOuId(), null);

    when(padOrganisationRolesRepository.findActiveOrganisationPipelineRolesByPwaApplicationDetail(detail))
        .thenReturn(List.of(orgPipelineRole));

    var allOrgRolePipelineGroupView = padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(detail);

    assertThat(allOrgRolePipelineGroupView.getOrgRolePipelineGroupView(orgPipelineRole.getHuooRole())).isNotEmpty();
    var orgRolePipelineGroup = allOrgRolePipelineGroupView.getOrgRolePipelineGroupView(orgPipelineRole.getHuooRole()).get(0);
    assertThat(orgRolePipelineGroup.getOrganisationRoleOwner().getOrganisationUnitId())
        .isEqualTo(orgPipelineRole.getOrganisationUnitId());

    assertThat(orgRolePipelineGroup.getPipelineNumbersAndSplits()).isEmpty();
  }

  @Test
  public void getOrganisationRoleSummary_unassignedPipelinesExist_manualRoleInstancesCreated() {

    var orgPipelineRole = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER, orgUnit1.getOuId(), null);

    when(padPipelineService.getPadPipelineInactiveStatuses()).thenReturn(PIPELINE_INACTIVE_STATUSES);
    var padPipeline = PadPipelineTestUtil.createActivePadPipeline(detail, pipeline1);
    when(padPipelineService.getPipelines(detail)).thenReturn(List.of(padPipeline));

    when(padOrganisationRolesRepository.findActiveOrganisationPipelineRolesByPwaApplicationDetail(detail))
        .thenReturn(List.of(orgPipelineRole));

    when(pipelineNumberAndSplitsService.getAllPipelineNumbersAndSplitsRole(any(), any()))
        .thenReturn(Map.of(padPipeline.getPipelineId(), new PipelineNumbersAndSplits(
            padPipeline.getPipelineId(), String.valueOf(padPipeline.getPipelineId()), null)));


    var allOrgRolePipelineGroupView = padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(detail);

    assertThat(allOrgRolePipelineGroupView.getOrgRolePipelineGroupView(orgPipelineRole.getHuooRole())).isNotEmpty();
    assertThat(allOrgRolePipelineGroupView.getOrgRolePipelineGroupView(orgPipelineRole.getHuooRole())).anySatisfy(
        orgRolePipelineGroup -> {
          assertThat(orgRolePipelineGroup.getManuallyEnteredName())
              .isEqualTo(String.format("Pipelines without assigned %s", orgPipelineRole.getHuooRole().getDisplayText()));
          assertThat(orgRolePipelineGroup.getPipelineNumbersAndSplits()).isNotEmpty();
          assertThat(orgRolePipelineGroup.getPipelineNumbersAndSplits().get(0).getPipelineIdentifier())
              .isEqualTo(padPipeline.getPipelineId());
        }
    );
  }


  @Test
  public void deletePadPipelineRoleLinksForPipelinesAndRole_verifyServiceInteractions() {
    var pipeline = new Pipeline();
    pipeline.setId(pipelineId1.asInt());
    var idAsIntSet = Set.of(pipelineId1.asInt());
    var link = PadOrganisationRoleTestUtil.createOrgRolePipelineLink(HuooRole.HOLDER, orgUnit1, pipeline);

    when(
        padPipelineOrganisationRoleLinkRepository.findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_RoleAndPipeline_IdIn(
            detail, HuooRole.HOLDER, idAsIntSet)
    ).thenReturn(List.of(link));

    padOrganisationRoleService.deletePadPipelineRoleLinksForPipelineIdentifiersAndRole(detail, Set.of(pipelineId1),
        HuooRole.HOLDER);

    var orderVerifier = Mockito.inOrder(padPipelineOrganisationRoleLinkRepository, entityManager);
    orderVerifier.verify(
        padPipelineOrganisationRoleLinkRepository).findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_RoleAndPipeline_IdIn(
        detail, HuooRole.HOLDER, idAsIntSet);
    orderVerifier.verify(padPipelineOrganisationRoleLinkRepository).deleteAll(List.of(link));
    orderVerifier.verify(entityManager).flush();
    orderVerifier.verifyNoMoreInteractions();

  }

  @Test
  public void getPipelineSplitsForRole_whenNoSplits() {
    var splitPipelines = padOrganisationRoleService.getPipelineSplitsForRole(detail, HuooRole.HOLDER);
    assertThat(splitPipelines).isEmpty();
  }

  @Test
  public void getPipelineSplitsForRole_whenSplitsExist_filterOutWholePipelines() {
    var pipeline1 = new Pipeline();
    pipeline1.setId(pipelineId1.asInt());

    var pipeline2 = new Pipeline();
    pipeline2.setId(pipelineId2.asInt());

    var wholePipelineLink = PadOrganisationRoleTestUtil.createOrgRolePipelineLink(HuooRole.HOLDER, orgUnit2, pipeline2);

    var split1Link = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        HuooRole.HOLDER,
        orgUnit1,
        pipeline1,
        "FROM_1",
        "TO_1",
        1);

    var split2Link = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        HuooRole.HOLDER,
        orgUnit1,
        pipeline1,
        "FROM_2",
        "TO_2",
        2);
    when(padPipelineOrganisationRoleLinkRepository.findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_Role(
        detail,
        HuooRole.HOLDER)
    ).thenReturn(List.of(split1Link, split2Link, wholePipelineLink));

    var splitPipelines = padOrganisationRoleService.getPipelineSplitsForRole(detail, HuooRole.HOLDER);
    assertThat(splitPipelines).containsExactlyInAnyOrder(
        PipelineSection.from(pipelineId1, 1, PipelineIdentPoint.inclusivePoint("FROM_1"),
            PipelineIdentPoint.inclusivePoint("TO_1")),
        PipelineSection.from(pipelineId1, 2, PipelineIdentPoint.inclusivePoint("FROM_2"),
            PipelineIdentPoint.inclusivePoint("TO_2"))
    );
  }

  @Test
  public void deletePipelineRoleLinksForPadPipeline_serviceInteraction() {
    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline(detail);
    padPipeline.setPipeline(pipeline);

    var roleLink = new PadPipelineOrganisationRoleLink(padOrgUnit1UserRole, pipeline);

    when(padPipelineOrganisationRoleLinkRepository.getAllByPadOrgRole_PwaApplicationDetailAndPipeline(detail, pipeline))
        .thenReturn(List.of(roleLink));

    padOrganisationRoleService.deletePipelineRoleLinksForPadPipeline(padPipeline);
    verify(padPipelineOrganisationRoleLinkRepository, times(1)).deleteAll(List.of(roleLink));
  }



  @Test
  public void getAllOrganisationRolePipelineGroupView_includesPortalOrgsAndTreaty() {

    //Organisation Roles Summary DTO
    var orgPipelineRoleInstanceDto1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER,
        1,
        1
    );

    var orgPipelineRoleInstanceDto2 = OrganisationRoleDtoTestUtil.createTreatyOrgUnitPipelineRoleInstance(
        HuooRole.USER,
        TreatyAgreement.ANY_TREATY_COUNTRY,
        1
    );

    var orgPipelineRoleInstanceDto3 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.OPERATOR,
        3,
        1
    );

    var orgPipelineRoleInstanceDto4 =  OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.OWNER,
        4,
        1
    );

    when(padOrganisationRolesRepository.findActiveOrganisationPipelineRolesByPwaApplicationDetail(detail))
        .thenReturn(List.of(orgPipelineRoleInstanceDto1, orgPipelineRoleInstanceDto2, orgPipelineRoleInstanceDto3, orgPipelineRoleInstanceDto4));

    //Portal org units
    var portalOrgUnitDetail1 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        PortalOrganisationTestUtils.generateOrganisationUnit(1, "company"), "address", "123");
    var organisationUnitDetailDto1 = OrganisationUnitDetailDto.from(portalOrgUnitDetail1);

    var portalOrgUnitDetail3 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        PortalOrganisationTestUtils.generateOrganisationUnit(3, "company3"), "address3", "1234");
    var organisationUnitDetailDto3 = OrganisationUnitDetailDto.from(portalOrgUnitDetail3);

    var portalOrgUnitDetail4 = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        PortalOrganisationTestUtils.generateOrganisationUnit(4, "company4"), "address4", "12345");
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
    var actualView = padOrganisationRoleService.getAllOrganisationRolePipelineGroupView(detail);

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
  public void getOrCreateUnassignedPipelineSplitRole_whenNoUnassignedSplitRoleTypeFound() {
    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(List.of());
    var roleSaveCaptor = ArgumentCaptor.forClass(PadOrganisationRole.class);
    var role = padOrganisationRoleService.getOrCreateUnassignedPipelineSplitRole(detail, HuooRole.HOLDER);

    verify(padOrganisationRolesRepository, times(1)).save(roleSaveCaptor.capture());

    assertThat(roleSaveCaptor.getValue().getType()).isEqualTo(HuooType.UNASSIGNED_PIPELINE_SPLIT);
    assertThat(role).isEqualTo(roleSaveCaptor.getValue());
  }

  @Test
  public void getOrCreateUnassignedPipelineSplitRole_whenUnassignedSplitRoleTypeFound() {

    var unassignedRole = PadOrganisationRole.forUnassignedSplitPipeline(detail, HuooRole.HOLDER);
    unassignedRole.setId(9999);
    var roleList = List.of(
        PadOrganisationRole.fromOrganisationUnit(detail, orgUnit1, HuooRole.HOLDER),
        unassignedRole
    );

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(roleList);

    var foundRole = padOrganisationRoleService.getOrCreateUnassignedPipelineSplitRole(detail, HuooRole.HOLDER);
    var roleSaveCaptor = ArgumentCaptor.forClass(PadOrganisationRole.class);

    verify(padOrganisationRolesRepository, times(1)).save(roleSaveCaptor.capture());

    assertThat(roleSaveCaptor.getValue().getType()).isEqualTo(HuooType.UNASSIGNED_PIPELINE_SPLIT);
    // just check its the same role id
    assertThat(roleSaveCaptor.getValue().getId()).isEqualTo(unassignedRole.getId());
    assertThat(foundRole).isEqualTo(roleSaveCaptor.getValue());
  }

  private Map<PipelineIdentifier, PipelineNumbersAndSplits> createPipelineNumberAndSplitMap(){
    Map<PipelineIdentifier, PipelineNumbersAndSplits> allPipelineNumbersAndSplitsRole = new HashMap<>();
    var pipeline1NumberAndSplit = new PipelineNumbersAndSplits(pipelineId1, "PL1", null);
    var pipeline2Section1NumberAndSplit = new PipelineNumbersAndSplits(pipeline2Section1, "PL2", "section1");
    var pipeline2Section2NumberAndSplit = new PipelineNumbersAndSplits(pipeline2Section2, "PL2", "section2");

    allPipelineNumbersAndSplitsRole.put(pipeline1NumberAndSplit.getPipelineIdentifier(), pipeline1NumberAndSplit);
    allPipelineNumbersAndSplitsRole.put(
        pipeline2Section1NumberAndSplit.getPipelineIdentifier(),
        pipeline2Section1NumberAndSplit
    );

    allPipelineNumbersAndSplitsRole.put(
        pipeline2Section2NumberAndSplit.getPipelineIdentifier(),
        pipeline2Section2NumberAndSplit
    );

    return allPipelineNumbersAndSplitsRole;
  }

  @Test
  public void getAllAssignableAndNonAssignableOrgRolesForDetailByRole_doesNotOutFilterOutUnassignableRoles() {
    var org1HolderRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.HOLDER, orgUnit1);
    var org1OwnerRole = PadOrganisationRoleTestUtil.createOrgRole(HuooRole.OWNER, orgUnit1);
    var org2HolderRole = PadOrganisationRole.fromTreatyAgreement(detail, TreatyAgreement.ANY_TREATY_COUNTRY, HuooRole.HOLDER);
    var unassignableRole = PadOrganisationRole.forUnassignedSplitPipeline(detail, HuooRole.HOLDER);

    when(padOrganisationRolesRepository.getAllByPwaApplicationDetail(detail)).thenReturn(
        List.of(org1HolderRole, org1OwnerRole, org2HolderRole, unassignableRole)
    );

    assertThat(padOrganisationRoleService.getAllAssignableAndNonAssignableOrgRolesForDetailByRole(
        detail,
        HuooRole.HOLDER
    )).containsExactly(org1HolderRole, org2HolderRole, unassignableRole);
  }


  @Test
  public void organisationExistsAndActive_orgNotFound_false() {
    assertThat(padOrganisationRoleService.organisationExistsAndActive(1)).isFalse();
  }

  @Test
  public void organisationExistsAndActive_orgInactive_false() {
    var orgUnit = PortalOrganisationTestUtils.getInactiveOrganisationUnitInOrgGroup();
    when(portalOrganisationsAccessor.getOrganisationUnitById(1)).thenReturn(Optional.of(orgUnit));
    assertThat(padOrganisationRoleService.organisationExistsAndActive(1)).isFalse();
  }

  @Test
  public void organisationExistsAndActive_orgIsActive_true() {
    when(portalOrganisationsAccessor.getOrganisationUnitById(1)).thenReturn(Optional.of(orgUnit1));
    assertThat(padOrganisationRoleService.organisationExistsAndActive(1)).isTrue();
  }


}
