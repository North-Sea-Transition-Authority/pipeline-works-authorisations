package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.service.organisations.PortalOrganisationsAccessor;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleDto;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.huooaggregations.PipelineAndOrganisationRoleGroupSummaryDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationsDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PipelineAndOrgRoleGroupViewFactoryTest {

  private static final int OU_ID1 = 10;
  private static final int PIPELINE_1_ID = 100;
  private static final String PIPELINE_1_NUMBER = "PL1";
  private static final int OU_ID2 = 20;
  private static final int PIPELINE_2_ID = 200;
  private static final String PIPELINE_2_NUMBER = "PL2";

  private OrganisationPipelineRoleDto holderRole;
  private OrganisationPipelineRoleDto userRole;
  private OrganisationPipelineRoleDto operatorRole;
  private OrganisationPipelineRoleDto ownerRole;

  private OrganisationUnitDetailDto ou1DetailDto = OrganisationsDtoTestUtil.createDetailDto(OU_ID1, "OU_1", "12345");
  private OrganisationUnitDetailDto ou2DetailDto = OrganisationsDtoTestUtil.createDetailDto(OU_ID2, "OU_2", null);

  @Mock
  private PortalOrganisationsAccessor portalOrganisationsAccessor;

  @Mock
  private PadPipelineService padPipelineService;

  private PipelineAndOrgRoleGroupViewFactory pipelineAndOrgRoleGroupViewFactory;

  private PwaApplicationDetail pwaApplicationDetail;

  private PipelineAndOrganisationRoleGroupSummaryDto pipelineAndOrganisationRoleGroupSummaryDto;

  @Before
  public void setup() {

    holderRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID1, PIPELINE_1_ID);
    userRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.USER, OU_ID2, PIPELINE_2_ID);
    operatorRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.OPERATOR, OU_ID1, PIPELINE_1_ID);
    ownerRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.OWNER, OU_ID1, PIPELINE_1_ID);

    when(portalOrganisationsAccessor.getOrganisationUnitDetailDtosByOrganisationUnitId(any()))
        .thenReturn(List.of(ou1DetailDto, ou2DetailDto));
    var pipelineMap = new HashMap<PipelineId, String>();
    pipelineMap.put(new PipelineId(PIPELINE_1_ID), PIPELINE_1_NUMBER);
    pipelineMap.put(new PipelineId(PIPELINE_2_ID), PIPELINE_2_NUMBER);

    when(padPipelineService.getApplicationOrConsentedPipelineNumberLookup(any(), any()))
        .thenReturn(pipelineMap);

    pipelineAndOrganisationRoleGroupSummaryDto = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holderRole, userRole, operatorRole, ownerRole));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    pipelineAndOrgRoleGroupViewFactory = new PipelineAndOrgRoleGroupViewFactory(
        portalOrganisationsAccessor,
        padPipelineService
    );

  }

  @Test
  public void createPipelineAndOrgsGroupsByRoleView_serviceInteractions_dataRetrievedForAllPipelinesAndOrgsInSummary() {
    pipelineAndOrgRoleGroupViewFactory.createPipelineAndOrgsGroupsByRoleView(pwaApplicationDetail,
        pipelineAndOrganisationRoleGroupSummaryDto);
    verify(padPipelineService, times(1)).getApplicationOrConsentedPipelineNumberLookup(
        pwaApplicationDetail,
        Set.of(new PipelineId(PIPELINE_1_ID), new PipelineId(PIPELINE_2_ID)));

    verify(portalOrganisationsAccessor, times(1)).getOrganisationUnitDetailDtosByOrganisationUnitId(
        Set.of(new OrganisationUnitId(OU_ID1), new OrganisationUnitId(OU_ID2)));

  }

  @Test
  public void createPipelineAndOrgsGroupsByRoleView_constructsViewsAsExpected() {
    var view = pipelineAndOrgRoleGroupViewFactory.createPipelineAndOrgsGroupsByRoleView(pwaApplicationDetail,
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
    assertThat(testPipelinesAndOrgRoleGroupView.getHuooRole()).isEqualTo(huooRole);
    assertThat(testPipelinesAndOrgRoleGroupView.getOrganisationNames()).containsExactly(orgName);
    assertThat(testPipelinesAndOrgRoleGroupView.getPipelineNumbers()).containsExactly(pipelineNumber);
    assertThat(testPipelinesAndOrgRoleGroupView.getOrganisationUnitIdSet()).containsExactly(organisationUnitId);
    assertThat(testPipelinesAndOrgRoleGroupView.getPipelineIdSet()).containsExactly(pipelineId);
  }
}