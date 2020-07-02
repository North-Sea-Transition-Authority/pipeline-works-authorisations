package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.PipelineAndOrganisationRoleGroupSummaryDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationsDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.huoo.PadOrganisationRoleService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PadPipelineHuooViewFactoryTest {

  private static final int OU_ID1 = 10;
  private static final int PIPELINE_1_ID = 100;
  private static final String PIPELINE_1_NUMBER = "PL1";
  private static final int OU_ID2 = 20;
  private static final int PIPELINE_2_ID = 200;
  private static final String PIPELINE_2_NUMBER = "PL2";

  private OrganisationPipelineRoleInstanceDto holderOrg1Pipeline1RoleDto;
  private OrganisationPipelineRoleInstanceDto userOrg2Pipeline2RoleDto;
  private OrganisationPipelineRoleInstanceDto operatorOrg1Pipeline1RoleDto;
  private OrganisationPipelineRoleInstanceDto ownerOrg1Pipeline1RoleDto;

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

    var orgRoleDtos = Set.of(
        holderOrg1Pipeline1RoleDto.getOrganisationRoleInstanceDto(),
        userOrg2Pipeline2RoleDto.getOrganisationRoleInstanceDto(),
        operatorOrg1Pipeline1RoleDto.getOrganisationRoleInstanceDto(),
        ownerOrg1Pipeline1RoleDto.getOrganisationRoleInstanceDto()
    );
    when(padOrganisationRoleService.getOrganisationRoleDtos(any()))
        .thenReturn(orgRoleDtos);

    when(portalOrganisationsAccessor.getOrganisationUnitDetailDtosByOrganisationUnitId(any()))
        .thenReturn(List.of(ou1DetailDto, ou2DetailDto));

    var pipelineMap = new HashMap<PipelineId, String>();
    pipelineMap.put(new PipelineId(PIPELINE_1_ID), PIPELINE_1_NUMBER);
    pipelineMap.put(new PipelineId(PIPELINE_2_ID), PIPELINE_2_NUMBER);

    when(padPipelineService.getApplicationOrConsentedPipelineNumberLookup(any()))
        .thenReturn(pipelineMap);

    pipelineAndOrganisationRoleGroupSummaryDto = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holderOrg1Pipeline1RoleDto, userOrg2Pipeline2RoleDto, operatorOrg1Pipeline1RoleDto,
            ownerOrg1Pipeline1RoleDto));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

        padPipelineHuooViewFactory = new PadPipelineHuooViewFactory(
            portalOrganisationsAccessor,
            padPipelineService,
            padOrganisationRoleService);

  }

  @Test
  public void createPipelineAndOrgGroupViewsByRole_containsNotNullViews() {
    var summaryView = padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(
        pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto);

    assertThat(summaryView.getHolderRoleSummaryView()).isNotNull();
    assertThat(summaryView.getUserRoleSumaryView()).isNotNull();
    assertThat(summaryView.getOperatorRoleSummaryView()).isNotNull();
    assertThat(summaryView.getOwnerRoleSummaryView()).isNotNull();
  }


  @Test
  public void createPipelineAndOrgGroupViewsByRole_holderSummaryViewWhenTwoPipelinesButOneHolderRole() {
    var holderSummaryView = padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(
        pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto).getHolderRoleSummaryView();

    assertThat(holderSummaryView.getHuooRole()).isEqualTo(HuooRole.HOLDER);
    assertThat(holderSummaryView.getUnassignedPipelineNumberMapForRole()).containsExactly(
        entry(new PipelineId(PIPELINE_2_ID), PIPELINE_2_NUMBER)
    );
    assertThat(holderSummaryView.getUnassignedOrganisationNameMapForRole()).isEmpty();
  }

  @Test
  public void createPipelineAndOrgGroupViewsByRole_holderSummaryViewWhenTwoHoldersButOneHolderHasRole() {
    var holderOrg1Pipeline2Role = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1,
        PIPELINE_2_ID);

    var holderOrg2Role = OrganisationRoleDtoTestUtil.createOrganisationUnitOrgRoleInstance(HuooRole.HOLDER, OU_ID2);

    when(padOrganisationRoleService.getOrganisationRoleDtosByRole(any(), any(), any()))
        .thenReturn(Set.of(holderOrg1Pipeline2Role.getOrganisationRoleInstanceDto(), holderOrg2Role));

    pipelineAndOrganisationRoleGroupSummaryDto = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holderOrg1Pipeline1RoleDto, holderOrg1Pipeline2Role));

    var holderSummaryView = padPipelineHuooViewFactory.createPipelineAndOrgGroupViewsByRole(
        pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto).getHolderRoleSummaryView();

    assertThat(holderSummaryView.getHuooRole()).isEqualTo(HuooRole.HOLDER);
    assertThat(holderSummaryView.getUnassignedPipelineNumberMapForRole()).isEmpty();
    assertThat(holderSummaryView.getUnassignedOrganisationNameMapForRole()).containsExactly(
        entry(new OrganisationUnitId(OU_ID2), ou2DetailDto.getCompanyName())
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
    assertThat(view.getOwnerGroups()).hasOnlyOneElementSatisfying(pipelinesAndOrgRoleGroupView -> {
      assertPipelineAndOrgRoleGroupMatchesSingle(
          pipelinesAndOrgRoleGroupView,
          HuooRole.OWNER,
          new OrganisationUnitId(OU_ID1),
          new PipelineId(PIPELINE_1_ID),
          String.format("%s (%s)", ou1DetailDto.getCompanyName(), ou1DetailDto.getRegisteredNumber()),
          PIPELINE_1_NUMBER
      );
    });

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

  private void assertPipelineAndOrgRoleGroupMatchesSingle(PipelinesAndOrgRoleGroupView testPipelinesAndOrgRoleGroupView,
                                                          HuooRole huooRole,
                                                          OrganisationUnitId organisationUnitId,
                                                          PipelineId pipelineId,
                                                          String orgName,
                                                          String pipelineNumber) {
    assertThat(testPipelinesAndOrgRoleGroupView.getOrganisationNames()).containsExactly(orgName);
    assertThat(testPipelinesAndOrgRoleGroupView.getPipelineNumbers()).containsExactly(pipelineNumber);
    assertThat(testPipelinesAndOrgRoleGroupView.getOrganisationUnitIdSet()).containsExactly(organisationUnitId);
    assertThat(testPipelinesAndOrgRoleGroupView.getPipelineIdSet()).containsExactly(pipelineId);
  }
}