package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;

/**
 * Captures a set of organisationRoleOwners where they have an instance of that role associated with a pipeline and allows
 * easy display of the owner names and pipeline numbers.
 */
public final class PipelinesAndOrgRoleGroupView {

  private final Set<PipelineId> pipelineIdSet;
  private final Set<OrganisationRoleOwnerDto> organisationRoleOwnerSet;
  private final List<String> pipelineNumbers;
  private final List<String> organisationNames;

  PipelinesAndOrgRoleGroupView(
      Set<PipelineId> pipelineIdSet,
      Set<OrganisationRoleOwnerDto> organisationRoleOwnerSet,
      List<String> pipelineNumbers,
      List<String> organisationNames) {
    this.pipelineIdSet = pipelineIdSet;
    this.organisationRoleOwnerSet = organisationRoleOwnerSet;
    this.pipelineNumbers = pipelineNumbers;
    this.organisationNames = organisationNames;
  }

  public Set<PipelineId> getPipelineIdSet() {
    return pipelineIdSet;
  }

  public Set<OrganisationRoleOwnerDto> getOrganisationRoleOwnerSet() {
    return organisationRoleOwnerSet;
  }

  public List<String> getPipelineNumbers() {
    return pipelineNumbers;
  }

  public List<String> getOrganisationNames() {
    return organisationNames;
  }

  @Override
  public String toString() {
    return "PipelinesAndOrgRoleGroupView{" +
        "pipelineIdSet=" + pipelineIdSet +
        ", organisationRoleOwnerSet=" + organisationRoleOwnerSet +
        ", pipelineNumbers=" + pipelineNumbers +
        ", organisationNames=" + organisationNames +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PipelinesAndOrgRoleGroupView that = (PipelinesAndOrgRoleGroupView) o;
    return Objects.equals(pipelineIdSet, that.pipelineIdSet)
        && Objects.equals(organisationRoleOwnerSet, that.organisationRoleOwnerSet)
        && Objects.equals(pipelineNumbers, that.pipelineNumbers)
        && Objects.equals(organisationNames, that.organisationNames);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineIdSet, organisationRoleOwnerSet, pipelineNumbers, organisationNames);
  }
}
