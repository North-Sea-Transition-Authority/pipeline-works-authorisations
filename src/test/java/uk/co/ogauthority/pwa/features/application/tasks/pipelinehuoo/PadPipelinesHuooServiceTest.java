package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentPoint;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRole;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickHuooPipelineValidationType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickHuooPipelinesForm;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickHuooPipelinesFormValidator;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineOption;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickableHuooPipelineType;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.PickablePipelineOptionTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.modifyhuoo.ReconciledHuooPickablePipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.PadPipelineHuooViewFactory;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.PipelineAndOrgRoleGroupViewsByRoleTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.PipelineHuooRoleSummaryViewTestUtil;
import uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview.PipelinesAndOrgRoleGroupViewTestUtil;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.view.PipelineAndIdentView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelinesHuooServiceTest {
  private final HuooRole DEFAULT_ROLE = HuooRole.HOLDER;

  private static final String POINT_A = "A";
  private static final String POINT_B = "B";
  private static final String POINT_C = "C";
  private static final String POINT_D = "D";

  private final int CONSENTED_PIPELINE_ID = 10;
  private final int APPLICATION_PIPELINE_ID = 20;
  private final int NON_MASTER_PWA_PIPELINE_ID = 30;
  private final PickableHuooPipelineOption PIPELINE_WITH_ROLE = PickablePipelineOptionTestUtil.createOption(
      new PipelineId(CONSENTED_PIPELINE_ID),
      "PL1");
  private final PickableHuooPipelineOption PIPELINE_WITHOUT_ROLE = PickablePipelineOptionTestUtil.createOption(
      new PipelineId(APPLICATION_PIPELINE_ID),
      "TEMP_1");

  private static final PipelineIdentifier PIPELINE_1_ID = new PipelineId(1);
  private static final PipelineIdentifier PIPELINE_1_SECTION_1 = PipelineSection.from(
      PIPELINE_1_ID.getPipelineId(), 1,
      PipelineIdentPoint.inclusivePoint(POINT_A),
      PipelineIdentPoint.exclusivePoint(POINT_B)
  );
  private static final PipelineIdentifier PIPELINE_1_SECTION_2 = PipelineSection.from(
      PIPELINE_1_ID.getPipelineId(), 2,
      PipelineIdentPoint.inclusivePoint(POINT_B),
      PipelineIdentPoint.exclusivePoint(POINT_C)
  );
  private static final PipelineIdentifier PIPELINE_1_SECTION_3 = PipelineSection.from(
      PIPELINE_1_ID.getPipelineId(), 3,
      PipelineIdentPoint.inclusivePoint(POINT_C),
      PipelineIdentPoint.inclusivePoint(POINT_D)
  );

  private static final String PIPELINE_1_NUMBER = "PL1";
  private static final int ORGANISATION_UNIT_1_ID = 10;
  private static final String ORGANISATION_UNIT_1_NAME = "ORG1";

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
  private PipelineOverview consentedPipelineOverview;

  @Mock
  private PipelineOverview pipeline1Overview;

  @Mock
  private IdentView ident1View;

  @Mock
  private IdentView ident2View;

  @Mock
  private IdentView ident3View;


  @Mock
  private PadPipelineHuooViewFactory padPipelineHuooViewFactory;

  @Mock
  private PipelineAndIdentViewFactory pipelineAndIdentViewFactory;

  @Mock
  private PadOptionConfirmedService padOptionConfirmedService;

  private PwaApplicationDetail pwaApplicationDetail;
  private PickHuooPipelinesForm form;

  private PortalOrganisationUnit organisationUnit1;
  private PortalOrganisationUnit organisationUnit2;

  private PipelineAndIdentView pipeline1AndIdents;

  private TreatyAgreement treatyAgreement = TreatyAgreement.ANY_TREATY_COUNTRY;

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

    setupIdentViewMock(ident1View, POINT_A, POINT_B, 1);
    setupIdentViewMock(ident2View, POINT_B, POINT_C, 2);
    setupIdentViewMock(ident3View, POINT_C, POINT_D, 3);

    padPipelinesHuooService = new PadPipelinesHuooService(
        pickableHuooPipelineService,
        portalOrganisationsAccessor,
        padOrganisationRoleService,
        pickHuooPipelinesFormValidator,
        padPipelineOrganisationRoleLinkRepository,
        pipelineAndIdentViewFactory,
        padPipelineHuooViewFactory,
        padOptionConfirmedService);

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

    setupIdentViewMock(ident1View, POINT_A, POINT_B, 1);
    setupIdentViewMock(ident2View, POINT_B, POINT_C, 2);
    setupIdentViewMock(ident3View, POINT_C, POINT_D, 3);


  }

  private void setupPipelineAndIdentView(){
    when(pipeline1Overview.getPipelineNumber()).thenReturn(PIPELINE_1_NUMBER);
    when(pipeline1Overview.getPipelineId()).thenReturn(PIPELINE_1_ID.getPipelineIdAsInt());
    pipeline1AndIdents = new PipelineAndIdentView(pipeline1Overview, List.of(ident1View, ident2View, ident3View));
    when(pipelineAndIdentViewFactory.getAllAppAndMasterPwaPipelineAndIdentViews(pwaApplicationDetail, PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES))
        .thenReturn(List.of(pipeline1AndIdents));


  }

  private void setupIdentViewMock(IdentView mockIdentView,
                                  String fromLocation,
                                  String toLocation,
                                  int identNumber) {
    when(mockIdentView.getFromLocation()).thenReturn(fromLocation);
    when(mockIdentView.getToLocation()).thenReturn(toLocation);
    when(mockIdentView.getIdentNumber()).thenReturn(identNumber);

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
    var holderRole = PadOrganisationRole.fromTreatyAgreement(pwaApplicationDetail, TreatyAgreement.ANY_TREATY_COUNTRY,
        HuooRole.HOLDER);
    var userRole = PadOrganisationRole.fromTreatyAgreement(
        pwaApplicationDetail,
        TreatyAgreement.ANY_TREATY_COUNTRY,
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
  public void reconcileOrganisationRoleOwnersFrom_serviceInteractions_andInvalidOrgProvided() {
    var role = HuooRole.HOLDER;
    var validOrgUnitId = 1;
    var validTreaty = TreatyAgreement.ANY_TREATY_COUNTRY;
    var invalidOrgUnitId = 2;

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
        Set.of(validTreaty)
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

    assertThat(padPipelinesHuooService.getSortedPickablePipelineOptionsForApplicationDetail(
        pwaApplicationDetail,
        DEFAULT_ROLE
    )).allSatisfy(pickableHuooPipelineOption ->
        assertThat(pickableHuooPipelineOption.getPickableHuooPipelineType()).isEqualTo(PickableHuooPipelineType.FULL)
    );
  }

  @Test
  public void getSplitablePipelineOverviewForApplication_serviceInteractions() {

    var overviewMap = new HashMap<PipelineId, PipelineOverview>();
    overviewMap.put(new PipelineId(CONSENTED_PIPELINE_ID), consentedPipelineOverview);

    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail
        , PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES
    ))
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

    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES
    ))
        .thenReturn(overviewMap);

    var overviews = padPipelinesHuooService.getSplitablePipelinesForAppAndMasterPwa(pwaApplicationDetail);

    assertThat(overviews).isEmpty();

  }

  @Test
  public void getSplitablePipelinesForAppAndMasterPwa_containsPipelinesWithOneOrMoreIdents() {
    var overviewMap = new HashMap<PipelineId, PipelineOverview>();
    overviewMap.put(new PipelineId(CONSENTED_PIPELINE_ID), consentedPipelineOverview);

    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES
    ))
        .thenReturn(overviewMap);

    var overviews = padPipelinesHuooService.getSplitablePipelinesForAppAndMasterPwa(pwaApplicationDetail);

    assertThat(overviews).containsExactly(consentedPipelineOverview);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getSplitablePipelineForAppAndMasterPwaOrError_throwsErrorWhenPipelineNotFound() {

    var overviewMap = new HashMap<PipelineId, PipelineOverview>();

    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES
    ))
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

    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ONLY_ON_SEABED_PIPELINES
    ))
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
    var treatyOrgRole = PadOrganisationRole.fromTreatyAgreement(pwaApplicationDetail, TreatyAgreement.ANY_TREATY_COUNTRY, DEFAULT_ROLE);

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

  @Test
  public void getPadPipelinesHuooSummaryView_verifyServiceInteractions() {
    when(padPipelineOrganisationRoleLinkRepository.findOrganisationPipelineRoleDtoByPwaApplicationDetail(
        pwaApplicationDetail)).thenReturn(List.of());

    var summaryView = padPipelinesHuooService.getPadPipelinesHuooSummaryView(pwaApplicationDetail);

    verify(padPipelineHuooViewFactory,times(1)).createPipelineAndOrgGroupViewsByRole(
        eq(pwaApplicationDetail),
        isNotNull());
  }

  @Test
  public void isComplete_whenValid() {
    var rolesByView = PipelineAndOrgRoleGroupViewsByRoleTestUtil.createFrom(
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.HOLDER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.USER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OPERATOR),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OWNER)
    );
    when(padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(eq(pwaApplicationDetail), any()))
        .thenReturn(rolesByView);

    assertThat(padPipelinesHuooService.isComplete(pwaApplicationDetail)).isTrue();

  }

  @Test
  public void isComplete_whenInValid() {
    var organisationRoleOwnerDto1 = OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(ORGANISATION_UNIT_1_ID);

    var rolesByView = PipelineAndOrgRoleGroupViewsByRoleTestUtil.createFrom(
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.HOLDER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.USER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithUnassigned(HuooRole.OPERATOR,
            Map.of(PIPELINE_1_ID, PIPELINE_1_NUMBER),
            Map.of(organisationRoleOwnerDto1, ORGANISATION_UNIT_1_NAME)
        ),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OWNER)
    );
    when(padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(eq(pwaApplicationDetail), any()))
        .thenReturn(rolesByView);

    assertThat(padPipelinesHuooService.isComplete(pwaApplicationDetail)).isFalse();

  }

  @Test
  public void generatePipelineHuooValidationResult_whenHolderHasPipelineAndOrgRoleOwnersUnnassigned() {
    setupPipelineAndIdentView();
    var organisationRoleOwnerDto1 = OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(ORGANISATION_UNIT_1_ID);

    var rolesByView = PipelineAndOrgRoleGroupViewsByRoleTestUtil.createFrom(
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithUnassigned(HuooRole.HOLDER,
            Map.of(PIPELINE_1_ID, PIPELINE_1_NUMBER),
            Map.of(organisationRoleOwnerDto1, ORGANISATION_UNIT_1_NAME)),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.USER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OPERATOR),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OWNER)
    );

    var validationResult = padPipelinesHuooService.generatePipelineHuooValidationResult(pwaApplicationDetail, rolesByView);

    assertThat(validationResult.isValid()).isFalse();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).hasErrors()).isTrue();
    assertThat(validationResult.getValidationResult(HuooRole.USER).hasErrors()).isFalse();
    assertThat(validationResult.getValidationResult(HuooRole.OPERATOR).hasErrors()).isFalse();
    assertThat(validationResult.getValidationResult(HuooRole.OWNER).hasErrors()).isFalse();

    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getUnassignedPipelineErrorMessage()).isNotNull();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getUnassignedRoleOwnerErrorMessage()).isNotNull();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getInvalidSplitsErrorMessage()).isNull();
  }

  @Test
  public void generatePipelineHuooValidationResult_whenUnassignedPipelineSections_andSectionsValid() {
    setupPipelineAndIdentView();
    var organisationRoleOwnerDto1 = OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(ORGANISATION_UNIT_1_ID);

    var rolesByView = PipelineAndOrgRoleGroupViewsByRoleTestUtil.createFrom(
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithUnassigned(HuooRole.HOLDER,
            Map.ofEntries(
                Map.entry(PIPELINE_1_SECTION_1, PIPELINE_1_NUMBER+"/1"),
                Map.entry(PIPELINE_1_SECTION_2, PIPELINE_1_NUMBER+"/2"),
                Map.entry(PIPELINE_1_SECTION_3, PIPELINE_1_NUMBER+"/3")
            ),
            Map.of(organisationRoleOwnerDto1, ORGANISATION_UNIT_1_NAME)),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.USER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OPERATOR),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OWNER)
    );

    var validationResult = padPipelinesHuooService.generatePipelineHuooValidationResult(pwaApplicationDetail, rolesByView);

    assertThat(validationResult.isValid()).isFalse();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).hasErrors()).isTrue();

    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getUnassignedPipelineErrorMessage()).isNotNull();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getUnassignedRoleOwnerErrorMessage()).isNotNull();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getInvalidSplitsErrorMessage()).isNull();
  }

  @Test
  public void generatePipelineHuooValidationResult_whenUnassignedAndAssignedPipelineSections_andFinalSectionInvalid() {
    setupPipelineAndIdentView();
    var organisationRoleOwnerDto1 = OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(ORGANISATION_UNIT_1_ID);

    var rolesByView = PipelineAndOrgRoleGroupViewsByRoleTestUtil.createFrom(
        PipelineHuooRoleSummaryViewTestUtil.createWithNoUnassignedPipelinesOrOrgs(HuooRole.HOLDER,
            List.of(
                PipelinesAndOrgRoleGroupViewTestUtil.createMultiPipelineSingleOrgGroupView(
                    Set.of(
                        PIPELINE_1_SECTION_1, PIPELINE_1_SECTION_2,
                        // dodgy section
                        PipelineSection.from(
                            PIPELINE_1_ID.getPipelineId(),
                            4,
                            PipelineIdentPoint.inclusivePoint("POINT Y"),
                            PipelineIdentPoint.inclusivePoint("POINT Y")
                        )
                    ),
                    List.of(
                        PIPELINE_1_NUMBER + "/1", PIPELINE_1_NUMBER + "/2", PIPELINE_1_NUMBER + "/4"
                    ),
                    organisationRoleOwnerDto1,
                    ORGANISATION_UNIT_1_NAME
                )
              )
        ),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.USER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OPERATOR),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OWNER)
    );

    var validationResult = padPipelinesHuooService.generatePipelineHuooValidationResult(pwaApplicationDetail, rolesByView);

    assertThat(validationResult.isValid()).isFalse();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).hasErrors()).isTrue();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getUnassignedPipelineErrorMessage()).isNull();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getUnassignedRoleOwnerErrorMessage()).isNull();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getInvalidSplitsErrorMessage()).contains(PIPELINE_1_NUMBER);
  }

  @Test
  public void generatePipelineHuooValidationResult_whenUnassignedAndAssignedPipelineSections_andFirstSectionInvalid() {
    setupPipelineAndIdentView();
    var organisationRoleOwnerDto1 = OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(ORGANISATION_UNIT_1_ID);

    var rolesByView = PipelineAndOrgRoleGroupViewsByRoleTestUtil.createFrom(
        PipelineHuooRoleSummaryViewTestUtil.createWithNoUnassignedPipelinesOrOrgs(HuooRole.HOLDER,
            List.of(
                PipelinesAndOrgRoleGroupViewTestUtil.createMultiPipelineSingleOrgGroupView(
                    Set.of(
                        PipelineSection.from(
                            // dodgy section
                            PIPELINE_1_ID.getPipelineId(),
                            1,
                            PipelineIdentPoint.inclusivePoint("POINT Y"),
                            PipelineIdentPoint.inclusivePoint("POINT Y")
                        ),
                        PIPELINE_1_SECTION_2, PIPELINE_1_SECTION_3
                    ),
                    List.of(
                        PIPELINE_1_NUMBER + "/1", PIPELINE_1_NUMBER + "/2", PIPELINE_1_NUMBER + "/3"
                    ),
                    organisationRoleOwnerDto1,
                    ORGANISATION_UNIT_1_NAME
                )
            )
        ),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.USER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OPERATOR),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OWNER)
    );

    var validationResult = padPipelinesHuooService.generatePipelineHuooValidationResult(pwaApplicationDetail, rolesByView);

    assertThat(validationResult.isValid()).isFalse();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).hasErrors()).isTrue();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getUnassignedPipelineErrorMessage()).isNull();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getUnassignedRoleOwnerErrorMessage()).isNull();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getInvalidSplitsErrorMessage()).contains(PIPELINE_1_NUMBER);
  }

  @Test
  public void generatePipelineHuooValidationResult_whenUnassignedAndAssignedPipelineSections_andMiddleSectionInvalidLocationsForIdent() {
    setupPipelineAndIdentView();
    var organisationRoleOwnerDto1 = OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(ORGANISATION_UNIT_1_ID);

    var rolesByView = PipelineAndOrgRoleGroupViewsByRoleTestUtil.createFrom(
        PipelineHuooRoleSummaryViewTestUtil.createWithNoUnassignedPipelinesOrOrgs(HuooRole.HOLDER,
            List.of(
                PipelinesAndOrgRoleGroupViewTestUtil.createMultiPipelineSingleOrgGroupView(
                    Set.of(
                        PIPELINE_1_SECTION_1,
                        PipelineSection.from(
                            // dodgy section
                            PIPELINE_1_ID.getPipelineId(),
                            2,
                            PipelineIdentPoint.inclusivePoint(POINT_C),
                            PipelineIdentPoint.inclusivePoint(POINT_B)
                        ),
                        PIPELINE_1_SECTION_3
                    ),
                    List.of(
                        PIPELINE_1_NUMBER + "/1", PIPELINE_1_NUMBER + "/2", PIPELINE_1_NUMBER + "/3"
                    ),
                    organisationRoleOwnerDto1,
                    ORGANISATION_UNIT_1_NAME
                )
            )
        ),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.USER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OPERATOR),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OWNER)
    );

    var validationResult = padPipelinesHuooService.generatePipelineHuooValidationResult(pwaApplicationDetail, rolesByView);

    assertThat(validationResult.isValid()).isFalse();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).hasErrors()).isTrue();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getUnassignedPipelineErrorMessage()).isNull();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getUnassignedRoleOwnerErrorMessage()).isNull();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).getInvalidSplitsErrorMessage()).contains(PIPELINE_1_NUMBER);
  }

  @Test
  public void generatePipelineHuooValidationResult_whenAllRolesAreValid() {
    setupPipelineAndIdentView();
    var rolesByView = PipelineAndOrgRoleGroupViewsByRoleTestUtil.createFrom(
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.HOLDER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.USER),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OPERATOR),
        PipelineHuooRoleSummaryViewTestUtil.createEmptyGroupWithNoUnassigned(HuooRole.OWNER)
    );

    var validationResult = padPipelinesHuooService.generatePipelineHuooValidationResult(pwaApplicationDetail, rolesByView);

    assertThat(validationResult.isValid()).isTrue();
    assertThat(validationResult.getValidationResult(HuooRole.HOLDER).hasErrors()).isFalse();
    assertThat(validationResult.getValidationResult(HuooRole.USER).hasErrors()).isFalse();
    assertThat(validationResult.getValidationResult(HuooRole.OPERATOR).hasErrors()).isFalse();
    assertThat(validationResult.getValidationResult(HuooRole.OWNER).hasErrors()).isFalse();


  }

  @Test
  public void canShowInTaskList_notOptionsVariation() {
    var notOptions = EnumSet.allOf(PwaApplicationType.class);
    notOptions.remove(PwaApplicationType.OPTIONS_VARIATION);

    for (PwaApplicationType type : notOptions) {
      pwaApplicationDetail.getPwaApplication().setApplicationType(type);
      assertThat(padPipelinesHuooService.canShowInTaskList(pwaApplicationDetail)).isTrue();
    }

  }

  @Test
  public void canShowInTaskList_OptionsVariation_optionsNotComplete() {
    when(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).thenReturn(false);

    pwaApplicationDetail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(padPipelinesHuooService.canShowInTaskList(pwaApplicationDetail)).isFalse();

  }

  @Test
  public void canShowInTaskList_OptionsVariation_optionsComplete() {
    when(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).thenReturn(true);

    pwaApplicationDetail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(padPipelinesHuooService.canShowInTaskList(pwaApplicationDetail)).isTrue();

  }

}