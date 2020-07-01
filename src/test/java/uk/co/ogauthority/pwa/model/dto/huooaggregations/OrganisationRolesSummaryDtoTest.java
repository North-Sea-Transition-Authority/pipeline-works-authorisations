package uk.co.ogauthority.pwa.model.dto.huooaggregations;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationRolesSummaryDtoTest {

  private static final int OU_ID1 = 10;
  private static final int PIPELINE_ID1 = 20;

  private static final int OU_ID2 = 100;
  private static final int PIPELINE_ID2 = 200;

  private PipelineId pipelineId1;
  private PipelineId pipelineId2;

  private OrganisationPipelineRoleInstanceDto holderOrg1Pipeline1Role;
  private OrganisationPipelineRoleInstanceDto userOrg1Pipeline1Role;
  private OrganisationPipelineRoleInstanceDto operatorOrg1Pipeline1Role;
  private OrganisationPipelineRoleInstanceDto ownerOrg1Pipeline1Role;

  @Before
  public void setup() {
    holderOrg1Pipeline1Role = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    userOrg1Pipeline1Role = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.USER, OU_ID1, PIPELINE_ID1);
    operatorOrg1Pipeline1Role = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.OPERATOR, OU_ID1, PIPELINE_ID1);
    ownerOrg1Pipeline1Role = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.OWNER, OU_ID1, PIPELINE_ID1);

    pipelineId1 = new PipelineId(PIPELINE_ID1);
    pipelineId2 = new PipelineId(PIPELINE_ID2);
  }


  @Test
  public void getHolderOrganisationUnitGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role, userOrg1Pipeline1Role, operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role));
    assertThat(summary.getHolderOrganisationUnitGroups())
        .containsExactly(
            new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of(pipelineId1)));

  }


  @Test(expected = UnsupportedOperationException.class)
  public void getHolderOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role));
    assertThat(summary.getHolderOrganisationUnitGroups()).hasSize(1);
    summary.getHolderOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(operatorOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getHolderOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getHolderOrganisationUnitGroups()).hasSize(0);
    summary.getHolderOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of()));
  }

  @Test
  public void getUserOrganisationUnitGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role, userOrg1Pipeline1Role, operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role));
    assertThat(summary.getUserOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(userOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of(pipelineId1)));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUserOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(userOrg1Pipeline1Role));
    assertThat(summary.getUserOrganisationUnitGroups()).hasSize(1);
    summary.getUserOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUserOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getUserOrganisationUnitGroups()).hasSize(0);
    summary.getUserOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(userOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of()));
  }

  @Test
  public void getOperatorOrganisationUnitGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role, userOrg1Pipeline1Role, operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role));
    assertThat(summary.getOperatorOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(operatorOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of(
            pipelineId1)));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOperatorOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(operatorOrg1Pipeline1Role));
    assertThat(summary.getOperatorOrganisationUnitGroups()).hasSize(1);
    summary.getOperatorOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOperatorOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getOperatorOrganisationUnitGroups()).hasSize(0);
    summary.getOperatorOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(operatorOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of()));
  }

  @Test
  public void getOwnerOrganisationGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role, userOrg1Pipeline1Role, operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role));
    assertThat(summary.getOwnerOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(ownerOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of(pipelineId1)));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOwnerOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(ownerOrg1Pipeline1Role));
    assertThat(summary.getOwnerOrganisationUnitGroups()).hasSize(1);
    summary.getOwnerOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOwnerOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getOwnerOrganisationUnitGroups()).hasSize(0);
    summary.getOwnerOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(ownerOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of()));
  }


  @Test
  public void getHolderOrganisationUnitGroups_whenMultipleGroups() {
    var secondOrganisationPipelineRole = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID2, PIPELINE_ID2);

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(secondOrganisationPipelineRole, holderOrg1Pipeline1Role, userOrg1Pipeline1Role,
            operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role)
    );
    assertThat(summary.getHolderOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleDto(), Set.of(pipelineId2)
            )
        );

  }

  @Test
  public void getUserOrganisationUnitGroups_whenMultipleGroups() {
    var secondOrganisationPipelineRole = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.USER, OU_ID2, PIPELINE_ID2);

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(secondOrganisationPipelineRole, holderOrg1Pipeline1Role, userOrg1Pipeline1Role,
            operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role)
    );

    assertThat(summary.getUserOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(userOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleDto(), Set.of(pipelineId2)
            )

        );

  }

  @Test
  public void getOperatorOrganisationUnitGroups_whenMultipleGroups() {
    var secondOrganisationPipelineRole = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.OPERATOR, OU_ID2, PIPELINE_ID2);

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(secondOrganisationPipelineRole, holderOrg1Pipeline1Role, userOrg1Pipeline1Role,
            operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role)
    );

    assertThat(summary.getOperatorOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(operatorOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleDto(), Set.of(pipelineId2)
            )

        );

  }

  @Test
  public void getOwnerOrganisationUnitGroups_whenMultipleGroups() {
    var secondOrganisationPipelineRole = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.OWNER, OU_ID2, PIPELINE_ID2);

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(secondOrganisationPipelineRole, holderOrg1Pipeline1Role, userOrg1Pipeline1Role,
            operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role)
    );

    assertThat(summary.getOwnerOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(ownerOrg1Pipeline1Role.getOrganisationRoleDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleDto(), Set.of(pipelineId2)
            )

        );

  }


}