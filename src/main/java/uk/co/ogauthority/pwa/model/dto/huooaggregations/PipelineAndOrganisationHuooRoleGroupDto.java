package uk.co.ogauthority.pwa.model.dto.huooaggregations;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;

public class PipelineAndOrganisationHuooRoleGroupDto {
  private final Set<OrganisationRoleDto> organisationRoleDtoSet;
  private final Set<PipelineId> pipelineIdSet;

  PipelineAndOrganisationHuooRoleGroupDto(
      Set<OrganisationRoleDto> organisationRoleDtoSet,
      Set<PipelineId> pipelineIdSet) {
    this.organisationRoleDtoSet = organisationRoleDtoSet;
    this.pipelineIdSet = pipelineIdSet;
  }

  public Set<OrganisationRoleDto> getOrganisationRoleDtoSet() {
    return Collections.unmodifiableSet(organisationRoleDtoSet);
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
    PipelineAndOrganisationHuooRoleGroupDto that = (PipelineAndOrganisationHuooRoleGroupDto) o;
    return Objects.equals(organisationRoleDtoSet, that.organisationRoleDtoSet)
        && Objects.equals(pipelineIdSet, that.pipelineIdSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationRoleDtoSet, pipelineIdSet);
  }
}
