package uk.co.ogauthority.pwa.model.dto.consents;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;

/**
 * Data object containing all the pipeline links for a given organisation with a role on a PWA.
 */
public class OrganisationRolePipelineGroupDto {

  private final OrganisationRoleDto organisationRoleDto;
  private final Set<PipelineId> pipelines;

  public OrganisationRolePipelineGroupDto(OrganisationRoleDto organisationRoleDto,
                                          Set<PipelineId> pipelines) {
    this.organisationRoleDto = organisationRoleDto;
    this.pipelines = pipelines;
  }


  public OrganisationUnitId getOrganisationUnitId() {
    return this.organisationRoleDto != null ? this.organisationRoleDto.getOrganisationUnitId() : null;
  }

  public Optional<String> getManualOrganisationName() {
    return this.organisationRoleDto.getManualOrganisationName();
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

  public Set<PipelineId> getPipelines() {
    return pipelines;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganisationRolePipelineGroupDto that = (OrganisationRolePipelineGroupDto) o;
    return Objects.equals(organisationRoleDto, that.organisationRoleDto)
        && Objects.equals(pipelines, that.pipelines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationRoleDto, pipelines);
  }

  @Override
  public String toString() {
    return "OrganisationPipelineRoleGroupDto{" +
        "organisationRoleDto=" + organisationRoleDto +
        ", pipelines=" + pipelines +
        '}';
  }
}
