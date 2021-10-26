package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.AllOrgRolePipelineGroupsView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.OrganisationRolePipelineGroupView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.PipelineNumbersAndSplits;

@RunWith(MockitoJUnitRunner.class)
public class AllOrgRolePipelineGroupsViewTest {

  private static int ORG_ID_1 = 1;
  private static int ORG_ID_2 = 2;
  private static int PIPELINE_ID_1 = 1;
  private static int PIPELINE_ID_2 = 2;




  private OrganisationRolePipelineGroupView createOrgRolePipelineGroupView(int orgId, int pipelineId) {
    var portalOrgUnitDetail = PortalOrganisationTestUtils.generateOrganisationUnitDetail(
        PortalOrganisationTestUtils.generateOrganisationUnit(orgId, "company" + orgId), "address" + orgId, "11" + orgId);
    var organisationUnitDetail = OrganisationUnitDetailDto.from(portalOrgUnitDetail);
    var organisationRoleOwnerDto = OrganisationRoleOwnerDto.fromOrganisationUnitId(new OrganisationUnitId(1));
    var pipelineNumbersAndSplits = List.of(new PipelineNumbersAndSplits(new PipelineId(pipelineId), "ppl" + pipelineId, null));
    return new OrganisationRolePipelineGroupView(
        HuooType.PORTAL_ORG, organisationUnitDetail, false, null, null, organisationRoleOwnerDto, pipelineNumbersAndSplits);

  }


  @Test
  public void hasOnlyOneGroupOfPipelineIdentifiersForRole_when2OrgRoleOwners_andDifferentPipelineGroups() {

    var operatorPipelineGroupView1 = createOrgRolePipelineGroupView(ORG_ID_1, PIPELINE_ID_1);
    var operatorPipelineGroupView2 = createOrgRolePipelineGroupView(ORG_ID_2, PIPELINE_ID_2);
    var operatorOrgRolePipelineGroups = List.of(operatorPipelineGroupView1, operatorPipelineGroupView2);

    AllOrgRolePipelineGroupsView allOrgRolePipelineGroupsView = new AllOrgRolePipelineGroupsView(
      List.of(), List.of(), operatorOrgRolePipelineGroups, List.of());

    var hasOnlyOneGroupOfPipelineIdentifiersForRole = allOrgRolePipelineGroupsView.hasOnlyOneGroupOfPipelineIdentifiersForRole(
        HuooRole.OPERATOR);
    assertThat(hasOnlyOneGroupOfPipelineIdentifiersForRole).isFalse();
  }


  @Test
  public void hasOnlyOneGroupOfPipelineIdentifiersForRole_when2OrgRoleOwners_andSamePipelineGroups() {

    var operatorPipelineGroupView1 = createOrgRolePipelineGroupView(ORG_ID_1, PIPELINE_ID_1);
    var operatorPipelineGroupView2 = createOrgRolePipelineGroupView(ORG_ID_1, PIPELINE_ID_1);
    var operatorOrgRolePipelineGroups = List.of(operatorPipelineGroupView1, operatorPipelineGroupView2);

    AllOrgRolePipelineGroupsView allOrgRolePipelineGroupsView = new AllOrgRolePipelineGroupsView(
        List.of(), List.of(), operatorOrgRolePipelineGroups, List.of());

    var hasOnlyOneGroupOfPipelineIdentifiersForRole = allOrgRolePipelineGroupsView.hasOnlyOneGroupOfPipelineIdentifiersForRole(
        HuooRole.OPERATOR);
    assertThat(hasOnlyOneGroupOfPipelineIdentifiersForRole).isTrue();
  }



}