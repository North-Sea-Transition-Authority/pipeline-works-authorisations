package uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentPoint;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;

@RunWith(MockitoJUnitRunner.class)
public class PipelineAndOrganisationRoleGroupSummaryDtoTest {

  private static final int OU_ID1 = 10;
  private static final int PIPELINE_ID1 = 100;

  private static final int OU_ID2 = 20;
  private static final int PIPELINE_ID2 = 200;

  private static final int PIPELINE_ID3 = 300;

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
  }

  @Test
  public void getGroupsByType_smokeTestRoles_whenSingleGroup() {

    for (HuooRole role : HuooRole.values()) {
      try {
        var pipelineOrgRole = OrganisationRoleDtoTestUtil.createTreatyOrgUnitPipelineRoleInstance(
            role,
            TreatyAgreement.ANY_TREATY_COUNTRY,
            PIPELINE_ID1);
        var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
            Set.of(pipelineOrgRole)
        );

        assertThat(summary.getGroupsByHuooRole(role)).hasOnlyOneElementSatisfying(
            pipelineAndOrganisationRoleGroupDto -> {
              assertThat(pipelineAndOrganisationRoleGroupDto.getOrganisationRoleOwnerDtoSet())
                  .containsExactly(pipelineOrgRole.getOrganisationRoleOwnerDto());
              assertThat(pipelineAndOrganisationRoleGroupDto.getPipelineIdentifierSet())
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
        var pipelineOrgRole = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(role, OU_ID1, PIPELINE_ID1);
        var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
            Set.of(pipelineOrgRole)
        );

        assertThatThrownBy(
            () -> summary.getGroupsByHuooRole(role).add(new PipelineAndOrganisationRoleGroupDto(Set.of(), Set.of())))
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
        var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
            Set.of()
        );

        assertThatThrownBy(
            () -> summary.getGroupsByHuooRole(role).add(new PipelineAndOrganisationRoleGroupDto(Set.of(), Set.of())))
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

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holderOrg1Pipeline1Role));
    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).hasOnlyOneElementSatisfying(
        pipelineAndOrganisationRoleGroupDto -> {
          assertThat(pipelineAndOrganisationRoleGroupDto.getOrganisationRoleOwnerDtoSet())
              .containsExactly(holderOrg1Pipeline1Role.getOrganisationRoleInstanceDto().getOrganisationRoleOwnerDto());
          assertThat(pipelineAndOrganisationRoleGroupDto.getPipelineIdentifierSet())
              .containsExactly(new PipelineId(PIPELINE_ID1));
        });
  }

  /**
   * WHEN there are 2 pipelines
   * AND 2 organisation role owners share a role on both pipelines
   * THEN there is 1 group with both pipelines and both organisation roles
   */
  @Test
  public void aggregateOrganisationPipelineRoleDtos_singleHolderAndPipelineGroup_withMultipleOrgAndMultiplePipelines() {

    var holder1Pipeline1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    var holder1Pipeline2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID2);
    var holder2Pipeline1 = OrganisationRoleDtoTestUtil.createTreatyOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER, TreatyAgreement.ANY_TREATY_COUNTRY, PIPELINE_ID1);
    var holder2Pipeline2 = OrganisationRoleDtoTestUtil.createTreatyOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER, TreatyAgreement.ANY_TREATY_COUNTRY, PIPELINE_ID2);

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holder1Pipeline1,
            holder1Pipeline2,
            holder2Pipeline1,
            holder2Pipeline2)

    );

    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).hasOnlyOneElementSatisfying(
        pipelineAndOrganisationRoleGroupDto -> {
          assertThat(pipelineAndOrganisationRoleGroupDto.getOrganisationRoleOwnerDtoSet())
              .containsExactlyInAnyOrder(
                  holder1Pipeline1.getOrganisationRoleOwnerDto(),
                  holder2Pipeline1.getOrganisationRoleOwnerDto());
          assertThat(pipelineAndOrganisationRoleGroupDto.getPipelineIdentifierSet())
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

    var holder1Pipeline1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    var holder1Pipeline2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID2);

    var holder2Pipeline2 = OrganisationRoleDtoTestUtil.createTreatyOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER, TreatyAgreement.ANY_TREATY_COUNTRY, PIPELINE_ID2);
    var holder2Pipeline3 = OrganisationRoleDtoTestUtil.createTreatyOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER, TreatyAgreement.ANY_TREATY_COUNTRY, PIPELINE_ID3);

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holder1Pipeline1,
            holder1Pipeline2,
            holder2Pipeline2,
            holder2Pipeline3)

    );

    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).hasSize(3);
    // group containing holder 1 and pipeline 1
    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).anySatisfy(
        pipelineAndOrganisationRoleGroupDto -> {
          assertThat(pipelineAndOrganisationRoleGroupDto.getOrganisationRoleOwnerDtoSet())
              .containsExactlyInAnyOrder(holder1Pipeline1.getOrganisationRoleOwnerDto());
          assertThat(pipelineAndOrganisationRoleGroupDto.getPipelineIdentifierSet())
              .containsExactlyInAnyOrder(new PipelineId(PIPELINE_ID1));
        });

    // group containing holder 1 AND holder 2 and pipeline 2
    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).anySatisfy(
        pipelineAndOrganisationRoleGroupDto -> {
          assertThat(pipelineAndOrganisationRoleGroupDto.getOrganisationRoleOwnerDtoSet())
              .containsExactlyInAnyOrder(holder1Pipeline1.getOrganisationRoleOwnerDto(),
                  holder2Pipeline2.getOrganisationRoleOwnerDto());
          assertThat(pipelineAndOrganisationRoleGroupDto.getPipelineIdentifierSet())
              .containsExactlyInAnyOrder(new PipelineId(PIPELINE_ID2));
        });

    // group containing holder 2 and pipeline 3
    assertThat(summary.getGroupsByHuooRole(HuooRole.HOLDER)).anySatisfy(
        pipelineAndOrganisationRoleGroupDto -> {
          assertThat(pipelineAndOrganisationRoleGroupDto.getOrganisationRoleOwnerDtoSet())
              .containsExactlyInAnyOrder(holder2Pipeline2.getOrganisationRoleOwnerDto());
          assertThat(pipelineAndOrganisationRoleGroupDto.getPipelineIdentifierSet())
              .containsExactlyInAnyOrder(new PipelineId(PIPELINE_ID3));
        });
  }

  @Test
  public void getAllOrganisationRoleOwnersInSummary_whenSingleOrganisationHasMultipleRoles() {
    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holderOrg1Pipeline1Role, userOrg1Pipeline1Role, operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role)
    );
    assertThat(summary.getAllOrganisationRoleOwnersInSummary()).containsExactly(
        OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(OU_ID1));
  }

  @Test
  public void getAllOrganisationRoleOwnersInSummary_whenMultipleOrganisationHaveMultipleRoles() {
    var holder1Pipeline1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    var holder1Pipeline2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID2);

    var holder2Pipeline2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID2, PIPELINE_ID2);
    var holder2Pipeline3 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID2, PIPELINE_ID3);

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holder1Pipeline1, holder1Pipeline2, holder2Pipeline2, holder2Pipeline3)
    );
    assertThat(summary.getAllOrganisationRoleOwnersInSummary()).containsExactlyInAnyOrder(
        OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(OU_ID1),
        OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(OU_ID2)
    );
  }

  @Test
  public void getAllPipelineIdsInSummary_whenSinglePipelineHasMultipleRoles() {

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holderOrg1Pipeline1Role, userOrg1Pipeline1Role, operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role)
    );
    assertThat(summary.getAllPipelineIdentifiersInSummary()).containsExactly(new PipelineId(PIPELINE_ID1));
  }

  @Test
  public void getAllPipelineIdsInSummary_whenMultiplePipelinesHaveMultipleRoles() {

    var holder1Pipeline1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    var holder1Pipeline2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID2);

    var holder2Pipeline2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID2, PIPELINE_ID2);
    var holder2Pipeline3 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID2, PIPELINE_ID3);

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holder1Pipeline1, holder1Pipeline2, holder2Pipeline2, holder2Pipeline3)
    );

    assertThat(summary.getAllPipelineIdentifiersInSummary()).containsExactlyInAnyOrder(
        new PipelineId(PIPELINE_ID1),
        new PipelineId(PIPELINE_ID2),
        new PipelineId(PIPELINE_ID3)
    );
  }

  @Test
  public void getPipelineIdsWithAssignedRole_whenNoPipelinesForRole() {

    var holder1Pipeline1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    var holder1Pipeline2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID2);

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holder1Pipeline1, holder1Pipeline2)
    );

    assertThat(summary.getPipelineIdsWithAssignedRole(HuooRole.USER)).isEmpty();
  }

  @Test
  public void getPipelineIdsWithAssignedRole_whenPipelinesForRole() {

    var holder1Pipeline1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    var holder1Pipeline2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID2);

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holder1Pipeline1, holder1Pipeline2)
    );

    assertThat(summary.getPipelineIdsWithAssignedRole(HuooRole.HOLDER)).containsExactlyInAnyOrder(
        new PipelineId(PIPELINE_ID1),
        new PipelineId(PIPELINE_ID2)
    );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getPipelineIdsWithAssignedRole_unmodifiableSetReturned() {

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of()
    );

    summary.getPipelineIdsWithAssignedRole(HuooRole.HOLDER).add(new PipelineId(1));
  }

  @Test
  public void getOrganisationRoleOwnersWithAssignedRole_whenNoOrgsForRole() {

    var holder1Pipeline1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    var holder1Pipeline2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID2);

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holder1Pipeline1, holder1Pipeline2)
    );

    assertThat(summary.getOrganisationRoleOwnersWithAssignedRole(HuooRole.USER)).isEmpty();
  }

  @Test
  public void getOrganisationRoleOwnersWithAssignedRole_whenOrgsForRole() {

    var holder1Pipeline1 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID1);
    var holder1Pipeline2 = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(HuooRole.HOLDER, OU_ID1, PIPELINE_ID2);

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of(holder1Pipeline1, holder1Pipeline2)
    );

    assertThat(summary.getOrganisationRoleOwnersWithAssignedRole(HuooRole.HOLDER)).containsExactlyInAnyOrder(
        holder1Pipeline1.getOrganisationRoleOwnerDto()
    );
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOrganisationRoleOwnersWithAssignedRole_unmodifiableSetReturned() {

    var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
        Set.of()
    );

    summary.getOrganisationRoleOwnersWithAssignedRole(HuooRole.HOLDER).add(
        OrganisationRoleDtoTestUtil.createOrganisationUnitRoleOwnerDto(OU_ID1)
    );
  }

  @Test
  public void getGroupsByType_filtersOutRoleInstancesWhichAreNotAssignable() {

    for (HuooRole role : HuooRole.values()) {
      try {
        var unassignedPipelineSectionRoleInstance = OrganisationRoleDtoTestUtil.createUnassignedSplitPipelineSectionRoleInstance(
            role,
            PipelineSection.from(
                new PipelineId(PIPELINE_ID1),
                1,
                PipelineIdentPoint.inclusivePoint("A"), PipelineIdentPoint.inclusivePoint("B")
            ));
        var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
            Set.of(unassignedPipelineSectionRoleInstance)
        );

        assertThat(summary.getGroupsByHuooRole(role)).isEmpty();

      } catch (AssertionError e) {
        throw new AssertionError("Failed at HuooRole:" + role + "\n" + e.getMessage(), e);

      }
    }
  }

  @Test
  public void getAllPipelineIdentifiersInSummary_filtersOutRoleInstancesWhichAreNotAssignable() {

    for (HuooRole role : HuooRole.values()) {
      try {
        var unassignedPipelineSectionRoleInstance = OrganisationRoleDtoTestUtil.createUnassignedSplitPipelineSectionRoleInstance(
            role,
            PipelineSection.from(
                new PipelineId(PIPELINE_ID1),
                1,
                PipelineIdentPoint.inclusivePoint("A"), PipelineIdentPoint.inclusivePoint("B")
            ));
        var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
            Set.of(unassignedPipelineSectionRoleInstance)
        );

        assertThat(summary.getAllPipelineIdentifiersInSummary()).isEmpty();

      } catch (AssertionError e) {
        throw new AssertionError("Failed at HuooRole:" + role + "\n" + e.getMessage(), e);

      }
    }
  }

  @Test
  public void getOrganisationRoleOwnersWithAssignedRole_filtersOutRoleInstancesWhichAreNotAssignable() {

    for (HuooRole role : HuooRole.values()) {
      try {
        var unassignedPipelineSectionRoleInstance = OrganisationRoleDtoTestUtil.createUnassignedSplitPipelineSectionRoleInstance(
            role,
            PipelineSection.from(
                new PipelineId(PIPELINE_ID1),
                1,
                PipelineIdentPoint.inclusivePoint("A"), PipelineIdentPoint.inclusivePoint("B")
            ));
        var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
            Set.of(unassignedPipelineSectionRoleInstance)
        );

        assertThat(summary.getOrganisationRoleOwnersWithAssignedRole(role)).isEmpty();

      } catch (AssertionError e) {
        throw new AssertionError("Failed at HuooRole:" + role + "\n" + e.getMessage(), e);

      }
    }
  }

  @Test
  public void getAllOrganisationRoleOwnersInSummary_filtersOutRoleInstancesWhichAreNotAssignable() {

    for (HuooRole role : HuooRole.values()) {
      try {
        var unassignedPipelineSectionRoleInstance = OrganisationRoleDtoTestUtil.createUnassignedSplitPipelineSectionRoleInstance(
            role,
            PipelineSection.from(
                new PipelineId(PIPELINE_ID1),
                1,
                PipelineIdentPoint.inclusivePoint("A"), PipelineIdentPoint.inclusivePoint("B")
            ));
        var summary = PipelineAndOrganisationRoleGroupSummaryDto.aggregateOrganisationPipelineRoleDtos(
            Set.of(unassignedPipelineSectionRoleInstance)
        );

        assertThat(summary.getAllOrganisationRoleOwnersInSummary()).isEmpty();

      } catch (AssertionError e) {
        throw new AssertionError("Failed at HuooRole:" + role + "\n" + e.getMessage(), e);

      }
    }
  }

}