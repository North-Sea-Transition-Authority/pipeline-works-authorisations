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

/**
 * Summarises all the organisation roles for a pwa groups pipelines are organisations where the role relationships are the same
 * "For each HUOO role, when a set of pipelines has the same set of organisations with the role, group the sets together."
 **/
public class PipelineAndOrganisationRoleGroupSummaryDto {

  private final Map<HuooRole, Set<PipelineAndOrganisationRoleGroupDto>> groupedPipelineOrgRoleGroups;

  private final Set<PipelineId> allPipelineIdsInSummary;
  private final Set<OrganisationUnitId> allOrganisationUnitIdsInSummary;
  private final Map<HuooRole, Set<PipelineId>> pipelinesByAssociatedRole;
  private final Map<HuooRole, Set<OrganisationUnitId>> organisationsByAssociatedRole;

  // Private constructor. This is doing the aggregation of whatever OrganisationPipelineRoleDto are given,
  // but constructor are not very semantic when reading code. We also done want arbitrary constructions of this object to take place.
  // Static method used to aid code readability and prevent arbitrary object creations.
  private PipelineAndOrganisationRoleGroupSummaryDto(
      Collection<OrganisationPipelineRoleDto> portalOrganisationPipelineRoleDtos) {
    this.groupedPipelineOrgRoleGroups = new HashMap<>();
    this.allPipelineIdsInSummary = new HashSet<>();
    this.allOrganisationUnitIdsInSummary = new HashSet<>();
    this.pipelinesByAssociatedRole = new HashMap<>();
    this.organisationsByAssociatedRole = new HashMap<>();

    // populate simple maps and sets of pipelines and orgs upfront
    createPipelineAndOrganisationsRoleMaterialisations(portalOrganisationPipelineRoleDtos);

    // Do an initial grouping of organisation role instances by HUOO role
    Map<HuooRole, Set<OrganisationPipelineRoleDto>> orgPipelineRolesByType = portalOrganisationPipelineRoleDtos.stream()
        .collect(groupingBy(
            OrganisationPipelineRoleDto::getHuooRole,
            Collectors.mapping(o -> o, Collectors.toSet())
        ));

    // Per HUOO role, aggregated all the pipeline sets and organisation sets which have the same relationship, then store in map.
    for (HuooRole role : HuooRole.values()) {
      var orgPipelineRoles = orgPipelineRolesByType.getOrDefault(role, Set.of());
      this.groupedPipelineOrgRoleGroups.put(role, createPipelineAndOrganisationHuooRoleGroups(orgPipelineRoles));
    }

  }


  private void createPipelineAndOrganisationsRoleMaterialisations(
      Collection<OrganisationPipelineRoleDto> portalOrganisationPipelineRoleDtos) {

    // seed maps with all roles
    HuooRole.stream()
        .forEach(role -> {
          this.pipelinesByAssociatedRole.put(role, new HashSet<>());
          this.organisationsByAssociatedRole.put(role, new HashSet<>());
        });

    // loop through non-aggregated organisation roles for pipelines and extract distinct organisation units and
    // distinct top level pipeline ids.
    portalOrganisationPipelineRoleDtos.forEach(o -> {
      this.allPipelineIdsInSummary.add(o.getPipelineId());
      this.pipelinesByAssociatedRole.get(o.getHuooRole()).add(o.getPipelineId());
      this.allOrganisationUnitIdsInSummary.add(o.getOrganisationUnitId());
      this.organisationsByAssociatedRole.get(o.getHuooRole()).add(o.getOrganisationUnitId());
    });
  }

  /**
   * For a given set of organisation roles and associated pipelines,
   * find groups with the relationship between pipeline and organisation role.
   * Store theses distinct groups in an object and return the complete set.
   */
  private Set<PipelineAndOrganisationRoleGroupDto> createPipelineAndOrganisationHuooRoleGroups(
      Set<OrganisationPipelineRoleDto> organisationPipelineRoleDtos) {

    // For each pipeline map the associated organisaiton roles
    Map<PipelineId, Set<OrganisationRoleDto>> pipelineIdToOrgRolesMap = organisationPipelineRoleDtos.stream()
        .collect(groupingBy(
            OrganisationPipelineRoleDto::getPipelineId,
            Collectors.mapping(OrganisationPipelineRoleDto::getOrganisationRoleDto, Collectors.toSet())
        ));

    // Using a Set as the key will provide a consistent hash provided the set is not changed mid process.
    // All sets have been set as immutable final elements and are not modifiable. Enforced with unit tests of base objects.
    // the order of elements in a set does not affect the result of equals or hashcode so the group itself will provide the consistent
    // key.
    Map<Set<OrganisationRoleDto>, Set<PipelineId>> matchingPipelinesAndOrganisationGroups = new HashMap<>();

    // For each pipeline and set of org roles, add the org role set as a key, then add the pipelines to the associated set.
    // Once the loop is complete, all the pipelines which have the same associated organisation roles will be grouped together.
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

    // loop through the map and create an immutable object instance for each group entry to prvent subsequent mutation of the groups.
    return matchingPipelinesAndOrganisationGroups.entrySet()
        .stream()
        .map(entry -> new PipelineAndOrganisationRoleGroupDto(entry.getKey(), entry.getValue()))
        .collect(Collectors.toSet());

  }

  /**
   * For a given huoo role, find the pipeline and organisation groups.
   */
  public Set<PipelineAndOrganisationRoleGroupDto> getGroupsByHuooRole(HuooRole huooRole) {
    return Collections.unmodifiableSet(this.groupedPipelineOrgRoleGroups.getOrDefault(huooRole, Set.of()));
  }

  /**
   * Provide more descriptive construction method for aggregate object.
   */
  public static PipelineAndOrganisationRoleGroupSummaryDto aggregateOrganisationPipelineRoleDtos(
      Collection<OrganisationPipelineRoleDto> portalOrganisationPipelineRoleDtos
  ) {
    return new PipelineAndOrganisationRoleGroupSummaryDto(portalOrganisationPipelineRoleDtos);
  }

  /**
   * Convenience method to access all the distinct pipeline Ids contained within this aggregated summary object.
   */
  public Set<PipelineId> getAllPipelineIdsInSummary() {
    return Collections.unmodifiableSet(allPipelineIdsInSummary);
  }

  /**
   * Convenience method to access all the distinct organisation unit Ids contained within this aggregated summary object.
   */
  public Set<OrganisationUnitId> getAllOrganisationUnitIdsInSummary() {
    return Collections.unmodifiableSet(allOrganisationUnitIdsInSummary);
  }

  public Set<PipelineId> getPipelineIdsWithAssignedRole(HuooRole huooRole) {
    return Collections.unmodifiableSet(this.pipelinesByAssociatedRole.get(huooRole));
  }

  public Set<OrganisationUnitId> getOrganisationUnitIdsWithAssignedRole(HuooRole huooRole) {
    return Collections.unmodifiableSet(this.organisationsByAssociatedRole.get(huooRole));
  }
}

