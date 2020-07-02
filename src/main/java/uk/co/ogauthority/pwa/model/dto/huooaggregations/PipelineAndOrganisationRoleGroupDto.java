package uk.co.ogauthority.pwa.model.dto.huooaggregations;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;

public class PipelineAndOrganisationRoleGroupDto {
  private final Set<OrganisationRoleOwnerDto> organisationRoleOwnerDtoSet;
  private final Set<PipelineId> pipelineIdSet;

  PipelineAndOrganisationRoleGroupDto(
      Set<OrganisationRoleOwnerDto> organisationRoleOwnerDtoSet,
      Set<PipelineId> pipelineIdSet) {
    this.organisationRoleOwnerDtoSet = organisationRoleOwnerDtoSet;
    this.pipelineIdSet = pipelineIdSet;
  }

  public Set<OrganisationRoleOwnerDto> getOrganisationRoleOwnerDtoSet() {
    return Collections.unmodifiableSet(organisationRoleOwnerDtoSet);
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
    return Objects.equals(organisationRoleOwnerDtoSet, that.organisationRoleOwnerDtoSet)
        && Objects.equals(pipelineIdSet, that.pipelineIdSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationRoleOwnerDtoSet, pipelineIdSet);
  }
}
