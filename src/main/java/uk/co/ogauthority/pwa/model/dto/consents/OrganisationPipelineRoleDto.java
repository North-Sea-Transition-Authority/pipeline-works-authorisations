package uk.co.ogauthority.pwa.model.dto.consents;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;

/* Data object encapsulating link between an organisation's PWA role and a pipeline.
 *  Use Cases:
 *  a) building block object for creating application hupp data from consent model
 *  b) building block object for diffing application huoo data from consent model
 * */
public class OrganisationPipelineRoleDto {
  private final OrganisationRoleDto organisationRoleDto;
  private final PipelineId pipeline;


  public OrganisationPipelineRoleDto(Integer organisationUnitId,
                                     String manualOrganisationName,
                                     HuooRole huooRole,
                                     HuooType huooType,
                                     int pipelineId) {
    this.organisationRoleDto = new OrganisationRoleDto(organisationUnitId, manualOrganisationName, huooRole, huooType);
    this.pipeline = new PipelineId(pipelineId);
  }

  public OrganisationUnitId getOrganisationUnitId() {
    return this.organisationRoleDto.getOrganisationUnitId();
  }

  public boolean hasValidOrganisationRole() {
    return this.organisationRoleDto.isValidOrganisationRole();
  }

  public HuooRole getHuooRole() {
    return this.organisationRoleDto.getHuooRole();
  }

  public HuooType getHuooType() {
    return this.organisationRoleDto.getHuooType();
  }

  public PipelineId getPipeline() {
    return this.pipeline;
  }

  public OrganisationRoleDto getOrganisationRoleDto() {
    return this.organisationRoleDto;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganisationPipelineRoleDto that = (OrganisationPipelineRoleDto) o;
    return Objects.equals(organisationRoleDto, that.organisationRoleDto)
        && Objects.equals(pipeline, that.pipeline);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationRoleDto, pipeline);
  }


}
