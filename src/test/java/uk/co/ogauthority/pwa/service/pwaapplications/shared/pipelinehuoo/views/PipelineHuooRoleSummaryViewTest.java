package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

@RunWith(MockitoJUnitRunner.class)
public class PipelineHuooRoleSummaryViewTest {


  private PipelinesAndOrgRoleGroupView group1;
  private PipelinesAndOrgRoleGroupView group2;

  private OrganisationRoleOwnerDto orgUnitRoleOwner1;
  private OrganisationRoleOwnerDto orgUnitRoleOwner2;
  private OrganisationRoleOwnerDto orgTreatyRoleOwner;

  @Before
  public void setup() {

    orgUnitRoleOwner1 = OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(1);
    orgUnitRoleOwner2 = OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(2);
    orgTreatyRoleOwner = OrganisationRoleDtoTestUtil.createTreatyRoleOwnerDto(TreatyAgreement.ANY_TREATY_COUNTRY);

    group1 = new PipelinesAndOrgRoleGroupView(
        Set.of(new PipelineId(4)),
        Set.of(orgUnitRoleOwner1, orgTreatyRoleOwner),
        List.of("PL1"),
        List.of("Org1", "Org2")
    );

    group2 = new PipelinesAndOrgRoleGroupView(
        Set.of(new PipelineId(1), new PipelineId(2)),
        Set.of(orgUnitRoleOwner1),
        List.of("PL3", "PL2"),
        List.of("Org1")
    );


  }

  @Test
  public void pipelineHuooRoleSummaryView_sortsGroupsOnConstructionByPipelineNumbersInGroup() {
    var view = new PipelineHuooRoleSummaryView(
        HuooRole.HOLDER,
        // groups given to view out of desired order
        List.of(group2, group1),
        Collections.emptyMap(),
        Collections.emptyMap()
    );
    // requires ordering to be checked
    assertThat(view.getPipelinesAndOrgRoleGroupViews()).containsExactly(
        group1,
        group2
    );
  }


  @Test
  public void getTotalOrganisationRoleOwners_combinesUnassignedAndAssignedRoles() {
    var view = new PipelineHuooRoleSummaryView(
        HuooRole.HOLDER,
        // groups given to view out of desired order
        List.of(group2, group1),
        Collections.emptyMap(),
        Map.of(orgUnitRoleOwner2, "org2")
    );

    assertThat(view.getTotalOrganisationRoleOwners()).isEqualTo(3);

  }

  @Test
  public void getTotalOrganisationRoleOwners_whenNoRoles() {
    var view = new PipelineHuooRoleSummaryView(
        HuooRole.HOLDER,
        // groups given to view out of desired order
        List.of(),
        Collections.emptyMap(),
        Collections.emptyMap()
    );

    assertThat(view.getTotalOrganisationRoleOwners()).isEqualTo(0);

  }
}