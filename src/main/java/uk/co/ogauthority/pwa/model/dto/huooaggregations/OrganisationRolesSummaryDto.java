package uk.co.ogauthority.pwa.model.dto.huooaggregations;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

/* Summarises all the organisation roles for a pwa and gives access by huoo type or organisation unit*/
public class OrganisationRolesSummaryDto {

  private final Map<HuooRole, Set<OrganisationRolePipelineGroupDto>> getNonPortalOrgRoleGroupsByHuooType;
  private final Map<HuooRole, Set<OrganisationRolePipelineGroupDto>> orgRolesGroupsByHuooType;
  private final Set<OrganisationUnitId> allOrganisationUnitsWithRole;

  private OrganisationRolesSummaryDto(Collection<OrganisationPipelineRoleDto> portalOrganisationPipelineRoleDtos) {
    this.allOrganisationUnitsWithRole = new HashSet<>();

    portalOrganisationPipelineRoleDtos.stream()
        .filter(OrganisationPipelineRoleDto::hasValidOrganisationRole)
        .forEach(orgPipelineRole -> allOrganisationUnitsWithRole.add(orgPipelineRole.getOrganisationUnitId()));

    Set<OrganisationRolePipelineGroupDto> allGroups = portalOrganisationPipelineRoleDtos
        // group pipeline org role instances by the overall org role
        .stream()
        .collect(groupingBy(
            OrganisationPipelineRoleDto::getOrganisationRoleDto,
            Collectors.mapping(OrganisationPipelineRoleDto::getPipelineId, Collectors.toSet())
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
      Collection<OrganisationPipelineRoleDto> organisationPipelineRoleDtos) {
    return new OrganisationRolesSummaryDto(organisationPipelineRoleDtos);
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


  @Override
  public String toString() {
    return "PwaOrganisationRolesSummaryDto{" +
        "orgRolesGroupsByHuooType=" + orgRolesGroupsByHuooType +
        ", allOrganisationUnitsWithRole=" + allOrganisationUnitsWithRole +
        '}';
  }
}
