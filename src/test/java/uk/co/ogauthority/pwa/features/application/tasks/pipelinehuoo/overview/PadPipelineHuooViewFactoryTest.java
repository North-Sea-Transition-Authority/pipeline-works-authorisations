package uk.co.ogauthority.pwa.features.application.tasks.pipelinehuoo.overview;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationsDtoTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates.PipelineAndOrganisationRoleGroupSummaryDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifierTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;
import uk.co.ogauthority.pwa.domain.pwa.pipelinehuoo.model.PipelineNumbersAndSplits;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.features.application.tasks.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineHuooViewFactoryTest {

  private static final int OU_ID1 = 10;
  private static final int PIPELINE_1_ID = 100;
  private static final String PIPELINE_1_NUMBER = "PL1";
  private static final int OU_ID2 = 20;
  private static final int PIPELINE_2_ID = 200;
  private static final String PIPELINE_2_NUMBER = "PL2";

  private static final int PIPELINE_3_ID = 300;
  private static final String PIPELINE_POINT_1 = "START";
  private static final String PIPELINE_POINT_2 = "MID";
  private static final String PIPELINE_POINT_3 = "END";
  private static final String PIPELINE_3_NUMBER = "PL3";
  private static final String PIPELINE_3_SECTION1 = "from and including START to and including MID";
  private static final String PIPELINE_3_SECTION2 = "from and including MID to and including END";

  private OrganisationPipelineRoleInstanceDto holderOrg1Pipeline1RoleDto;
  private OrganisationPipelineRoleInstanceDto userOrg2Pipeline2RoleDto;
  private OrganisationPipelineRoleInstanceDto operatorOrg1Pipeline1RoleDto;
  private OrganisationPipelineRoleInstanceDto ownerOrg1Pipeline1RoleDto;
  private OrganisationPipelineRoleInstanceDto ownerBelgiumPipeline2RoleDto;

  private PipelineSection pipeline3Section1;
  private PipelineSection pipeline3Section2;

  private OrganisationUnitDetailDto ou1DetailDto = OrganisationsDtoTestUtil.createDetailDto(OU_ID1, "OU_1", "12345");
  private OrganisationUnitDetailDto ou2DetailDto = OrganisationsDtoTestUtil.createDetailDto(OU_ID2, "OU_2", null);

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private PadOrganisationRoleService padOrganisationRoleService;

  private PadPipelineHuooViewFactory padPipelineHuooViewFactory;

  private PwaApplicationDetail pwaApplicationDetail;

  private PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto;

  @Before
  public void setup() {

    holderOrg1Pipeline1RoleDto = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_1_ID);
    userOrg2Pipeline2RoleDto = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.USER, OU_ID2, PIPELINE_2_ID);
    operatorOrg1Pipeline1RoleDto = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.OPERATOR, OU_ID1,
        PIPELINE_1_ID);
    ownerOrg1Pipeline1RoleDto = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.OWNER, OU_ID1, PIPELINE_1_ID);
    ownerBelgiumPipeline2RoleDto = OrganisationRoleDtoTestUtil.createTreatyOrgUnitPipelineRoleInstance(
        HuooRole.OWNER, TreatyAgreement.ANY_TREATY_COUNTRY, PIPELINE_2_ID
    );

    var orgRoleDtos = Set.of(
        holderOrg1Pipeline1RoleDto.getOrganisationRoleInstanceDto(),
        userOrg2Pipeline2RoleDto.getOrganisationRoleInstanceDto(),
        operatorOrg1Pipeline1RoleDto.getOrganisationRoleInstanceDto(),
        ownerOrg1Pipeline1RoleDto.getOrganisationRoleInstanceDto(),
        ownerBelgiumPipeline2RoleDto.getOrganisationRoleInstanceDto()
    );
    when(padOrganisationRoleService.getAssignableOrganisationRoleDtos(any()))
        .thenReturn(orgRoleDtos);

    when(portalOrganisationsAccessor.getOrganisationUnitDetailDtosByOrganisationUnitId(any()))
        .thenReturn(List.of(ou1DetailDto, ou2DetailDto));

    pipelineAndOrganisationRoleGroupSummaryDto = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holderOrg1Pipeline1RoleDto, userOrg2Pipeline2RoleDto, operatorOrg1Pipeline1RoleDto,
            ownerOrg1Pipeline1RoleDto, ownerBelgiumPipeline2RoleDto));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    pipeline3Section1 = PipelineIdentifierTestUtil.createInclusivePipelineSection(PIPELINE_3_ID, PIPELINE_POINT_1, PIPELINE_POINT_2);
    pipeline3Section2 = PipelineIdentifierTestUtil.createInclusivePipelineSection(PIPELINE_3_ID, PIPELINE_POINT_2, PIPELINE_POINT_3);

    padPipelineHuooViewFactory = new PadPipelineHuooViewFactory(
        portalOrganisationsAccessor,
        padPipelineService,
        padOrganisationRoleService);

    var pipelineSplitInfoLookup = createPipelineNumberAndSplitMap();
    when( padOrganisationRoleService.getAllPipelineNumbersAndSplitsForRole(eq(pwaApplicationDetail), any()))
        .thenReturn(pipelineSplitInfoLookup);
  }

  private Map<PipelineIdentifier, PipelineNumbersAndSplits> createPipelineNumberAndSplitMap(){
    Map<PipelineIdentifier, PipelineNumbersAndSplits> allPipelineNumbersAndSplitsRole = new HashMap<>();
    var pipeline1NumberAndSplit = new PipelineNumbersAndSplits(new PipelineId(PIPELINE_1_ID), PIPELINE_1_NUMBER, null);
    var pipeline2NumberAndSplit = new PipelineNumbersAndSplits(new PipelineId(PIPELINE_2_ID), PIPELINE_2_NUMBER, null);
    var pipeline3Section1NumberAndSplit = new PipelineNumbersAndSplits(pipeline3Section1, PIPELINE_3_NUMBER, PIPELINE_3_SECTION1);
    var pipeline3Section2NumberAndSplit = new PipelineNumbersAndSplits(pipeline3Section2, PIPELINE_3_NUMBER, PIPELINE_3_SECTION2);

    allPipelineNumbersAndSplitsRole.put(pipeline1NumberAndSplit.getPipelineIdentifier(), pipeline1NumberAndSplit);
    allPipelineNumbersAndSplitsRole.put(pipeline2NumberAndSplit.getPipelineIdentifier(), pipeline2NumberAndSplit);
    allPipelineNumbersAndSplitsRole.put(
        pipeline3Section1NumberAndSplit.getPipelineIdentifier(),
        pipeline3Section1NumberAndSplit
    );

    allPipelineNumbersAndSplitsRole.put(
        pipeline3Section2NumberAndSplit.getPipelineIdentifier(),
        pipeline3Section2NumberAndSplit
    );

    return allPipelineNumbersAndSplitsRole;
  }

  @Test
  public void createPipelineAndOrgGroupViewsByRole_containsNotNullViews() {
    var summaryView = padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(
        pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto);

    assertThat(summaryView.getHolderRoleSummaryView()).isNotNull();
    assertThat(summaryView.getUserRoleSummaryView()).isNotNull();
    assertThat(summaryView.getOperatorRoleSummaryView()).isNotNull();
    assertThat(summaryView.getOwnerRoleSummaryView()).isNotNull();
  }

  @Test
  public void createPipelineAndOrgGroupViewsByRole_holderSummaryViewWheOneHolderRole_andUnsassignedPipelines() {

    var holderSummaryView = padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(
        pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto).getHolderRoleSummaryView();

    assertThat(holderSummaryView.getHuooRole()).isEqualTo(HuooRole.HOLDER);
    assertThat(holderSummaryView.getUnassignedPipelineNumberMapForRole()).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
        entry(new PipelineId(PIPELINE_2_ID), PIPELINE_2_NUMBER),
        entry(pipeline3Section1, "PL3 (from and including START to and including MID)"),
        entry(pipeline3Section2, "PL3 (from and including MID to and including END)")
    ));
    assertThat(holderSummaryView.getUnassignedOrganisationRoleOwnerNameMapForRole()).isEmpty();
  }

  @Test
  public void createPipelineAndOrgGroupViewsByRole_holderSummaryViewWhenTwoHoldersButOneHolderHasRole() {

    var holderOrg1Pipeline2Role = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1,
        PIPELINE_2_ID);

    var holderOrg2Role = OrganisationRoleDtoTestUtil.createOrganisationUnitOrgRoleInstance(HuooRole.HOLDER, OU_ID2);

    when(padOrganisationRoleService.getAssignableOrganisationRoleInstanceDtosByRole(any(), any()))
        .thenReturn(Set.of(holderOrg1Pipeline2Role.getOrganisationRoleInstanceDto(), holderOrg2Role));

    pipelineAndOrganisationRoleGroupSummaryDto = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holderOrg1Pipeline1RoleDto, holderOrg1Pipeline2Role));

    var holderSummaryView = padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(
        pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto).getHolderRoleSummaryView();

    assertThat(holderSummaryView.getHuooRole()).isEqualTo(HuooRole.HOLDER);
    assertThat(holderSummaryView.getUnassignedPipelineNumberMapForRole().keySet())
        .containsExactlyInAnyOrder(pipeline3Section1, pipeline3Section2);
    assertThat(holderSummaryView.getUnassignedOrganisationRoleOwnerNameMapForRole()).containsExactly(
        entry(OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(OU_ID2), ou2DetailDto.getCompanyName())
    );
  }

  @Test
  public void createPipelineAndOrgGroupViewsByRole_ownerSummaryViewWhenTwoOwners_andTreatyOwnerHasNoRole_andPipelinesUnassigned() {


    when(padOrganisationRoleService.getAssignableOrganisationRoleInstanceDtosByRole(any(), any()))
        .thenReturn(Set.of(ownerBelgiumPipeline2RoleDto.getOrganisationRoleInstanceDto(), ownerOrg1Pipeline1RoleDto.getOrganisationRoleInstanceDto()));

    pipelineAndOrganisationRoleGroupSummaryDto = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(ownerOrg1Pipeline1RoleDto));

    var ownerSummaryView = padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(
        pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto).getOwnerRoleSummaryView();

    assertThat(ownerSummaryView.getHuooRole()).isEqualTo(HuooRole.OWNER);
    assertThat(ownerSummaryView.getUnassignedPipelineNumberMapForRole()).containsExactlyInAnyOrderEntriesOf(Map.ofEntries(
        entry(new PipelineId(PIPELINE_2_ID), PIPELINE_2_NUMBER),
        entry(pipeline3Section1, "PL3 (from and including START to and including MID)"),
        entry(pipeline3Section2, "PL3 (from and including MID to and including END)")
    ));
    assertThat(ownerSummaryView.getUnassignedOrganisationRoleOwnerNameMapForRole()).containsExactly(
        entry(OrganisationRoleDtoTestUtil.createTreatyRoleOwnerDto(TreatyAgreement.ANY_TREATY_COUNTRY), TreatyAgreement.ANY_TREATY_COUNTRY.getAgreementText())
    );
  }

  @Test
  public void createPipelineAndOrgGroupViewsByRole_constructsViewsAsExpected() {
    var view = padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto);

    //holder, Operator, Owner groups checked as same input
    assertThat(view.getHolderGroups()).hasOnlyOneElementSatisfying(pipelinesAndOrgRoleGroupView -> {
      assertPipelineAndOrgRoleGroupMatchesSingle(
          pipelinesAndOrgRoleGroupView,
          HuooRole.HOLDER,
          new OrganisationUnitId(OU_ID1),
          new PipelineId(PIPELINE_1_ID),
          String.format("%s (%s)", ou1DetailDto.getCompanyName(), ou1DetailDto.getRegisteredNumber()),
          PIPELINE_1_NUMBER
      );
    });

    assertThat(view.getOperatorGroups()).hasOnlyOneElementSatisfying(pipelinesAndOrgRoleGroupView -> {
      assertPipelineAndOrgRoleGroupMatchesSingle(
          pipelinesAndOrgRoleGroupView,
          HuooRole.OPERATOR,
          new OrganisationUnitId(OU_ID1),
          new PipelineId(PIPELINE_1_ID),
          String.format("%s (%s)", ou1DetailDto.getCompanyName(), ou1DetailDto.getRegisteredNumber()),
          PIPELINE_1_NUMBER
      );
    });

    // 2 owners, one treaty, one org unit
    assertThat(view.getOwnerGroups()).hasSize(2);
    assertThat(view.getOwnerGroups()).containsExactlyInAnyOrder(
        new PipelinesAndOrgRoleGroupView(
            Set.of(new PipelineId(PIPELINE_1_ID)),
            Set.of(OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(OU_ID1)),
            List.of(PIPELINE_1_NUMBER),
            List.of(String.format("%s (%s)", ou1DetailDto.getCompanyName(), ou1DetailDto.getRegisteredNumber()))
        ),
        new PipelinesAndOrgRoleGroupView(
            Set.of(new PipelineId(PIPELINE_2_ID)),
            Set.of(OrganisationRoleDtoTestUtil.createTreatyRoleOwnerDto(TreatyAgreement.ANY_TREATY_COUNTRY)),
            List.of(PIPELINE_2_NUMBER),
            List.of(TreatyAgreement.ANY_TREATY_COUNTRY.getAgreementText())
        )
    );

    // User has distinct role
    assertThat(view.getUserGroups()).hasOnlyOneElementSatisfying(pipelinesAndOrgRoleGroupView -> {
      assertPipelineAndOrgRoleGroupMatchesSingle(
          pipelinesAndOrgRoleGroupView,
          HuooRole.USER,
          new OrganisationUnitId(OU_ID2),
          new PipelineId(PIPELINE_2_ID),
          ou2DetailDto.getCompanyName(),
          PIPELINE_2_NUMBER
      );
    });
  }

  @Test
  public void createPipelineAndOrgGroupViewsByRole_constructsViewsAsExpected_whenGivenAssignedSplitPipelines() {

    var holderOu1Section1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineSectionRoleInstance(
        HuooRole.HOLDER,
        OU_ID1,
        pipeline3Section1);
    var holderOu2Section2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineSectionRoleInstance(
        HuooRole.HOLDER,
        OU_ID2,
        pipeline3Section2);

    pipelineAndOrganisationRoleGroupSummaryDto = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holderOu1Section1, holderOu2Section2));

    var view = padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto);

    var holderView = view.getHolderRoleSummaryView();
    assertThat(holderView.getUnassignedPipelineNumberMapForRole()).containsExactly(
        entry(new PipelineId(PIPELINE_1_ID), PIPELINE_1_NUMBER),
        entry(new PipelineId(PIPELINE_2_ID), PIPELINE_2_NUMBER)
    );

    assertPipelineAndOrgRoleGroupMatchesPipelineSectionSingle(holderView.getPipelinesAndOrgRoleGroupViews().get(0),
        ou2DetailDto.getOrganisationUnitId(),
        pipeline3Section2,
        ou2DetailDto.getCompanyName(),
        "PL3 (from and including MID to and including END)"
        );

    assertPipelineAndOrgRoleGroupMatchesPipelineSectionSingle(holderView.getPipelinesAndOrgRoleGroupViews().get(1),
        ou1DetailDto.getOrganisationUnitId(),
        pipeline3Section1,
        String.format("%s (%s)", ou1DetailDto.getCompanyName(), ou1DetailDto.getRegisteredNumber()),
        "PL3 (from and including START to and including MID)"
    );

  }

  private void assertPipelineAndOrgRoleGroupMatchesPipelineSectionSingle(PipelinesAndOrgRoleGroupView testPipelinesAndOrgRoleGroupView,
                                                          OrganisationUnitId organisationUnitId,
                                                          PipelineSection pipelineSection,
                                                          String orgName,
                                                          String pipelineNumber) {
    assertThat(testPipelinesAndOrgRoleGroupView.getOrganisationNames()).containsExactly(orgName);
    assertThat(testPipelinesAndOrgRoleGroupView.getPipelineNumbers()).containsExactly(pipelineNumber);
    assertThat(testPipelinesAndOrgRoleGroupView.getOrganisationRoleOwnerSet()).containsExactly(
        OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(organisationUnitId)
    );
    assertThat(testPipelinesAndOrgRoleGroupView.getOrganisationIdsOfRoleOwners())
        .containsExactly(organisationUnitId);

    assertThat(testPipelinesAndOrgRoleGroupView.getPipelineIdentifierSet()).containsExactly(pipelineSection);
    assertThat(testPipelinesAndOrgRoleGroupView.getTreatyAgreementsOfRoleOwners()).isEmpty();
  }

  private void assertPipelineAndOrgRoleGroupMatchesSingle(PipelinesAndOrgRoleGroupView testPipelinesAndOrgRoleGroupView,
                                                          HuooRole huooRole,
                                                          OrganisationUnitId organisationUnitId,
                                                          PipelineId pipelineId,
                                                          String orgName,
                                                          String pipelineNumber) {
    assertThat(testPipelinesAndOrgRoleGroupView.getOrganisationNames()).containsExactly(orgName);
    assertThat(testPipelinesAndOrgRoleGroupView.getPipelineNumbers()).containsExactly(pipelineNumber);
    assertThat(testPipelinesAndOrgRoleGroupView.getOrganisationRoleOwnerSet()).containsExactly(
        OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(organisationUnitId)
    );
    assertThat(testPipelinesAndOrgRoleGroupView.getPipelineIdentifierSet()).containsExactly(pipelineId);
  }
}