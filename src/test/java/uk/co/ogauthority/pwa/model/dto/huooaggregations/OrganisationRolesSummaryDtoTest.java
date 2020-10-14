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
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifierTestUtil;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSection;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationRolesSummaryDtoTest {

  private static final int OU_ID1 = 10;
  private static final int PIPELINE_ID1 = 20;

  private static final int OU_ID2 = 100;
  private static final int PIPELINE_ID2 = 200;

  private static final int PIPELINE_ID3 = 200;
  private static final String PIPELINE_POINT_1 = "START";
  private static final String PIPELINE_POINT_2 = "MID";
  private static final String PIPELINE_POINT_3 = "END";

  private PipelineId pipelineId1;
  private PipelineId pipelineId2;
  private PipelineSection pipelineSection1;
  private PipelineSection pipelineSection2;


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

    pipelineSection1 = PipelineIdentifierTestUtil.createInclusivePipelineSection(PIPELINE_ID3, PIPELINE_POINT_1, PIPELINE_POINT_2);
    pipelineSection2 = PipelineIdentifierTestUtil.createInclusivePipelineSection(PIPELINE_ID3, PIPELINE_POINT_2, PIPELINE_POINT_3);
  }


  @Test
  public void getHolderOrganisationUnitGroups_whenSingleGroup_ofWholePipelines() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role, userOrg1Pipeline1Role, operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role));
    assertThat(summary.getHolderOrganisationUnitGroups())
        .containsExactly(
            new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of(pipelineId1)));

  }


  @Test(expected = UnsupportedOperationException.class)
  public void getHolderOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role));
    assertThat(summary.getHolderOrganisationUnitGroups()).hasSize(1);
    summary.getHolderOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(operatorOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getHolderOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getHolderOrganisationUnitGroups()).hasSize(0);
    summary.getHolderOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of()));
  }

  @Test
  public void getUserOrganisationUnitGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role, userOrg1Pipeline1Role, operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role));
    assertThat(summary.getUserOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(userOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of(pipelineId1)));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUserOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(userOrg1Pipeline1Role));
    assertThat(summary.getUserOrganisationUnitGroups()).hasSize(1);
    summary.getUserOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getUserOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getUserOrganisationUnitGroups()).hasSize(0);
    summary.getUserOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(userOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of()));
  }

  @Test
  public void getOperatorOrganisationUnitGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role, userOrg1Pipeline1Role, operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role));
    assertThat(summary.getOperatorOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(operatorOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of(
            pipelineId1)));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOperatorOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(operatorOrg1Pipeline1Role));
    assertThat(summary.getOperatorOrganisationUnitGroups()).hasSize(1);
    summary.getOperatorOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOperatorOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getOperatorOrganisationUnitGroups()).hasSize(0);
    summary.getOperatorOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(operatorOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of()));
  }

  @Test
  public void getOwnerOrganisationGroups_whenSingleGroup() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role, userOrg1Pipeline1Role, operatorOrg1Pipeline1Role, ownerOrg1Pipeline1Role));
    assertThat(summary.getOwnerOrganisationUnitGroups())
        .containsExactly(new OrganisationRolePipelineGroupDto(ownerOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of(pipelineId1)));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOwnerOrganisationUnitGroups_cannotModifyPopulatedSet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(ownerOrg1Pipeline1Role));
    assertThat(summary.getOwnerOrganisationUnitGroups()).hasSize(1);
    summary.getOwnerOrganisationUnitGroups().add(
        // different role so not a duplicate entry
        new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getOwnerOrganisationUnitGroups_cannotModifyEmptySet() {

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of());
    assertThat(summary.getOwnerOrganisationUnitGroups()).hasSize(0);
    summary.getOwnerOrganisationUnitGroups().add(
        new OrganisationRolePipelineGroupDto(ownerOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of()));
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
            new OrganisationRolePipelineGroupDto(holderOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleInstanceDto(), Set.of(pipelineId2)
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
            new OrganisationRolePipelineGroupDto(userOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleInstanceDto(), Set.of(pipelineId2)
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
            new OrganisationRolePipelineGroupDto(operatorOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleInstanceDto(), Set.of(pipelineId2)
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
            new OrganisationRolePipelineGroupDto(ownerOrg1Pipeline1Role.getOrganisationRoleInstanceDto(), Set.of(pipelineId1)),
            new OrganisationRolePipelineGroupDto(
                secondOrganisationPipelineRole.getOrganisationRoleInstanceDto(), Set.of(pipelineId2)
            )

        );

  }

  @Test
  public void getHolderOrganisationUnitGroups_whenMultipleGroups_ofSectionedPipelines_andWholePipelines() {
    var ou1HolderSection = OrganisationRoleDtoTestUtil.createOrgUnitPipelineSectionRoleInstance(HuooRole.HOLDER, OU_ID1,
        pipelineSection1);
    var ou2HolderSection = OrganisationRoleDtoTestUtil.createOrgUnitPipelineSectionRoleInstance(HuooRole.HOLDER, OU_ID2,
        pipelineSection2);

    var org1HolderOwnerDto = holderOrg1Pipeline1Role.getOrganisationRoleInstanceDto();
    var org2HolderOwnerDto = ou2HolderSection.getOrganisationRoleInstanceDto();

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(holderOrg1Pipeline1Role, ou1HolderSection, ou2HolderSection)
    );

    assertThat(summary.getHolderOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(org1HolderOwnerDto, Set.of(pipelineId1, pipelineSection1)),
            new OrganisationRolePipelineGroupDto(org2HolderOwnerDto, Set.of(pipelineSection2)
            )
        );
  }

  @Test
  public void getUserOrganisationUnitGroups_whenMultipleGroups_ofSectionedPipelines_andWholePipelines() {
    var ou1UserSection = OrganisationRoleDtoTestUtil.createOrgUnitPipelineSectionRoleInstance(HuooRole.USER, OU_ID1,
        pipelineSection1);
    var ou2UserSection = OrganisationRoleDtoTestUtil.createOrgUnitPipelineSectionRoleInstance(HuooRole.USER, OU_ID2,
        pipelineSection2);

    var org1UserOwnerDto = userOrg1Pipeline1Role.getOrganisationRoleInstanceDto();
    var org2UserOwnerDto = ou2UserSection.getOrganisationRoleInstanceDto();

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(userOrg1Pipeline1Role, ou1UserSection, ou2UserSection)
    );

    assertThat(summary.getUserOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(org1UserOwnerDto, Set.of(pipelineId1, pipelineSection1)),
            new OrganisationRolePipelineGroupDto(org2UserOwnerDto, Set.of(pipelineSection2)
            )
        );
  }

  @Test
  public void getOperatorOrganisationUnitGroups_whenMultipleGroups_ofSectionedPipelines_andWholePipelines() {
    var ou1OperatorSection = OrganisationRoleDtoTestUtil.createOrgUnitPipelineSectionRoleInstance(HuooRole.OPERATOR, OU_ID1,
        pipelineSection1);
    var ou2OperatorSection = OrganisationRoleDtoTestUtil.createOrgUnitPipelineSectionRoleInstance(HuooRole.OPERATOR, OU_ID2,
        pipelineSection2);

    var org1OperatorOwnerDto = operatorOrg1Pipeline1Role.getOrganisationRoleInstanceDto();
    var org2OperatorOwnerDto = ou2OperatorSection.getOrganisationRoleInstanceDto();

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(operatorOrg1Pipeline1Role, ou1OperatorSection, ou2OperatorSection)
    );

    assertThat(summary.getOperatorOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(org1OperatorOwnerDto, Set.of(pipelineId1, pipelineSection1)),
            new OrganisationRolePipelineGroupDto(org2OperatorOwnerDto, Set.of(pipelineSection2)
            )
        );
  }

  @Test
  public void getOwnerOrganisationUnitGroups_whenMultipleGroups_ofSectionedPipelines_andWholePipelines() {
    var ou1OwnerSection = OrganisationRoleDtoTestUtil.createOrgUnitPipelineSectionRoleInstance(HuooRole.OWNER, OU_ID1,
        pipelineSection1);
    var ou2OwnerSection = OrganisationRoleDtoTestUtil.createOrgUnitPipelineSectionRoleInstance(HuooRole.OWNER, OU_ID2,
        pipelineSection2);

    var org1OwnerOwnerDto = ownerOrg1Pipeline1Role.getOrganisationRoleInstanceDto();
    var org2OwnerOwnerDto = ou2OwnerSection.getOrganisationRoleInstanceDto();

    var summary = OrganisationRolesSummaryDto.aggregateOrganisationPipelineRoles(
        Set.of(ownerOrg1Pipeline1Role, ou1OwnerSection, ou2OwnerSection)
    );

    assertThat(summary.getOwnerOrganisationUnitGroups())
        .containsExactlyInAnyOrder(
            new OrganisationRolePipelineGroupDto(org1OwnerOwnerDto, Set.of(pipelineId1, pipelineSection1)),
            new OrganisationRolePipelineGroupDto(org2OwnerOwnerDto, Set.of(pipelineSection2)
            )
        );
  }


}