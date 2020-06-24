package uk.co.ogauthority.pwa.model.dto.huooaggregations;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleDto;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

// do the processing of pipeline huoo data in order to create many-many groups of pipelines which share huoo organisations
public class PipelineAndOrganisationRoleGroupSummaryDto {

  private final Map<HuooRole, Set<PipelineAndOrganisationRoleGroupDto>> groupedPipelineOrgRoleGroups;

  private final Set<PipelineId> allPipelineIdsInSummary;
  private final Set<OrganisationUnitId> allOrganisationUnitIdsInSummary;

  private PipelineAndOrganisationRoleGroupSummaryDto(
      Collection<OrganisationPipelineRoleDto> portalOrganisationPipelineRoleDtos) {
    this.groupedPipelineOrgRoleGroups = new HashMap<>();
    this.allPipelineIdsInSummary = new HashSet<>();
    this.allOrganisationUnitIdsInSummary = new HashSet<>();

    portalOrganisationPipelineRoleDtos.forEach(o -> {
      allPipelineIdsInSummary.add(o.getPipelineId());
      allOrganisationUnitIdsInSummary.add(o.getOrganisationUnitId());
    });

    Map<HuooRole, Set<OrganisationPipelineRoleDto>> orgPipelineRolesByType = portalOrganisationPipelineRoleDtos.stream()
        .collect(groupingBy(
            OrganisationPipelineRoleDto::getHuooRole,
            Collectors.mapping(o -> o, Collectors.toSet())
        ));

    for (HuooRole role : HuooRole.values()) {
      var orgPipelineRoles = orgPipelineRolesByType.getOrDefault(role, Set.of());
      this.groupedPipelineOrgRoleGroups.put(role, createPipelineAndOrganisationHuooRoleGroups(orgPipelineRoles));
    }

  }

  public Set<PipelineAndOrganisationRoleGroupDto> getGroupsByHuooRole(HuooRole huooRole) {
    return Collections.unmodifiableSet(this.groupedPipelineOrgRoleGroups.getOrDefault(huooRole, Set.of()));
  }

  public static PipelineAndOrganisationRoleGroupSummaryDto aggregateOrganisationPipelineRoleDtos(
      Collection<OrganisationPipelineRoleDto> portalOrganisationPipelineRoleDtos
  ) {

    return new PipelineAndOrganisationRoleGroupSummaryDto(portalOrganisationPipelineRoleDtos);
  }

  /**
   * For a given set of organisation role and associated pipeline, find groups with the same pipeline set and same organisation with role,
   * then create and return the groups using a Set of PipelineAndOrganisationHuooRoleGroupDto.
   */
  private Set<PipelineAndOrganisationRoleGroupDto> createPipelineAndOrganisationHuooRoleGroups(
      Set<OrganisationPipelineRoleDto> organisationPipelineRoleDtos) {

    Map<PipelineId, Set<OrganisationRoleDto>> pipelineIdToOrgRolesMap = organisationPipelineRoleDtos.stream()
        .collect(groupingBy(
            OrganisationPipelineRoleDto::getPipelineId,
            Collectors.mapping(OrganisationPipelineRoleDto::getOrganisationRoleDto, Collectors.toSet())
        ));

    // using a Set as the key will provided a consistent hash provided the set is not changed mid process.
    // All sets have been set as immutable final elements and are not modifiable. Enforced with unit tests of base objects.
    // the order of elements in a set does not affect the result of equals or hashcode so the group itself will provide the consistent
    // key.
    Map<Set<OrganisationRoleDto>, Set<PipelineId>> matchingPipelinesAndOrganisationGroups = new HashMap<>();

    pipelineIdToOrgRolesMap.forEach((pipelineId, organisationRoleDtoSet) -> {
      if (matchingPipelinesAndOrganisationGroups.containsKey(organisationRoleDtoSet)) {

        matchingPipelinesAndOrganisationGroups.get(organisationRoleDtoSet)
            .add(pipelineId);

      } else {

        var newPipelineSet = new HashSet<PipelineId>();
        newPipelineSet.add(pipelineId);
        matchingPipelinesAndOrganisationGroups.put(
            organisationRoleDtoSet,
            newPipelineSet
        );

      }

    });


    return matchingPipelinesAndOrganisationGroups.entrySet()
        .stream()
        .map(entry -> new PipelineAndOrganisationRoleGroupDto(entry.getKey(), entry.getValue()))
        .collect(Collectors.toSet());

  }

  public Set<PipelineId> getAllPipelineIdsInSummary() {
    return Collections.unmodifiableSet(allPipelineIdsInSummary);
  }

  public Set<OrganisationUnitId> getAllOrganisationUnitIdsInSummary() {
    return Collections.unmodifiableSet(allOrganisationUnitIdsInSummary);
  }
}

