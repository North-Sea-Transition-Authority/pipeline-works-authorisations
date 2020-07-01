package uk.co.ogauthority.pwa.model.dto.huooaggregations;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;

public class PipelineAndOrganisationRoleGroupDto {
  private final Set<OrganisationRoleInstanceDto> organisationRoleInstanceDtoSet;
  private final Set<PipelineId> pipelineIdSet;

  PipelineAndOrganisationRoleGroupDto(
      Set<OrganisationRoleInstanceDto> organisationRoleInstanceDtoSet,
      Set<PipelineId> pipelineIdSet) {
    this.organisationRoleInstanceDtoSet = organisationRoleInstanceDtoSet;
    this.pipelineIdSet = pipelineIdSet;
  }

  public Set<OrganisationRoleInstanceDto> getOrganisationRoleInstanceDtoSet() {
    return Collections.unmodifiableSet(organisationRoleInstanceDtoSet);
  }

  public Set<PipelineId> getPipelineIdSet() {
    return Collections.unmodifiableSet(pipelineIdSet);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelineAndOrganisationRoleGroupDto that = (PipelineAndOrganisationRoleGroupDto) o;
    return Objects.equals(organisationRoleInstanceDtoSet, that.organisationRoleInstanceDtoSet)
        && Objects.equals(pipelineIdSet, that.pipelineIdSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationRoleInstanceDtoSet, pipelineIdSet);
  }
}
