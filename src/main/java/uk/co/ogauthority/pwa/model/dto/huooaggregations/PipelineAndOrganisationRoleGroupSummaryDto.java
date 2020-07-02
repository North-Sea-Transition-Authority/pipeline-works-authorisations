package uk.co.ogauthority.pwa.model.dto.huooaggregations;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationPipelineRoleInstanceDto;
import uk.co.ogauthority.pwa.model.dto.consents.OrganisationRoleOwnerDto;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;

/**
 * Summarises all the organisation roles for a pwa groups pipelines are organisations where the role relationships are the same
 * "For each HUOO role, when a set of pipelines has the same set of organisations with the role, group the sets together."
 **/
public class PipelineAndOrganisationRoleGroupSummaryDto {

  private final Map<HuooRole, Set<PipelineAndOrganisationRoleGroupDto>> groupedPipelineOrgRoleGroups;

  private final Set<PipelineId> allPipelineIdsInSummary;
  private final Set<OrganisationRoleOwnerDto> allOrganisationRoleOwnersInSummary;
  private final Map<HuooRole, Set<PipelineId>> pipelinesByAssociatedRole;
  private final Map<HuooRole, Set<OrganisationRoleOwnerDto>> organisationsRoleOwnersByAssociatedRole;

  // Private constructor. This is doing the aggregation of whatever OrganisationPipelineRoleDto are given,
  // but constructor are not very semantic when reading code. We also done want arbitrary constructions of this object to take place.
  // Static method used to aid code readability and prevent arbitrary object creations.
  private PipelineAndOrganisationRoleGroupSummaryDto(
      Collection<OrganisationPipelineRoleInstanceDto> portalOrganisationPipelineRoleInstanceDtos) {
    this.groupedPipelineOrgRoleGroups = new HashMap<>();
    this.allPipelineIdsInSummary = new HashSet<>();
    this.allOrganisationRoleOwnersInSummary = new HashSet<>();
    this.pipelinesByAssociatedRole = new HashMap<>();
    this.organisationsRoleOwnersByAssociatedRole = new HashMap<>();

    // populate simple maps and sets of pipelines and orgs upfront
    createPipelineAndOrganisationsRoleMaterialisations(portalOrganisationPipelineRoleInstanceDtos);

    // Do an initial grouping of organisation role instances by HUOO role
    Map<HuooRole, Set<OrganisationPipelineRoleInstanceDto>> orgPipelineRolesByType = portalOrganisationPipelineRoleInstanceDtos.stream()
        .collect(groupingBy(
            OrganisationPipelineRoleInstanceDto::getHuooRole,
            Collectors.mapping(o -> o, Collectors.toSet())
        ));

    // Per HUOO role, aggregated all the pipeline sets and organisation sets which have the same relationship, then store in map.
    for (HuooRole role : HuooRole.values()) {
      var orgPipelineRoles = orgPipelineRolesByType.getOrDefault(role, Set.of());
      this.groupedPipelineOrgRoleGroups.put(role, createPipelineAndOrganisationHuooRoleGroups(orgPipelineRoles));
    }

  }


  private void createPipelineAndOrganisationsRoleMaterialisations(
      Collection<OrganisationPipelineRoleInstanceDto> portalOrganisationPipelineRoleInstanceDtos) {

    // seed maps with all roles
    HuooRole.stream()
        .forEach(role -> {
          this.pipelinesByAssociatedRole.put(role, new HashSet<>());
          this.organisationsRoleOwnersByAssociatedRole.put(role, new HashSet<>());
        });

    // loop through non-aggregated organisation roles for pipelines and extract distinct organisation units and
    // distinct top level pipeline ids.
    portalOrganisationPipelineRoleInstanceDtos.forEach(o -> {
      this.allPipelineIdsInSummary.add(o.getPipelineId());
      this.pipelinesByAssociatedRole.get(o.getHuooRole()).add(o.getPipelineId());
      this.allOrganisationRoleOwnersInSummary.add(o.getOrganisationRoleInstanceDto().getOrganisationRoleOwnerDto());
      this.organisationsRoleOwnersByAssociatedRole.get(o.getHuooRole()).add(
          o.getOrganisationRoleInstanceDto().getOrganisationRoleOwnerDto());
    });
  }

  /**
   * For a given set of organisation roles and associated pipelines,
   * find groups with the relationship between pipeline and organisation role.
   * Store theses distinct groups in an object and return the complete set.
   */
  private Set<PipelineAndOrganisationRoleGroupDto> createPipelineAndOrganisationHuooRoleGroups(
      Set<OrganisationPipelineRoleInstanceDto> organisationPipelineRoleInstanceDtos) {

    // For each pipeline map the associated organisation roles
    Map<PipelineId, Set<OrganisationRoleOwnerDto>> pipelineIdToOrgRolesMap = organisationPipelineRoleInstanceDtos.stream()
        .collect(groupingBy(
            OrganisationPipelineRoleInstanceDto::getPipelineId,
            Collectors.mapping(
                OrganisationPipelineRoleInstanceDto::getOrganisationRoleOwnerDto,
                Collectors.toSet())
        ));

    // Using a Set as the key will provide a consistent hash provided the set is not changed mid process.
    // All sets have been set as immutable final elements and are not modifiable. Enforced with unit tests of base objects.
    // the order of elements in a set does not affect the result of equals or hashcode so the group itself will provide the consistent
    // key.
    Map<Set<OrganisationRoleOwnerDto>, Set<PipelineId>> matchingPipelinesAndOrganisationGroups = new HashMap<>();

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
      Collection<OrganisationPipelineRoleInstanceDto> portalOrganisationPipelineRoleInstanceDtos
  ) {
    return new PipelineAndOrganisationRoleGroupSummaryDto(portalOrganisationPipelineRoleInstanceDtos);
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
  public Set<OrganisationRoleOwnerDto> getAllOrganisationRoleOwnersInSummary() {
    return Collections.unmodifiableSet(allOrganisationRoleOwnersInSummary);
  }

  public Set<PipelineId> getPipelineIdsWithAssignedRole(HuooRole huooRole) {
    return Collections.unmodifiableSet(this.pipelinesByAssociatedRole.get(huooRole));
  }

  public Set<OrganisationRoleOwnerDto> getOrganisationRoleOwnersWithAssignedRole(HuooRole huooRole) {
    return Collections.unmodifiableSet(this.organisationsRoleOwnersByAssociatedRole.get(huooRole));
  }
}

