package uk.co.ogauthority.pwa.model.dto.consents;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;

@RunWith(MockitoJUnitRunner.class)
public class PwaOrganisationRolesSummaryDtoTest {

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
    holderRole = createPipelineRole(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    userRole = createPipelineRole(HuooRole.USER, OU_ID1, PIPELINE_ID1);
    operatorRole = createPipelineRole(HuooRole.OPERATOR, OU_ID1, PIPELINE_ID1);
    ownerRole = createPipelineRole(HuooRole.OWNER, OU_ID1, PIPELINE_ID1);

    pipelineId1 = new PipelineId(PIPELINE_ID1);
    pipelineId2 = new PipelineId(PIPELINE_ID2);
  }

  private OrganisationPipelineRoleDto createPipelineRole(HuooRole huooRole, int ouId, int pipelineId) {
    return new OrganisationPipelineRoleDto(
        ouId,
        null,
        huooRole,
        HuooType.PORTAL_ORG,
        pipelineId);
  }


  @Test
  public void getHolderOrganisationUnitGroups_whenSingleGroup() {

    var summary = PwaOrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderRole, userRole, operatorRole, ownerRole));
    assertThat(summary.getHolderOrganisationUnitGroups())
        .containsExactly(
            new OrganisationRolePipelineGroupDto(holderRole.getOrganisationRoleDto(), Set.of(pipelineId1)));

  }

  @Test
  public void getUserOrganisationUnitGroups_whenSingleGroup() {

    var summary = PwaOrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderRole, userRole, operatorRole, ownerRole));
    assertThat(summary.getUserOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(userRole.getOrganisationRoleDto(), Set.of(pipelineId1)));

  }

  @Test
  public void getOperatorOrganisationUnitGroups_whenSingleGroup() {

    var summary = PwaOrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderRole, userRole, operatorRole, ownerRole));
    assertThat(summary.getOperatorOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(operatorRole.getOrganisationRoleDto(), Set.of(
            pipelineId1)));

  }

  @Test
  public void getOwnerOrganisationGroups_whenSingleGroup() {

    var summary = PwaOrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderRole, userRole, operatorRole, ownerRole));
    assertThat(summary.getOwnerOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(ownerRole.getOrganisationRoleDto(), Set.of(pipelineId1)));

  }


  @Test
  public void getHolderOrganisationUnitGroups_whenMultipleGroups() {
    var secondOrganisationPipelineRole = createPipelineRole(HuooRole.HOLDER, OU_ID2, PIPELINE_ID2);

    var summary = PwaOrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
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
    var secondOrganisationPipelineRole = createPipelineRole(HuooRole.USER, OU_ID2, PIPELINE_ID2);

    var summary = PwaOrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
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
    var secondOrganisationPipelineRole = createPipelineRole(HuooRole.OPERATOR, OU_ID2, PIPELINE_ID2);

    var summary = PwaOrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
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
    var secondOrganisationPipelineRole = createPipelineRole(HuooRole.OWNER, OU_ID2, PIPELINE_ID2);

    var summary = PwaOrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
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

  @Test
  public void getAllOrganisationUnitsIdsWithRole() {
  }


}