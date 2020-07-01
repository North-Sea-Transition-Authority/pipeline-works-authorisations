package uk.co.ogauthority.pwa.model.dto.huooaggregations;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;

public class OrganisationRolePipelineGroupDtoTest {

  private static int OU_ID = 1;
  private static int PIPELINE_ID = 2;

  private OrganisationRoleInstanceDto organisationRoleInstanceDto;
  private PipelineId pipelineId;

  @Before
  public void setup() {
    pipelineId = new PipelineId(PIPELINE_ID);
    organisationRoleInstanceDto = new OrganisationRoleInstanceDto(OU_ID, null, HuooRole.HOLDER, HuooType.PORTAL_ORG);
  }

  @Test
  public void getOrganisationUnitId_whenValidOrgRoleGiven() {
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto, Set.of(pipelineId));
    assertThat(organisationRolePipelineGroupDto.getOrganisationUnitId()).isEqualTo(new OrganisationUnitId(OU_ID));

  }

  @Test
  public void getOrganisationUnitId_whenInvalidOrgRoleGiven() {
    organisationRoleInstanceDto = new OrganisationRoleInstanceDto(null, "some name", HuooRole.HOLDER, HuooType.PORTAL_ORG);
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto, Set.of(pipelineId));
    assertThat(organisationRolePipelineGroupDto.getOrganisationUnitId()).isNull();
  }



  @Test
  public void hasValidOrganisationRole_whenValidOrgRoleGiven() {
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto, Set.of(pipelineId));
    assertThat(organisationRolePipelineGroupDto.hasValidOrganisationRole()).isTrue();

  }

  @Test
  public void hasValidOrganisationRole_whenInvalidOrgRoleGiven() {
    organisationRoleInstanceDto = new OrganisationRoleInstanceDto(null, "some name", HuooRole.HOLDER, HuooType.PORTAL_ORG);
    var t = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto, Set.of(pipelineId));
    assertThat(t.hasValidOrganisationRole()).isFalse();
  }

  @Test
  public void getPipelines_returnsConstructorArg() {
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto, Set.of(pipelineId));
    assertThat(organisationRolePipelineGroupDto.getPipelineIds()).isEqualTo(Set.of(pipelineId));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void getPipelines_setCannotBeModified() {
    // The pipeline set MUST be unmodifiable as we want to use it as a map key
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto, Set.of(pipelineId));
    organisationRolePipelineGroupDto.getPipelineIds().add(new PipelineId(99999));
  }
}