package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.pipelinehuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentPoint;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSection;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.repository.pwaapplications.pipelinehuoo.PadPipelineOrganisationRoleLinkRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickHuooPipelineValidationType;
import uk.co.ogauthority.pwa.validators.pipelinehuoo.PickHuooPipelinesFormValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelinesHuooServiceTest {
  private final HuooRole DEFAULT_ROLE = HuooRole.HOLDER;

  private final int CONSENTED_PIPELINE_ID = 10;
  private final int APPLICATION_PIPELINE_ID = 20;
  private final int NON_MASTER_PWA_PIPELINE_ID = 30;
  private final PickableHuooPipelineOption PIPELINE_WITH_ROLE = PickablePipelineOptionTestUtil.createOption(
      new PipelineId(CONSENTED_PIPELINE_ID),
      "PL1");
  private final PickableHuooPipelineOption PIPELINE_WITHOUT_ROLE = PickablePipelineOptionTestUtil.createOption(
      new PipelineId(APPLICATION_PIPELINE_ID),
      "TEMP_1");

  @Mock
  private PickableHuooPipelineService pickableHuooPipelineService;

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  @Mock
  private PickHuooPipelinesFormValidator pickHuooPipelinesFormValidator;

  @Mock
  private PadPipelineOrganisationRoleLinkRepository padPipelineOrganisationRoleLinkRepository;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PipelineOverview consentedPipelineOverview;

  private PwaApplicationDetail pwaApplicationDetail;
  private PickHuooPipelinesForm form;

  private PortalOrganisationUnit organisationUnit1;
  private PortalOrganisationUnit organisationUnit2;

  private TreatyAgreement treatyAgreement = TreatyAgreement.BELGIUM;

  private PadPipelinesHuooService padPipelinesHuooService;

  @Before
  public void setup() {
    var orgGrp = PortalOrganisationTestUtils.generateOrganisationGroup(10, "ArbitraryOrgGroup", "ArbOrgGrp");
    organisationUnit1 = PortalOrganisationTestUtils.generateOrganisationUnit(100, "org1", orgGrp);
    organisationUnit2 = PortalOrganisationTestUtils.generateOrganisationUnit(200, "org2", orgGrp);

    when(consentedPipelineOverview.getPipelineId()).thenReturn(CONSENTED_PIPELINE_ID);
    when(consentedPipelineOverview.getNumberOfIdents()).thenReturn(1L);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    form = new PickHuooPipelinesForm();

    padPipelinesHuooService = new PadPipelinesHuooService(
        pickableHuooPipelineService,
        portalOrganisationsAccessor,
        padOrganisationRoleService,
        pickHuooPipelinesFormValidator,
        padPipelineOrganisationRoleLinkRepository,
        padPipelineService);


    when(pickableHuooPipelineService.getAllPickablePipelinesForApplicationAndRole(pwaApplicationDetail, DEFAULT_ROLE))
        .thenReturn(
            Set.of(PIPELINE_WITH_ROLE, PIPELINE_WITHOUT_ROLE)
        );

    // mimic successful reconciliation
    when(pickableHuooPipelineService.reconcilePickablePipelineIds(any(), any(), any())).thenReturn(
        Set.of(
            new ReconciledHuooPickablePipeline(
                PIPELINE_WITH_ROLE.generatePickableHuooPipelineId(),
                new PipelineId(CONSENTED_PIPELINE_ID)
            ),
            new ReconciledHuooPickablePipeline(
                PIPELINE_WITHOUT_ROLE.generatePickableHuooPipelineId(),
                new PipelineId(APPLICATION_PIPELINE_ID)
            )
        )
    );

  }

  @Test
  public void validateAddPipelineHuooForm_serviceInteraction() {

    var formBindingResult = new BeanPropertyBindingResult(form, "form");

    padPipelinesHuooService.validateAddPipelineHuooForm(
        pwaApplicationDetail,
        form,
        formBindingResult,
        PickHuooPipelineValidationType.PIPELINES,
        HuooRole.HOLDER
    );

    verify(pickHuooPipelinesFormValidator, times(1)).validate(
        form,
        formBindingResult,
        HuooRole.HOLDER,
        pwaApplicationDetail,
        PickHuooPipelineValidationType.PIPELINES);

  }

  @Test
  public void getAssignablePadOrganisationRolesFrom_noRolesFoundForOrgs() {

    var foundRoles = padPipelinesHuooService.getAssignablePadOrganisationRolesFrom(
        pwaApplicationDetail,
        HuooRole.HOLDER,
        Set.of(OrganisationUnitId.from(organisationUnit1), OrganisationUnitId.from(organisationUnit2)),
        Set.of(treatyAgreement));

    assertThat(foundRoles).isEmpty();
  }

  @Test
  public void getAssignablePadOrganisationRolesFrom_rolesFoundForOrgs_treatyRoleNotFound() {
    var org1HolderRole = PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit1,
        HuooRole.HOLDER);
    var org2HolderRole = PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit2,
        HuooRole.HOLDER);

    when(padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(any(), any()))
        .thenReturn(List.of(org1HolderRole, org2HolderRole));

    var foundRoles = padPipelinesHuooService.getAssignablePadOrganisationRolesFrom(
        pwaApplicationDetail,
        HuooRole.HOLDER,
        Set.of(OrganisationUnitId.from(organisationUnit1), OrganisationUnitId.from(organisationUnit2)),
        Set.of(treatyAgreement));

    assertThat(foundRoles).containsExactlyInAnyOrder(org1HolderRole, org2HolderRole);
    verify(padOrganisationRoleService, times(1)).getAssignableOrgRolesForDetailByRole(
        pwaApplicationDetail,
        HuooRole.HOLDER
    );
  }

  @Test
  public void getAssignablePadOrganisationRolesFrom_onlyTreatyRoleFound() {
    var treatyRole = PadOrganisationRole.fromTreatyAgreement(pwaApplicationDetail, treatyAgreement,
        HuooRole.HOLDER);


    when(padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(any(), any()))
        .thenReturn(List.of(treatyRole));

    var foundRoles = padPipelinesHuooService.getAssignablePadOrganisationRolesFrom(
        pwaApplicationDetail,
        HuooRole.HOLDER,
        Set.of(OrganisationUnitId.from(organisationUnit1), OrganisationUnitId.from(organisationUnit2)),
        Set.of(treatyAgreement));

    assertThat(foundRoles).containsExactlyInAnyOrder(treatyRole);

  }

  @Test
  public void getAssignablePadOrganisationRolesFrom_filterOutSplitPipelineHuooTypeRoles() {
    var treatyRole = PadOrganisationRole.fromTreatyAgreement(pwaApplicationDetail, treatyAgreement,
        HuooRole.HOLDER);

    var splitPipelineRole = PadOrganisationRole.forUnassignedSplitPipeline(pwaApplicationDetail, HuooRole.HOLDER);


    when(padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(any(), any()))
        .thenReturn(List.of(treatyRole, splitPipelineRole));

    var foundRoles = padPipelinesHuooService.getAssignablePadOrganisationRolesFrom(
        pwaApplicationDetail,
        HuooRole.HOLDER,
        Set.of(OrganisationUnitId.from(organisationUnit1), OrganisationUnitId.from(organisationUnit2)),
        Set.of(treatyAgreement));

    assertThat(foundRoles).containsExactlyInAnyOrder(treatyRole);

  }

  @Test
  public void updatePipelineHuooLinks_everyPipelineGetsLinkedToEveryRole() {
    var org1HolderRole = PadOrganisationRole.fromOrganisationUnit(
        pwaApplicationDetail,
        organisationUnit1,
        HuooRole.HOLDER);
    var org2HolderRole = PadOrganisationRole.fromOrganisationUnit(
        pwaApplicationDetail,
        organisationUnit2,
        HuooRole.HOLDER);
    var orgTreatyHolderRole = PadOrganisationRole.fromTreatyAgreement(
        pwaApplicationDetail,
        treatyAgreement,
        HuooRole.HOLDER);

    when(padOrganisationRoleService.getAssignableOrgRolesForDetailByRole(pwaApplicationDetail, HuooRole.HOLDER))
        .thenReturn(List.of(org1HolderRole, org2HolderRole, orgTreatyHolderRole));

    var pipeline1 = new Pipeline();
    pipeline1.setId(CONSENTED_PIPELINE_ID);
    var pipeline2 = new Pipeline();
    pipeline2.setId(APPLICATION_PIPELINE_ID);


    padPipelinesHuooService.updatePipelineHuooLinks(
        pwaApplicationDetail,
        Set.of(pipeline1.getPipelineId(), pipeline2.getPipelineId()),
        HuooRole.HOLDER,
        Set.of(OrganisationUnitId.from(organisationUnit1), OrganisationUnitId.from(organisationUnit2)),
        Set.of(treatyAgreement)
    );

    verify(padOrganisationRoleService).deletePadPipelineRoleLinksForPipelineIdentifiersAndRole(
        pwaApplicationDetail,
        Set.of(pipeline1.getPipelineId(), pipeline2.getPipelineId()),
        HuooRole.HOLDER);
    verify(padOrganisationRoleService, times(1)).createPadPipelineOrganisationRoleLink(org1HolderRole, pipeline1.getPipelineId());
    verify(padOrganisationRoleService, times(1)).createPadPipelineOrganisationRoleLink(org1HolderRole, pipeline2.getPipelineId());
    verify(padOrganisationRoleService, times(1)).createPadPipelineOrganisationRoleLink(org2HolderRole, pipeline1.getPipelineId());
    verify(padOrganisationRoleService, times(1)).createPadPipelineOrganisationRoleLink(org2HolderRole, pipeline2.getPipelineId());
    verify(padOrganisationRoleService, times(1)).createPadPipelineOrganisationRoleLink(orgTreatyHolderRole, pipeline1.getPipelineId());
    verify(padOrganisationRoleService, times(1)).createPadPipelineOrganisationRoleLink(orgTreatyHolderRole, pipeline2.getPipelineId());
  }


  @Test
  public void getAvailableOrgUnitDetailsForRole_filtersOrgRolesNotOfDesiredType() {
    //noinspection unchecked
    ArgumentCaptor<List<PortalOrganisationUnit>> orgsforRoleCapture = ArgumentCaptor.forClass(List.class);

    when(padOrganisationRoleService.getOrgRolesForDetail(pwaApplicationDetail)).thenReturn(
        List.of(
            PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit1, HuooRole.HOLDER),
            PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit2, HuooRole.OWNER)
        )
    );

    var orgs = padPipelinesHuooService.getAvailableOrgUnitDetailsForRole(pwaApplicationDetail, HuooRole.HOLDER);

    verify(padOrganisationRoleService, times(1)).getOrgRolesForDetail(pwaApplicationDetail);
    verify(portalOrganisationsAccessor, times(1)).getOrganisationUnitDetailDtos(orgsforRoleCapture.capture());

    assertThat(orgsforRoleCapture.getValue()).containsExactly(organisationUnit1);

  }

  @Test
  public void getAvailableTreatyAgreementsForRole_filtersTreatiesByRole() {
    var holderRole = PadOrganisationRole.fromTreatyAgreement(pwaApplicationDetail, TreatyAgreement.BELGIUM,
        HuooRole.HOLDER);
    var userRole = PadOrganisationRole.fromTreatyAgreement(
        pwaApplicationDetail,
        TreatyAgreement.IRELAND,
        HuooRole.USER);
    var orgRole = PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit2, HuooRole.USER);
    when(padOrganisationRoleService.getOrgRolesForDetail(any())).thenReturn(List.of(
        holderRole, userRole, orgRole
    ));


    var foundTreatiesWithRole = padPipelinesHuooService.getAvailableTreatyAgreementsForRole(pwaApplicationDetail,
        HuooRole.USER);

    assertThat(foundTreatiesWithRole).containsExactlyInAnyOrder(userRole.getAgreement());
  }

  @Test
  public void reconcilePickablePipelinesFromPipelineIds_serviceInteractions_andInvalidPipelineIdProvided() {

    var invalidPickablePipelineId = PickableHuooPipelineType.createPickableString(new PipelineId(NON_MASTER_PWA_PIPELINE_ID));

    var reconciledPickablePipeline = new ReconciledHuooPickablePipeline(
        PIPELINE_WITH_ROLE.generatePickableHuooPipelineId(),
        PIPELINE_WITH_ROLE.asPipelineIdentifier()
    );

    var result = padPipelinesHuooService.reconcilePickablePipelinesFromPipelineIds(
        pwaApplicationDetail,
        DEFAULT_ROLE,
        Set.of(PIPELINE_WITH_ROLE.getPickableString(), invalidPickablePipelineId)
    );

    assertThat(result).containsExactly(
        reconciledPickablePipeline
    );

  }

  @Test
  public void reconcileOrganisationRoleOwnersFrom_serviceInteractions_andInvalidOptionsProvided() {
    var role = HuooRole.HOLDER;
    var validOrgUnitId = 1;
    var validTreaty = TreatyAgreement.NORWAY;
    var invalidOrgUnitId = 2;
    var invalidTreaty = TreatyAgreement.BELGIUM;

    var validOrgRoleInstances = Set.of(
        OrganisationRoleDtoTestUtil.createTreatyOrgRoleInstance(role, validTreaty),
        OrganisationRoleDtoTestUtil.createOrganisationUnitOrgRoleInstance(role, validOrgUnitId)

    );
    when(padOrganisationRoleService.getAssignableOrganisationRoleInstanceDtosByRole(
        pwaApplicationDetail,
        role
    )).thenReturn(validOrgRoleInstances);

    var result = padPipelinesHuooService.reconcileOrganisationRoleOwnersFrom(
        pwaApplicationDetail,
        role,
        Set.of(validOrgUnitId, invalidOrgUnitId),
        Set.of(validTreaty, invalidTreaty)
    );

    assertThat(result).containsExactlyInAnyOrder(
        OrganisationRoleDtoTestUtil.createTreatyOrgRoleInstance(role, validTreaty).getOrganisationRoleOwnerDto(),
        OrganisationRoleDtoTestUtil.createOrganisationUnitOrgRoleInstance(
            role,
            validOrgUnitId
        ).getOrganisationRoleOwnerDto()

    );


  }

  @Test
  public void getSortedPickablePipelineOptionsForApplicationDetail_whenOnlyWholePipelines(){

    var result = padPipelinesHuooService.getSortedPickablePipelineOptionsForApplicationDetail(
        pwaApplicationDetail,
        DEFAULT_ROLE

    );
  }

  @Test
  public void getSplitablePipelineOverviewForApplication_serviceInteractions() {

    var overviewMap = new HashMap<PipelineId, PipelineOverview>();
    overviewMap.put(new PipelineId(CONSENTED_PIPELINE_ID), consentedPipelineOverview);

    when(padPipelineService.getAllPipelineOverviewsFromAppAndMasterPwa(pwaApplicationDetail))
        .thenReturn(overviewMap);


    var overview = padPipelinesHuooService.getSplitablePipelineOverviewForApplication(
        pwaApplicationDetail,
        new PipelineId(CONSENTED_PIPELINE_ID)
    );

    assertThat(overview).contains(consentedPipelineOverview);


  }

  @Test
  public void getSplitablePipelinesForAppAndMasterPwa_stripsOutPipelinesWithZeroIdents() {
    when(consentedPipelineOverview.getNumberOfIdents()).thenReturn(0L);

    var overviewMap = new HashMap<PipelineId, PipelineOverview>();
    overviewMap.put(new PipelineId(CONSENTED_PIPELINE_ID), consentedPipelineOverview);

    when(padPipelineService.getAllPipelineOverviewsFromAppAndMasterPwa(pwaApplicationDetail))
        .thenReturn(overviewMap);

    var overviews = padPipelinesHuooService.getSplitablePipelinesForAppAndMasterPwa(pwaApplicationDetail);

    assertThat(overviews).isEmpty();

  }

  @Test
  public void getSplitablePipelinesForAppAndMasterPwa_containsPipelinesWithOneOrMoreIdents() {
    var overviewMap = new HashMap<PipelineId, PipelineOverview>();
    overviewMap.put(new PipelineId(CONSENTED_PIPELINE_ID), consentedPipelineOverview);

    when(padPipelineService.getAllPipelineOverviewsFromAppAndMasterPwa(pwaApplicationDetail))
        .thenReturn(overviewMap);

    var overviews = padPipelinesHuooService.getSplitablePipelinesForAppAndMasterPwa(pwaApplicationDetail);

    assertThat(overviews).containsExactly(consentedPipelineOverview);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getSplitablePipelineForAppAndMasterPwaOrError_throwsErrorWhenPipelineNotFound() {

    var overviewMap = new HashMap<PipelineId, PipelineOverview>();

    when(padPipelineService.getAllPipelineOverviewsFromAppAndMasterPwa(pwaApplicationDetail))
        .thenReturn(overviewMap);


    var overview = padPipelinesHuooService.getSplitablePipelineForAppAndMasterPwaOrError(
        pwaApplicationDetail,
        new PipelineId(CONSENTED_PIPELINE_ID)
    );

  }

  @Test
  public void getSplitablePipelineForAppAndMasterPwaOrError_whenPipelineFound() {

    var overviewMap = new HashMap<PipelineId, PipelineOverview>();
    overviewMap.put(new PipelineId(CONSENTED_PIPELINE_ID), consentedPipelineOverview);

    when(padPipelineService.getAllPipelineOverviewsFromAppAndMasterPwa(pwaApplicationDetail))
        .thenReturn(overviewMap);


    var overview = padPipelinesHuooService.getSplitablePipelineForAppAndMasterPwaOrError(
        pwaApplicationDetail,
        new PipelineId(CONSENTED_PIPELINE_ID)
    );

    assertThat(overview).isEqualTo(consentedPipelineOverview);

  }

  @Test
  public void removeSplitsForPipeline_removesTemporarySplitRole_whenNoPipelineLinksToRoleRemain(){
    var pipelineId =  new PipelineId(CONSENTED_PIPELINE_ID);
    var pipeline = new Pipeline();
    pipeline.setId(pipelineId.asInt());

    var tempSplitRole = PadOrganisationRole.forUnassignedSplitPipeline(pwaApplicationDetail, DEFAULT_ROLE);
    var tempSplitRolePipelineLink1 = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        tempSplitRole, pipeline, "A", "B", 1);
    var tempSplitRolePipelineLink2 = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        tempSplitRole, pipeline, "B", "C", 2);

    when(padPipelineOrganisationRoleLinkRepository.countByPadOrgRole(any())).thenReturn(0L);

    when(padPipelineOrganisationRoleLinkRepository
        .findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_RoleAndPipeline_IdIn(
            pwaApplicationDetail, DEFAULT_ROLE, Set.of(pipelineId.asInt())
        )
    ).thenReturn(List.of(
        tempSplitRolePipelineLink1,
        tempSplitRolePipelineLink2
    ));

    padPipelinesHuooService.removeSplitsForPipeline(
        pwaApplicationDetail,
       pipelineId,
        DEFAULT_ROLE
    );

    verify(padOrganisationRoleService, times(1))
        .removalPipelineOrgRoleLinks(List.of(tempSplitRolePipelineLink1, tempSplitRolePipelineLink2));
    verify(padOrganisationRoleService, times(1))
        .removeOrgRole(tempSplitRole);

  }

  @Test
  public void removeSplitsForPipeline_doesNotremoveTemporarySplitRole_whenPipelineLinksToRoleRemain(){
    var pipelineId =  new PipelineId(CONSENTED_PIPELINE_ID);
    var pipeline = new Pipeline();
    pipeline.setId(pipelineId.asInt());

    var tempSplitRole = PadOrganisationRole.forUnassignedSplitPipeline(pwaApplicationDetail, DEFAULT_ROLE);
    var tempSplitRolePipelineLink1 = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        tempSplitRole, pipeline, "A", "B", 1);
    var tempSplitRolePipelineLink2 = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        tempSplitRole, pipeline, "B", "C", 2);

    when(padPipelineOrganisationRoleLinkRepository.countByPadOrgRole(any())).thenReturn(1L);

    when(padPipelineOrganisationRoleLinkRepository
        .findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_RoleAndPipeline_IdIn(
            pwaApplicationDetail, DEFAULT_ROLE, Set.of(pipelineId.asInt())
        )
    ).thenReturn(List.of(
        tempSplitRolePipelineLink1,
        tempSplitRolePipelineLink2
    ));

    padPipelinesHuooService.removeSplitsForPipeline(
        pwaApplicationDetail,
        pipelineId,
        DEFAULT_ROLE
    );

    verify(padOrganisationRoleService, times(1))
        .removalPipelineOrgRoleLinks(List.of(tempSplitRolePipelineLink1, tempSplitRolePipelineLink2));
    verify(padOrganisationRoleService, times(0))
        .removeOrgRole(tempSplitRole);

  }

  @Test
  public void removeSplitsForPipeline_doesNotRemovePortalOrgOrTreatyRoles(){
    var pipelineId =  new PipelineId(CONSENTED_PIPELINE_ID);
    var pipeline = new Pipeline();
    pipeline.setId(pipelineId.asInt());

    var portalOrgRole = PadOrganisationRole.fromOrganisationUnit(pwaApplicationDetail, organisationUnit1, DEFAULT_ROLE);
    var treatyOrgRole = PadOrganisationRole.fromTreatyAgreement(pwaApplicationDetail, TreatyAgreement.NORWAY, DEFAULT_ROLE);

    var treatyorgLink = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        treatyOrgRole, pipeline, "A", "B", 1);
    var portalOrgLink = PadOrganisationRoleTestUtil.createOrgRoleInclusivePipelineSplitLink(
        portalOrgRole, pipeline, "B", "C", 2);

    when(padPipelineOrganisationRoleLinkRepository
        .findByPadOrgRole_pwaApplicationDetailAndPadOrgRole_RoleAndPipeline_IdIn(
            pwaApplicationDetail, DEFAULT_ROLE, Set.of(pipelineId.asInt())
        )
    ).thenReturn(List.of(
        treatyorgLink,
        portalOrgLink
    ));

    padPipelinesHuooService.removeSplitsForPipeline(
        pwaApplicationDetail,
        pipelineId,
        DEFAULT_ROLE
    );

    verify(padOrganisationRoleService, times(1))
        .removalPipelineOrgRoleLinks(List.of(treatyorgLink, portalOrgLink));
    verify(padOrganisationRoleService, times(0)).removeOrgRole(any());

  }


  @Test
  public void replacePipelineSectionsForPipelineAndRole_createsRolesLinksAsExpected() {

    when(padOrganisationRoleService.getOrCreateUnassignedPipelineSplitRole(pwaApplicationDetail, HuooRole.HOLDER))
        .thenReturn(PadOrganisationRole.forUnassignedSplitPipeline(pwaApplicationDetail, HuooRole.HOLDER));
    when(padPipelineOrganisationRoleLinkRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));


    var pipelineId = new PipelineId(10);
    var section1 = PipelineSection.from(
        pipelineId,
        1,
        PipelineIdentPoint.from("POINT1", IdentLocationInclusionMode.INCLUSIVE),
        PipelineIdentPoint.from("POINT2", IdentLocationInclusionMode.INCLUSIVE)
    );
    var section2 = PipelineSection.from(
        pipelineId,
        2,
        PipelineIdentPoint.from("POINT2", IdentLocationInclusionMode.EXCLUSIVE),
        PipelineIdentPoint.from("POINT3", IdentLocationInclusionMode.INCLUSIVE)
    );
    var pipelineSections = List.of(section1, section2);

    var newRoles = padPipelinesHuooService.replacePipelineSectionsForPipelineAndRole(
        pwaApplicationDetail, HuooRole.HOLDER, pipelineId, pipelineSections
    );

    assertThat(newRoles).hasSize(2);
    assertThat(newRoles).anySatisfy(section1RoleLink -> {
      assertThat(section1RoleLink.getPadOrgRole().getType()).isEqualTo(HuooType.UNASSIGNED_PIPELINE_SPLIT);
      assertThat(section1RoleLink.getSectionNumber()).isEqualTo(1);
      assertThat(section1RoleLink.getFromLocation()).isEqualTo("POINT1");
      assertThat(section1RoleLink.getFromLocationIdentInclusionMode()).isEqualTo(IdentLocationInclusionMode.INCLUSIVE);
      assertThat(section1RoleLink.getToLocation()).isEqualTo("POINT2");
      assertThat(section1RoleLink.getToLocationIdentInclusionMode()).isEqualTo(IdentLocationInclusionMode.INCLUSIVE);
    });

    assertThat(newRoles).anySatisfy(section2RoleLink -> {
      assertThat(section2RoleLink.getPadOrgRole().getType()).isEqualTo(HuooType.UNASSIGNED_PIPELINE_SPLIT);
      assertThat(section2RoleLink.getSectionNumber()).isEqualTo(2);
      assertThat(section2RoleLink.getFromLocation()).isEqualTo("POINT2");
      assertThat(section2RoleLink.getFromLocationIdentInclusionMode()).isEqualTo(IdentLocationInclusionMode.EXCLUSIVE);
      assertThat(section2RoleLink.getToLocation()).isEqualTo("POINT3");
      assertThat(section2RoleLink.getToLocationIdentInclusionMode()).isEqualTo(IdentLocationInclusionMode.INCLUSIVE);
    });
  }
}