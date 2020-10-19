package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

@RunWith(MockitoJUnitRunner.class)
public class PipelineHuooRoleSummaryViewTest {


  private PipelinesAndOrgRoleGroupView group1;
  private PipelinesAndOrgRoleGroupView group2;

  private OrganisationRoleOwnerDto orgUnitRoleOwner;
  private OrganisationRoleOwnerDto orgTreatyRoleOwner;

  @Before
  public void setup() {

    orgUnitRoleOwner = OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(1);
    orgTreatyRoleOwner = OrganisationRoleDtoTestUtil.createTreatyRoleOwnerDto(TreatyAgreement.ANY_TREATY_COUNTRY);

    group1 = new PipelinesAndOrgRoleGroupView(
        Set.of(new PipelineId(4)),
        Set.of(orgUnitRoleOwner, orgTreatyRoleOwner),
        List.of("PL1"),
        List.of("Org1", "Org2")
    );

    group2 = new PipelinesAndOrgRoleGroupView(
        Set.of(new PipelineId(1), new PipelineId(2)),
        Set.of(orgUnitRoleOwner),
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
}