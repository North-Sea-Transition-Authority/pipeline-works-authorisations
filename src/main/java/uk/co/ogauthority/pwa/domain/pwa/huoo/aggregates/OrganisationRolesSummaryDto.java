package uk.co.ogauthority.pwa.domain.pwa.huoo.aggregates;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooRole;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.HuooType;
import uk.co.ogauthority.pwa.domain.pwa.huoo.model.OrganisationPipelineRoleInstanceDto;

/**
 *  Summarises all organisation roles and pipeline associations and provides easy access to grouped role instances
 *  by huoo type or organisation unit.
 *  "For each HUOO role, group pipelines by the linked organisations with that role."
 **/
public class OrganisationRolesSummaryDto {

  private final Map<HuooRole, Set<OrganisationRolePipelineGroupDto>> getNonPortalOrgRoleGroupsByHuooType;
  private final Map<HuooRole, Set<OrganisationRolePipelineGroupDto>> orgRolesGroupsByHuooType;
  private final Set<OrganisationUnitId> allOrganisationUnitsWithRole;

  private OrganisationRolesSummaryDto(Collection<OrganisationPipelineRoleInstanceDto> portalOrganisationPipelineRoleInstanceDtos) {
    this.allOrganisationUnitsWithRole = new HashSet<>();

    portalOrganisationPipelineRoleInstanceDtos.stream()
        .filter(OrganisationPipelineRoleInstanceDto::hasValidOrganisationRole)
        .forEach(orgPipelineRole -> allOrganisationUnitsWithRole.add(orgPipelineRole.getOrganisationUnitId()));

    Set<OrganisationRolePipelineGroupDto> allGroups = portalOrganisationPipelineRoleInstanceDtos
        // group pipeline org role instances by the overall org role
        .stream()
        .filter(o -> HuooType.getSelectable().contains(o.getHuooType()))
        .collect(groupingBy(
            OrganisationPipelineRoleInstanceDto::getOrganisationRoleInstanceDto,
            Collectors.mapping(OrganisationPipelineRoleInstanceDto::getPipelineIdentifier, Collectors.toSet())
        ))
        // loop over each grouped entry to create a group object
        .entrySet()
        .stream()
        .map(o -> new OrganisationRolePipelineGroupDto(o.getKey(), o.getValue()))
        .collect(Collectors.toSet());

    this.orgRolesGroupsByHuooType = allGroups.stream()
        .filter(OrganisationRolePipelineGroupDto::hasValidOrganisationRole)
        .collect(groupingBy(OrganisationRolePipelineGroupDto::getHuooRole, toSet()));

    // this could be exposed to find migrated organisation groups where we have not created a new app org role due to legacy data.
    this.getNonPortalOrgRoleGroupsByHuooType = allGroups.stream()
        .filter(organisationRolePipelineGroupDto -> !organisationRolePipelineGroupDto.hasValidOrganisationRole())
        .collect(groupingBy(OrganisationRolePipelineGroupDto::getHuooRole, toSet()));
  }

  public static OrganisationRolesSummaryDto aggregateOrganisationPipelineRoles(
      Collection<OrganisationPipelineRoleInstanceDto> organisationPipelineRoleInstanceDtos) {
    return new OrganisationRolesSummaryDto(organisationPipelineRoleInstanceDtos);
  }

  public Set<OrganisationRolePipelineGroupDto> getHolderOrganisationUnitGroups() {
    return Collections.unmodifiableSet(
        this.orgRolesGroupsByHuooType.getOrDefault(HuooRole.HOLDER, Set.of())
    );
  }

  public Set<OrganisationRolePipelineGroupDto> getUserOrganisationUnitGroups() {
    return  Collections.unmodifiableSet(
        this.orgRolesGroupsByHuooType.getOrDefault(HuooRole.USER, Set.of())
    );
  }

  public Set<OrganisationRolePipelineGroupDto> getOperatorOrganisationUnitGroups() {
    return  Collections.unmodifiableSet(
        this.orgRolesGroupsByHuooType.getOrDefault(HuooRole.OPERATOR, Set.of())
    );
  }

  public Set<OrganisationRolePipelineGroupDto> getOwnerOrganisationUnitGroups() {
    return  Collections.unmodifiableSet(
        this.orgRolesGroupsByHuooType.getOrDefault(HuooRole.OWNER, Set.of())
    );
  }

  public Set<OrganisationUnitId> getAllOrganisationUnitIdsWithRole() {
    return Collections.unmodifiableSet(this.allOrganisationUnitsWithRole);
  }

  public Optional<OrganisationRolePipelineGroupDto> getOrganisationRolePipelineGroupBy(HuooRole huooRole,
                                                                                       OrganisationUnitId organisationUnitId) {
    // O(N) search where N is the number of organisation role groups with a given role.
    // Should be fine as will normally be small amounts of data easily < 100.
    return this.orgRolesGroupsByHuooType.getOrDefault(huooRole, Set.of())
        .stream()
        .filter(orgRoleGroup -> organisationUnitId.equals(orgRoleGroup.getOrganisationUnitId()))
        .findFirst();
  }

  public Set<OrganisationRolePipelineGroupDto> getHolderNonPortalOrgRoleGroups() {
    return  Collections.unmodifiableSet(
        this.getNonPortalOrgRoleGroupsByHuooType.getOrDefault(HuooRole.HOLDER, Set.of())
    );
  }

  public Set<OrganisationRolePipelineGroupDto> getUserNonPortalOrgRoleGroups() {
    return  Collections.unmodifiableSet(
        this.getNonPortalOrgRoleGroupsByHuooType.getOrDefault(HuooRole.USER, Set.of())
    );
  }

  public Set<OrganisationRolePipelineGroupDto> getOperatorNonPortalOrgRoleGroups() {
    return  Collections.unmodifiableSet(
        this.getNonPortalOrgRoleGroupsByHuooType.getOrDefault(HuooRole.OPERATOR, Set.of())
    );
  }

  public Set<OrganisationRolePipelineGroupDto> getOwnerNonPortalOrgRoleGroups() {
    return  Collections.unmodifiableSet(
        this.getNonPortalOrgRoleGroupsByHuooType.getOrDefault(HuooRole.OWNER, Set.of())
    );
  }

  @Override
  public String toString() {
    return "PwaOrganisationRolesSummaryDto{" +
        "orgRolesGroupsByHuooType=" + orgRolesGroupsByHuooType +
        ", allOrganisationUnitsWithRole=" + allOrganisationUnitsWithRole +
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
    OrganisationRolesSummaryDto that = (OrganisationRolesSummaryDto) o;
    return Objects.equals(getNonPortalOrgRoleGroupsByHuooType, that.getNonPortalOrgRoleGroupsByHuooType)
        && Objects.equals(orgRolesGroupsByHuooType, that.orgRolesGroupsByHuooType)
        && Objects.equals(allOrganisationUnitsWithRole, that.allOrganisationUnitsWithRole);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getNonPortalOrgRoleGroupsByHuooType, orgRolesGroupsByHuooType, allOrganisationUnitsWithRole);
  }
}
