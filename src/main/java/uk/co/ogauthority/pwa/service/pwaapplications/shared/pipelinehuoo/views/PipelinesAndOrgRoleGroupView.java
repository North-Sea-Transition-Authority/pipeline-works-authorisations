package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooType;
import uk.co.ogauthority.pwa.model.entity.enums.TreatyAgreement;

/**
 * Captures a set of organisationRoleOwners where they have an instance of that role associated with a pipeline and allows
 * easy display of the owner names and pipeline numbers.
 */
public final class PipelinesAndOrgRoleGroupView {

  private final Set<PipelineId> pipelineIdSet;
  private final Set<OrganisationRoleOwnerDto> organisationRoleOwnerSet;
  private final List<String> pipelineNumbers;
  private final List<String> organisationNames;
  private final String sortKey;

  PipelinesAndOrgRoleGroupView(
      Set<PipelineId> pipelineIdSet,
      Set<OrganisationRoleOwnerDto> organisationRoleOwnerSet,
      List<String> pipelineNumbers,
      List<String> organisationNames) {
    this.pipelineIdSet = pipelineIdSet;
    this.organisationRoleOwnerSet = organisationRoleOwnerSet;
    this.pipelineNumbers = pipelineNumbers.stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());
    this.organisationNames = organisationNames.stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());
    this.sortKey = String.join(",", this.pipelineNumbers);
  }

  public Set<PipelineId> getPipelineIdSet() {
    return pipelineIdSet;
  }

  public Set<OrganisationRoleOwnerDto> getOrganisationRoleOwnerSet() {
    return organisationRoleOwnerSet;
  }


  public Set<OrganisationUnitId> getOrganisationIdsOfRoleOwners() {
    return this.organisationRoleOwnerSet.stream()
        .filter(o -> HuooType.PORTAL_ORG.equals(o.getHuooType()))
        .map(OrganisationRoleOwnerDto::getOrganisationUnitId)
        .collect(Collectors.toSet());
  }

  public Set<TreatyAgreement> getTreatyAgreementsOfRoleOwners() {
    return this.organisationRoleOwnerSet.stream()
        .filter(o -> HuooType.TREATY_AGREEMENT.equals(o.getHuooType()))
        .map(OrganisationRoleOwnerDto::getTreatyAgreement)
        .collect(Collectors.toSet());
  }

  public List<String> getPipelineNumbers() {
    return pipelineNumbers;
  }

  public List<String> getOrganisationNames() {
    return organisationNames;
  }

  public String getSortKey() {
    return sortKey;
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
        && Objects.equals(organisationNames, that.organisationNames)
        && Objects.equals(sortKey, that.sortKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pipelineIdSet, organisationRoleOwnerSet, pipelineNumbers, organisationNames, sortKey);
  }

  @Override
  public String toString() {
    return "PipelinesAndOrgRoleGroupView{" +
        "pipelineIdSet=" + pipelineIdSet +
        ", organisationRoleOwnerSet=" + organisationRoleOwnerSet +
        ", pipelineNumbers=" + pipelineNumbers +
        ", organisationNames=" + organisationNames +
        ", sortKey='" + sortKey + '\'' +
        '}';
  }
}
