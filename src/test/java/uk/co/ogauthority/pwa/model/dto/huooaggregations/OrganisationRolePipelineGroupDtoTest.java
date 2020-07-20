package uk.co.ogauthority.pwa.model.dto.huooaggregations;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineIdentifierTestUtil;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineSegment;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

public class OrganisationRolePipelineGroupDtoTest {

  private static int OU_ID = 1;
  private static int PIPELINE_ID = 2;
  private static int SPLIT_PIPELINE_ID = 3;

  private OrganisationRoleInstanceDto organisationRoleInstanceDto;
  private PipelineId pipelineId;
  private PipelineSegment pipelineSegment;

  @Before
  public void setup() {
    pipelineId = new PipelineId(PIPELINE_ID);
    pipelineSegment = PipelineIdentifierTestUtil.createInclusivePipelineSegment(SPLIT_PIPELINE_ID, "FROM", "TO");

    organisationRoleInstanceDto = OrganisationRoleDtoTestUtil.createOrganisationUnitOrgRoleInstance(
        HuooRole.HOLDER,
        OU_ID
    );
  }

  @Test
  public void getOrganisationUnitId_whenValidOrgRoleGiven() {
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto,
        Set.of(pipelineId));
    assertThat(organisationRolePipelineGroupDto.getOrganisationUnitId()).isEqualTo(new OrganisationUnitId(OU_ID));

  }

  @Test
  public void getOrganisationUnitId_whenInvalidOrgRoleGiven() {
    organisationRoleInstanceDto = OrganisationRoleDtoTestUtil.createMigratedOrgRoleInstance(
        HuooRole.HOLDER,
        "some name"
    );
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto,
        Set.of(pipelineId));
    assertThat(organisationRolePipelineGroupDto.getOrganisationUnitId()).isNull();
  }


  @Test
  public void hasValidOrganisationRole_whenValidOrgRoleGiven() {
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto,
        Set.of(pipelineId));
    assertThat(organisationRolePipelineGroupDto.hasValidOrganisationRole()).isTrue();

  }

  @Test
  public void hasValidOrganisationRole_whenInvalidOrgRoleGiven() {
    organisationRoleInstanceDto = OrganisationRoleDtoTestUtil.createMigratedOrgRoleInstance(
        HuooRole.HOLDER,
        "some name"
    );
    var t = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto, Set.of(pipelineId));
    assertThat(t.hasValidOrganisationRole()).isFalse();
  }

  @Test
  public void getPipelines_returnsConstructorArg() {

    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto,
        Set.of(
            pipelineId,
            pipelineSegment
        )
    );
    assertThat(organisationRolePipelineGroupDto.getPipelineIdentifiers()).isEqualTo(
        Set.of(pipelineId, pipelineSegment));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getPipelines_setCannotBeModified() {
    // The pipeline set MUST be unmodifiable as we want to use it as a map key
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto,
        Set.of(pipelineId));
    organisationRolePipelineGroupDto.getPipelineIdentifiers().add(new PipelineId(99999));
  }
}