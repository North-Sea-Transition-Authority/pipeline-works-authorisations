package uk.co.ogauthority.pwa.domain.pwa.huoo.model;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.IdentLocationInclusionMode;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentPoint;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineSection;

@ExtendWith(MockitoExtension.class)
class OrganisationPipelineRoleInstanceDtoTest {
  private static int OU_ID = 1;
  private static int PIPELINE_ID = 2;


  @Test
  void organisationPipelineRoleDto_constructorMapsArgsAsExpected_whenWholePipelines() {

    for (HuooRole role : HuooRole.values()) {
      for (HuooType type : HuooType.values()) {
        var organisationPipelineRoleDto = new OrganisationPipelineRoleInstanceDto(
            OU_ID,
            null,
            null,
            role,
            type,
            PIPELINE_ID,
            null,
            null,
            null,
            null,
            null);

        assertThat(organisationPipelineRoleDto.getHuooRole()).isEqualTo(role);
        assertThat(organisationPipelineRoleDto.getHuooType()).isEqualTo(type);
        assertThat(organisationPipelineRoleDto.getPipelineIdentifier()).isEqualTo(new PipelineId(PIPELINE_ID));
        assertThat(organisationPipelineRoleDto.getOrganisationUnitId()).isEqualTo(new OrganisationUnitId(OU_ID));
        assertThat(organisationPipelineRoleDto.getPipelineIdentifier()).isOfAnyClassIn(PipelineId.class);
      }

    }


  }

  @Test
  void organisationPipelineRoleDto_constructorMapsArgsAsExpected_whenPipelineSections() {

    for (HuooRole role : HuooRole.values()) {
      for (HuooType type : HuooType.values()) {
        var organisationPipelineRoleDto = new OrganisationPipelineRoleInstanceDto(
            OU_ID,
            null,
            null,
            role,
            type,
            PIPELINE_ID,
            "Start",
            IdentLocationInclusionMode.INCLUSIVE,
            "End",
            IdentLocationInclusionMode.EXCLUSIVE,
            500);

        assertThat(organisationPipelineRoleDto.getHuooRole()).isEqualTo(role);
        assertThat(organisationPipelineRoleDto.getHuooType()).isEqualTo(type);
        assertThat(organisationPipelineRoleDto.getPipelineIdentifier()).satisfies(pipelineIdentifier -> {
          var pipelineSection = (PipelineSection) pipelineIdentifier;
          assertThat(pipelineSection.getFromPoint()).isEqualTo(PipelineIdentPoint.inclusivePoint("Start"));
          assertThat(pipelineSection.getToPoint()).isEqualTo(PipelineIdentPoint.exclusivePoint("End"));
          assertThat(pipelineSection.getPipelineId()).isEqualTo(new PipelineId(PIPELINE_ID));
          assertThat(pipelineSection.getSectionNumber()).isEqualTo(500);
        });
        assertThat(organisationPipelineRoleDto.getOrganisationUnitId()).isEqualTo(new OrganisationUnitId(OU_ID));

      }

    }


  }

  @Test
  void hasValidOrganisationRole_whenGivenOrgUnitId() {
    var organisationPipelineRoleDto = OrganisationRoleDtoTestUtil.createOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER,
        OU_ID,
        PIPELINE_ID
    );

    assertThat(organisationPipelineRoleDto.hasValidOrganisationRole()).isTrue();
  }

  @Test
  void hasValidOrganisationRole_whenNotGivenOrgUnitId() {
    var organisationPipelineRoleDto = OrganisationRoleDtoTestUtil.createMigratedOrgUnitPipelineRoleInstance(
        HuooRole.HOLDER,
        "someName",
        PIPELINE_ID
    );

    assertThat(organisationPipelineRoleDto.hasValidOrganisationRole()).isFalse();
  }

  @Test
  void equals(){

    EqualsVerifier.forClass(OrganisationPipelineRoleInstanceDto.class)
        .verify();
  }





}