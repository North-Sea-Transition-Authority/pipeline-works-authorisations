package uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;

/**
 * <p>Multiple whole pipelines or pipeline sections can be associated with one or more org role owners.</p>
 *
 * <p>This class captures a group of pipelines or pipeline sections which have the same group of associated org role owners.</p>
 */
public class PipelineAndOrganisationRoleGroupDto {
  private final Set<OrganisationRoleOwnerDto> organisationRoleOwnerDtoSet;
  private final Set<PipelineIdentifier> pipelineIdentifierSet;

  PipelineAndOrganisationRoleGroupDto(
      Set<OrganisationRoleOwnerDto> organisationRoleOwnerDtoSet,
      Set<PipelineIdentifier> pipelineIdentifierSet) {
    this.organisationRoleOwnerDtoSet = organisationRoleOwnerDtoSet;
    this.pipelineIdentifierSet = pipelineIdentifierSet;
  }

  public Set<OrganisationRoleOwnerDto> getOrganisationRoleOwnerDtoSet() {
    return Collections.unmodifiableSet(organisationRoleOwnerDtoSet);
  }

  public Set<PipelineIdentifier> getPipelineIdentifierSet() {
    return Collections.unmodifiableSet(pipelineIdentifierSet);
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
        && Objects.equals(pipelineIdentifierSet, that.pipelineIdentifierSet);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationRoleOwnerDtoSet, pipelineIdentifierSet);
  }
}
