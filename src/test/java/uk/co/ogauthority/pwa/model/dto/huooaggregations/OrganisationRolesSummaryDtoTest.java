package uk.co.ogauthority.pwa.model.dto.huooaggregations;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleDto;
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

  private OrganisationPipelineRoleDto holderRole;
  private OrganisationPipelineRoleDto userRole;
  private OrganisationPipelineRoleDto operatorRole;
  private OrganisationPipelineRoleDto ownerRole;

  @Before
  public void setup() {
    holderRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    userRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.USER, OU_ID1, PIPELINE_ID1);
    operatorRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.OPERATOR, OU_ID1, PIPELINE_ID1);
    ownerRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.OWNER, OU_ID1, PIPELINE_ID1);

    pipelineId1 = new PipelineId(PIPELINE_ID1);
    pipelineId2 = new PipelineId(PIPELINE_ID2);
  }


  @Test
  public void getHolderOrganisationUnitGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderRole, userRole, operatorRole, ownerRole));
    assertThat(summary.getHolderOrganisationUnitGroups())
        .containsExactly(
            new OrganisationRolePipelineGroupDto(holderRole.getOrganisationRoleDto(), Set.of(pipelineId1)));

  }


  @Test(expected = UnsupportedOperationException.class)
  public void getHolderOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderRole));
    assertThat(summary.getHolderOrganisationUnitGroups()).hasSize(1);
    summary.getHolderOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(operatorRole.getOrganisationRoleDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getHolderOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getHolderOrganisationUnitGroups()).hasSize(0);
    summary.getHolderOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(holderRole.getOrganisationRoleDto(), Set.of()));
  }

  @Test
  public void getUserOrganisationUnitGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderRole, userRole, operatorRole, ownerRole));
    assertThat(summary.getUserOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(userRole.getOrganisationRoleDto(), Set.of(pipelineId1)));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUserOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(userRole));
    assertThat(summary.getUserOrganisationUnitGroups()).hasSize(1);
    summary.getUserOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(holderRole.getOrganisationRoleDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUserOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getUserOrganisationUnitGroups()).hasSize(0);
    summary.getUserOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(userRole.getOrganisationRoleDto(), Set.of()));
  }

  @Test
  public void getOperatorOrganisationUnitGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderRole, userRole, operatorRole, ownerRole));
    assertThat(summary.getOperatorOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(operatorRole.getOrganisationRoleDto(), Set.of(
            pipelineId1)));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOperatorOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(operatorRole));
    assertThat(summary.getOperatorOrganisationUnitGroups()).hasSize(1);
    summary.getOperatorOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(holderRole.getOrganisationRoleDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOperatorOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getOperatorOrganisationUnitGroups()).hasSize(0);
    summary.getOperatorOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(operatorRole.getOrganisationRoleDto(), Set.of()));
  }

  @Test
  public void getOwnerOrganisationGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderRole, userRole, operatorRole, ownerRole));
    assertThat(summary.getOwnerOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(ownerRole.getOrganisationRoleDto(), Set.of(pipelineId1)));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOwnerOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(ownerRole));
    assertThat(summary.getOwnerOrganisationUnitGroups()).hasSize(1);
    summary.getOwnerOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(holderRole.getOrganisationRoleDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOwnerOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getOwnerOrganisationUnitGroups()).hasSize(0);
    summary.getOwnerOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(ownerRole.getOrganisationRoleDto(), Set.of()));
  }


  @Test
  public void getHolderOrganisationUnitGroups_whenMultipleGroups() {
    var secondOrganisationPipelineRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID2, PIPELINE_ID2);

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(secondOrganisationPipelineRole, holderRole, userRole, operatorRole, ownerRole)
    );
    assertThat(summary.getHolderOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(holderRole.getOrganisationRoleDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleDto(), Set.of(pipelineId2)
            )
        );

  }

  @Test
  public void getUserOrganisationUnitGroups_whenMultipleGroups() {
    var secondOrganisationPipelineRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.USER, OU_ID2, PIPELINE_ID2);

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(secondOrganisationPipelineRole, holderRole, userRole, operatorRole, ownerRole)
    );

    assertThat(summary.getUserOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(userRole.getOrganisationRoleDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleDto(), Set.of(pipelineId2)
            )

        );

  }

  @Test
  public void getOperatorOrganisationUnitGroups_whenMultipleGroups() {
    var secondOrganisationPipelineRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.OPERATOR, OU_ID2, PIPELINE_ID2);

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(secondOrganisationPipelineRole, holderRole, userRole, operatorRole, ownerRole)
    );

    assertThat(summary.getOperatorOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(operatorRole.getOrganisationRoleDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleDto(), Set.of(pipelineId2)
            )

        );

  }

  @Test
  public void getOwnerOrganisationUnitGroups_whenMultipleGroups() {
    var secondOrganisationPipelineRole = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.OWNER, OU_ID2, PIPELINE_ID2);

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(secondOrganisationPipelineRole, holderRole, userRole, operatorRole, ownerRole)
    );

    assertThat(summary.getOwnerOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(ownerRole.getOrganisationRoleDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleDto(), Set.of(pipelineId2)
            )

        );

  }


}