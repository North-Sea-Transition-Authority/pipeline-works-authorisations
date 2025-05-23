package uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleInstanceDto;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;

/**
 * Data object containing all the pipeline links for a given organisation with a role.
 */
public class OrganisationRolePipelineGroupDto {

  private final OrganisationRoleInstanceDto organisationRoleInstanceDto;
  private final Set<PipelineIdentifier> pipelineIdentifiers;

  public OrganisationRolePipelineGroupDto(OrganisationRoleInstanceDto organisationRoleInstanceDto,
                                          Set<PipelineIdentifier> pipelineIdentifiers) {
    this.organisationRoleInstanceDto = organisationRoleInstanceDto;
    this.pipelineIdentifiers = pipelineIdentifiers.stream()
        // make sure no null pipeline identifiers entries get into the final set. Required because we want role owners with
        // no pipeline identifier links to be accounted for in aggregate summaries.
        .filter(Objects::nonNull)
        .collect(Collectors.toUnmodifiableSet());
  }


  public OrganisationUnitId getOrganisationUnitId() {
    return this.organisationRoleInstanceDto != null ? this.organisationRoleInstanceDto.getOrganisationUnitId() : null;
  }

  public OrganisationRoleInstanceDto getOrganisationRoleInstanceDto() {
    return this.organisationRoleInstanceDto;
  }

  public Optional<String> getManualOrganisationName() {
    return this.organisationRoleInstanceDto.getManualOrganisationName();
  }

  public boolean hasValidOrganisationRole() {
    return this.organisationRoleInstanceDto.isPortalOrgRole();
  }

  public HuooRole getHuooRole() {
    return this.organisationRoleInstanceDto.getHuooRole();
  }

  public HuooType getHuooType() {
    return this.organisationRoleInstanceDto.getHuooType();
  }

  public Set<PipelineIdentifier> getPipelineIdentifiers() {
    return Collections.unmodifiableSet(pipelineIdentifiers);
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
    return Objects.equals(organisationRoleInstanceDto, that.organisationRoleInstanceDto)
        && Objects.equals(pipelineIdentifiers, that.pipelineIdentifiers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organisationRoleInstanceDto, pipelineIdentifiers);
  }

  @Override
  public String toString() {
    return "OrganisationPipelineRoleGroupDto{" +
        "organisationRoleInstanceDto=" + organisationRoleInstanceDto +
        ", pipelineIds=" + pipelineIdentifiers +
        '}';
  }
}
