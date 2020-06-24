package uk.co.ogauthority.pwa.model.dto.huooaggregations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
public class PipelineAndOrganisationRoleGroupSummaryDtoTest {

  private static final int OU_ID1 = 10;
  private static final int PIPELINE_ID1 = 100;

  private static final int OU_ID2 = 20;
  private static final int PIPELINE_ID2 = 200;

  private static final int PIPELINE_ID3 = 300;

  private PipelineId pipelineId1;
  private PipelineId pipelineId2;

  private OrganisationPipelineRoleDto holderRole;
  private OrganisationPipelineRoleDto userRole;
  private OrganisationPipelineRoleDto operatorRole;
  private OrganisationPipelineRoleDto ownerRole;

  private PipelineAndOrganisationHuooRoleGroupSummaryDto pipelineAndOrganisationHuooRoleGroupSummaryDto;

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
  public void getGroupsByType_smokeTestRoles_whenSingleGroup() {

    for (HuooRole role : HuooRole.values()) {
      try {
        var pipelineOrgRole = OrganisationRoleDtoTestUtil.createPipelineRole(role, OU_ID1, PIPELINE_ID1);
        var summary = PipelineAndOrganisationHuooRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
            Set.of(pipelineOrgRole)
        );

        assertThat(summary.getGroupsByHuooRole(role)).hasOnlyOneElementSatisfying(
            pipelineAndOrganisationHuooRoleGroupDto -> {
              assertThat(pipelineAndOrganisationHuooRoleGroupDto.getOrganisationRoleDtoSet())
                  .containsExactly(pipelineOrgRole.getOrganisationRoleDto());
              assertThat(pipelineAndOrganisationHuooRoleGroupDto.getPipelineIdSet())
                  .containsExactly(new PipelineId(PIPELINE_ID1));
            });

      } catch (AssertionError e) {
        throw new AssertionError("Failed at HuooRole:" + role + "\n" + e.getMessage(), e);

      }
    }


  }


  @Test
  public void getGroupsByHuooRole_smokeTestRoles_cannotModifyPopulatedSet() {

    for (HuooRole role : HuooRole.values()) {
      try {
        var pipelineOrgRole = OrganisationRoleDtoTestUtil.createPipelineRole(role, OU_ID1, PIPELINE_ID1);
        var summary = PipelineAndOrganisationHuooRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
            Set.of(pipelineOrgRole)
        );

        assertThatThrownBy(() -> summary.getGroupsByHuooRole(role).add(new PipelineAndOrganisationHuooRoleGroupDto(Set.of(), Set.of())))
            .isInstanceOf(UnsupportedOperationException.class);

      } catch (AssertionError e) {
        throw new AssertionError("Failed at HuooRole:" + role + "\n" + e.getMessage(), e);

      }
    }

  }

  @Test
  public void getGroupsByHuooRole_smokeTestRoles_cannotModifyEmptySet() {

    for (HuooRole role : HuooRole.values()) {
      try {
        var summary = PipelineAndOrganisationHuooRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
            Set.of()
        );

        assertThatThrownBy(() -> summary.getGroupsByHuooRole(role).add(new PipelineAndOrganisationHuooRoleGroupDto(Set.of(), Set.of())))
            .isInstanceOf(UnsupportedOperationException.class);

      } catch (AssertionError e) {
        throw new AssertionError("Failed at HuooRole:" + role + "\n" + e.getMessage(), e);

      }
    }

  }


  /**
   * WHEN there is a single pipeline and a single organisation role
   * THEN there is a single group with 1 pipeline and 1 org role
   */
  @Test
  public void aggregateOrganisationPipelineRoleDtos_singleHolderAndPipelineGroup_withSingleOrgAndPipeline() {

    var summary = PipelineAndOrganisationHuooRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holderRole));
    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).hasOnlyOneElementSatisfying(
        pipelineAndOrganisationHuooRoleGroupDto -> {
          assertThat(pipelineAndOrganisationHuooRoleGroupDto.getOrganisationRoleDtoSet())
              .containsExactly(holderRole.getOrganisationRoleDto());
          assertThat(pipelineAndOrganisationHuooRoleGroupDto.getPipelineIdSet())
              .containsExactly(new PipelineId(PIPELINE_ID1));
        });
  }

  /**
   * WHEN there are 2 pipelines
   * AND 2 organisation share a role on both pipelines
   * THEN there is 1 group with both pipelines and both organisation roles
   */
  @Test
  public void aggregateOrganisationPipelineRoleDtos_singleHolderAndPipelineGroup_withMultipleOrgAndMultiplePipelines() {

    var holder1Pipeline1 = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    var holder1Pipeline2 = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID1, PIPELINE_ID2);
    var holder2Pipeline1 = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID2, PIPELINE_ID1);
    var holder2Pipeline2 = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID2, PIPELINE_ID2);

    var summary = PipelineAndOrganisationHuooRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holder1Pipeline1,
            holder1Pipeline2,
            holder2Pipeline1,
            holder2Pipeline2)

    );

    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).hasOnlyOneElementSatisfying(
        pipelineAndOrganisationHuooRoleGroupDto -> {
          assertThat(pipelineAndOrganisationHuooRoleGroupDto.getOrganisationRoleDtoSet())
              .containsExactlyInAnyOrder(holder1Pipeline1.getOrganisationRoleDto(),
                  holder2Pipeline1.getOrganisationRoleDto());
          assertThat(pipelineAndOrganisationHuooRoleGroupDto.getPipelineIdSet())
              .containsExactlyInAnyOrder(new PipelineId(PIPELINE_ID1), new PipelineId(PIPELINE_ID2));
        });
  }

  /**
   * WHEN there are 3 pipelines
   * AND 2 organisations share a role on 1 of the pipelines (pipeline 2)
   * AND each organisation has the sole role on the other pipelines  (pipeline 1 and 3)
   * THEN there a 3 groups, each with 1 pipeline, where the pipeline 2 group has both orgs
   */
  @Test
  public void aggregateOrganisationPipelineRoleDtos_multipleHolderAndPipelineGroups_withSharedPipelineInGroup() {

    var holder1Pipeline1 = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    var holder1Pipeline2 = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID1, PIPELINE_ID2);

    var holder2Pipeline2 = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID2, PIPELINE_ID2);
    var holder2Pipeline3 = OrganisationRoleDtoTestUtil.createPipelineRole(HuooRole.HOLDER, OU_ID2, PIPELINE_ID3);

    var summary = PipelineAndOrganisationHuooRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holder1Pipeline1,
            holder1Pipeline2,
            holder2Pipeline2,
            holder2Pipeline3)

    );

    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).hasSize(3);
    // group containing holder 1 and pipeline 1
    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).anySatisfy(
        pipelineAndOrganisationHuooRoleGroupDto -> {
          assertThat(pipelineAndOrganisationHuooRoleGroupDto.getOrganisationRoleDtoSet())
              .containsExactlyInAnyOrder(holder1Pipeline1.getOrganisationRoleDto());
          assertThat(pipelineAndOrganisationHuooRoleGroupDto.getPipelineIdSet())
              .containsExactlyInAnyOrder(new PipelineId(PIPELINE_ID1));
        });

    // group containing holder 1 AND holder 2 and pipeline 2
    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).anySatisfy(
        pipelineAndOrganisationHuooRoleGroupDto -> {
          assertThat(pipelineAndOrganisationHuooRoleGroupDto.getOrganisationRoleDtoSet())
              .containsExactlyInAnyOrder(holder1Pipeline1.getOrganisationRoleDto(),
                  holder2Pipeline2.getOrganisationRoleDto());
          assertThat(pipelineAndOrganisationHuooRoleGroupDto.getPipelineIdSet())
              .containsExactlyInAnyOrder(new PipelineId(PIPELINE_ID2));
        });

    // group containing holder 2 and pipeline 3
    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).anySatisfy(
        pipelineAndOrganisationHuooRoleGroupDto -> {
          assertThat(pipelineAndOrganisationHuooRoleGroupDto.getOrganisationRoleDtoSet())
              .containsExactlyInAnyOrder(holder2Pipeline2.getOrganisationRoleDto());
          assertThat(pipelineAndOrganisationHuooRoleGroupDto.getPipelineIdSet())
              .containsExactlyInAnyOrder(new PipelineId(PIPELINE_ID3));
        });
  }
}