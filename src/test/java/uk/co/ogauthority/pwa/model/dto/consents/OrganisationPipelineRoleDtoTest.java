package uk.co.ogauthority.pwa.model.dto.consents;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;

@RunWith(MockitoJUnitRunner.class)
public class OrganisationPipelineRoleDtoTest {
  private static int OU_ID = 1;
  private static int PIPELINE_ID = 2;


  @Test
  public void organisationPipelineRoleDto_constructorMapsArgsAsExpected() {

    for (HuooRole role : HuooRole.values()) {
      for (HuooType type : HuooType.values()) {
        var organisationPipelineRoleDto = new OrganisationPipelineRoleDto(
            OU_ID,
            null,
            role,
            type,
            PIPELINE_ID);

        assertThat(organisationPipelineRoleDto.getHuooRole()).isEqualTo(role);
        assertThat(organisationPipelineRoleDto.getHuooType()).isEqualTo(type);
        assertThat(organisationPipelineRoleDto.getPipeline()).isEqualTo(new PipelineId(PIPELINE_ID));
        assertThat(organisationPipelineRoleDto.getOrganisationUnitId()).isEqualTo(new OrganisationUnitId(OU_ID));
      }

    }


  }

  @Test
  public void hasValidOrganisationRole_whenGivenOrgUnitId() {
    var organisationPipelineRoleDto = new OrganisationPipelineRoleDto(
        OU_ID,
        null,
        HuooRole.HOLDER,
        HuooType.PORTAL_ORG,
        PIPELINE_ID);

    assertThat(organisationPipelineRoleDto.hasValidOrganisationRole()).isTrue();
  }

  @Test
  public void hasValidOrganisationRole_whenNotGivenOrgUnitId() {
    var organisationPipelineRoleDto = new OrganisationPipelineRoleDto(
        null,
        "some name",
        HuooRole.HOLDER,
        HuooType.PORTAL_ORG,
        PIPELINE_ID);

    assertThat(organisationPipelineRoleDto.hasValidOrganisationRole()).isFalse();
  }
}