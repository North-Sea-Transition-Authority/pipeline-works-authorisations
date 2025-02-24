package uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleDtoTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifierTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;

class OrganisationRolePipelineGroupDtoTest {

  private static int OU_ID = 1;
  private static int PIPELINE_ID = 2;
  private static int SPLIT_PIPELINE_ID = 3;

  private OrganisationRoleInstanceDto organisationRoleInstanceDto;
  private PipelineId pipelineId;
  private PipelineSection pipelineSection;

  @BeforeEach
  void setup() {
    pipelineId = new PipelineId(PIPELINE_ID);
    pipelineSection = PipelineIdentifierTestUtil.createInclusivePipelineSection(SPLIT_PIPELINE_ID, "FROM", "TO");

    organisationRoleInstanceDto = OrganisationRoleDtoTestUtil.createOrganisationUnitOrgRoleInstance(
        HuooRole.HOLDER,
        OU_ID
    );
  }

  @Test
  void getOrganisationUnitId_whenValidOrgRoleGiven() {
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto,
        Set.of(pipelineId));
    assertThat(organisationRolePipelineGroupDto.getOrganisationUnitId()).isEqualTo(new OrganisationUnitId(OU_ID));

  }

  @Test
  void getOrganisationUnitId_whenInvalidOrgRoleGiven() {
    organisationRoleInstanceDto = OrganisationRoleDtoTestUtil.createMigratedOrgRoleInstance(
        HuooRole.HOLDER,
        "some name"
    );
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto,
        Set.of(pipelineId));
    assertThat(organisationRolePipelineGroupDto.getOrganisationUnitId()).isNull();
  }


  @Test
  void hasValidOrganisationRole_whenValidOrgRoleGiven() {
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto,
        Set.of(pipelineId));
    assertThat(organisationRolePipelineGroupDto.hasValidOrganisationRole()).isTrue();

  }

  @Test
  void hasValidOrganisationRole_whenInvalidOrgRoleGiven() {
    organisationRoleInstanceDto = OrganisationRoleDtoTestUtil.createMigratedOrgRoleInstance(
        HuooRole.HOLDER,
        "some name"
    );
    var t = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto, Set.of(pipelineId));
    assertThat(t.hasValidOrganisationRole()).isFalse();
  }

  @Test
  void getPipelines_returnsConstructorArg() {

    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto,
        Set.of(
            pipelineId,
            pipelineSection
        )
    );
    assertThat(organisationRolePipelineGroupDto.getPipelineIdentifiers()).isEqualTo(
        Set.of(pipelineId, pipelineSection));
  }

  @Test
  void getPipelines_setCannotBeModified() {
    var organisationRolePipelineGroupDto = new OrganisationRolePipelineGroupDto(organisationRoleInstanceDto,
          Set.of(pipelineId));
    assertThrows(UnsupportedOperationException.class, () ->
      organisationRolePipelineGroupDto.getPipelineIdentifiers().add(new PipelineId(99999)));
  }
}