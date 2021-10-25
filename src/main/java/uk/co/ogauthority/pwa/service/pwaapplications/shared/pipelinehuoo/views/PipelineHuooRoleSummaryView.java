package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.TreatyAgreement;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineIdentifier;

public class PipelineHuooRoleSummaryView {

  private final HuooRole huooRole;

  private final List<PipelinesAndOrgRoleGroupView> pipelinesAndOrgRoleGroupViews;

  private final Map<PipelineIdentifier, String> unassignedPipelineNumberMapForRole;

  private final Map<OrganisationRoleOwnerDto, String> unassignedOrganisationRoleOwnerNameMapForRole;

  private final long totalOrganisationRoleOwners;

  private final List<String> sortedUnassignedOrganisationNames;
  private final List<String> sortedUnassignedPipelineNumbers;

  PipelineHuooRoleSummaryView(HuooRole huooRole,
                              List<PipelinesAndOrgRoleGroupView> pipelinesAndOrgRoleGroupViews,
                              Map<PipelineIdentifier, String> unassignedPipelineNumberMapForRole,
                              Map<OrganisationRoleOwnerDto, String> unassignedOrganisationRoleOwnerNameMapForRole) {
    this.huooRole = huooRole;
    this.pipelinesAndOrgRoleGroupViews = pipelinesAndOrgRoleGroupViews.stream()
        .sorted(Comparator.comparing(PipelinesAndOrgRoleGroupView::getSortKey))
        .collect(Collectors.toList());
    this.unassignedPipelineNumberMapForRole = unassignedPipelineNumberMapForRole;
    this.unassignedOrganisationRoleOwnerNameMapForRole = unassignedOrganisationRoleOwnerNameMapForRole;

    this.sortedUnassignedOrganisationNames = this.unassignedOrganisationRoleOwnerNameMapForRole.values()
        .stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());

    this.sortedUnassignedPipelineNumbers = this.unassignedPipelineNumberMapForRole.values()
        .stream()
        .sorted(Comparator.comparing(String::toLowerCase))
        .collect(Collectors.toList());

    var assignedRoleOwnerDtoTotal = pipelinesAndOrgRoleGroupViews.stream()
        .flatMap(pipelinesAndOrgRoleGroupView -> pipelinesAndOrgRoleGroupView.getOrganisationRoleOwnerSet().stream())
        .distinct()
        .count();

    var unassignedRoleOwnerDtoTotal = unassignedOrganisationRoleOwnerNameMapForRole.size();

    this.totalOrganisationRoleOwners = assignedRoleOwnerDtoTotal + unassignedRoleOwnerDtoTotal;

  }

  public Set<PipelineIdentifier> getUnassignedPipelineIds() {
    return this.unassignedPipelineNumberMapForRole.keySet();
  }

  public Set<PipelineIdentifier> getAllPipelineIdentifiers() {
    var pipelines = new HashSet<PipelineIdentifier>();
    pipelines.addAll(this.getUnassignedPipelineIds());

    this.pipelinesAndOrgRoleGroupViews.stream()
        .flatMap(o -> o.getPipelineIdentifierSet().stream())
        .collect(Collectors.toSet());

    pipelines.addAll(
        this.pipelinesAndOrgRoleGroupViews.stream()
            .flatMap(o -> o.getPipelineIdentifierSet().stream())
            .collect(Collectors.toSet())
    );

    return pipelines;

  }

  public Set<OrganisationUnitId> getUnassignedRoleOwnerOrganisationIds() {
    return this.unassignedOrganisationRoleOwnerNameMapForRole.keySet().stream()
        .filter(o -> HuooType.PORTAL_ORG.equals(o.getHuooType()))
        .map(OrganisationRoleOwnerDto::getOrganisationUnitId)
        .collect(Collectors.toSet());
  }

  public Set<TreatyAgreement> getUnassignedRoleOwnerTreatyAgreements() {
    return this.unassignedOrganisationRoleOwnerNameMapForRole.keySet().stream()
        .filter(o -> HuooType.TREATY_AGREEMENT.equals(o.getHuooType()))
        .map(OrganisationRoleOwnerDto::getTreatyAgreement)
        .collect(Collectors.toSet());
  }

  public HuooRole getHuooRole() {
    return huooRole;
  }

  public String getRoleDisplayText() {
    return this.huooRole.getDisplayText();
  }

  public long getTotalOrganisationRoleOwners() {
    return totalOrganisationRoleOwners;
  }

  public List<PipelinesAndOrgRoleGroupView> getPipelinesAndOrgRoleGroupViews() {
    return Collections.unmodifiableList(pipelinesAndOrgRoleGroupViews);
  }

  Map<PipelineIdentifier, String> getUnassignedPipelineNumberMapForRole() {
    return Collections.unmodifiableMap(unassignedPipelineNumberMapForRole);
  }

  Map<OrganisationRoleOwnerDto, String> getUnassignedOrganisationRoleOwnerNameMapForRole() {
    return Collections.unmodifiableMap(unassignedOrganisationRoleOwnerNameMapForRole);
  }

  public List<String> getSortedUnassignedOrganisationNames() {
    return sortedUnassignedOrganisationNames;
  }

  public List<String> getSortedUnassignedPipelineNumbers() {
    return sortedUnassignedPipelineNumbers;
  }
}
