package uk.co.ogauthority.pwa.integrations.energyportal.organisations.external;


import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.domain.energyportal.organisations.model.OrganisationUnitId;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.internal.PortalOrganisationGroupRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.internal.PortalOrganisationUnitDetailRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.internal.PortalOrganisationUnitRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.internal.PortalOrganisationUnitSearchableRepository;

/**
 * API to interact with Portal Organisations. This service should not be polluted with business logic, and
 * should simply perform Read operations.
 */
@Service
public class PortalOrganisationsAccessor {
  private final PortalOrganisationGroupRepository organisationGroupRepository;
  private final PortalOrganisationUnitRepository organisationUnitRepository;
  private final PortalOrganisationUnitDetailRepository organisationUnitDetailRepository;

  private final PortalOrganisationUnitSearchableRepository organisationUnitSearchableRepository;

  @Autowired
  public PortalOrganisationsAccessor(PortalOrganisationGroupRepository organisationGroupRepository,
                                     PortalOrganisationUnitRepository organisationUnitRepository,
                                     PortalOrganisationUnitDetailRepository organisationUnitDetailRepository,
                                     PortalOrganisationUnitSearchableRepository organisationUnitSearchableRepository) {
    this.organisationGroupRepository = organisationGroupRepository;
    this.organisationUnitRepository = organisationUnitRepository;
    this.organisationUnitDetailRepository = organisationUnitDetailRepository;
    this.organisationUnitSearchableRepository = organisationUnitSearchableRepository;
  }

  /**
   * Find an organisation unit with matching ouId.
   *
   * @param ouId search org unit id
   * @return portal organisation unit wrapped in optional
   */
  @Deprecated
  public Optional<PortalOrganisationUnit> getOrganisationUnitById(Integer ouId) {
    return organisationUnitRepository.findById(ouId);
  }

  /**
   * Find an organisation unit with matching ouId.
   *
   * @param ouId search org unit id
   * @return portal organisation unit wrapped in optional
   */
  public Optional<PortalOrganisationUnit> getOrganisationUnitById(OrganisationUnitId ouId) {
    return organisationUnitRepository.findById(ouId.asInt());
  }

  /**
   * Returns true if an organisation unit with matching ouId exists in the database.
   *
   * @param ouId search org unit id
   */
  public boolean organisationUnitExistsForId(OrganisationUnitId ouId) {
    return organisationUnitRepository.existsById(ouId.asInt());
  }

  /**
   * Return a list of all active organisation units where the search term is contained within the actual name.
   *
   * @param searchString find org units with name containing this string
   * @return organisation unit Entities matching search term.
   */
  public List<PortalOrganisationUnit> findActiveOrganisationUnitsWhereNameContains(String searchString,
                                                                                   Pageable pageable) {
    return organisationUnitRepository.findByNameContainingIgnoreCaseAndIsActiveIsTrue(searchString, pageable);
  }

  public List<PortalOrganisationSearchUnit> findActiveOrganisationUnitsWhereNameOrRegNumberContains(String searchString,
                                                                                                    Pageable pageable) {
    return organisationUnitSearchableRepository.findByOrgSearchableUnitNameContainingIgnoreCaseAndIsActiveIsTrue(searchString, pageable);
  }

  /**
   * Returns a list of all active organisation units.
   */
  public List<PortalOrganisationUnit> getAllActiveOrganisationUnits() {
    return organisationUnitRepository.findByIsActiveIsTrue();
  }

  public List<PortalOrganisationSearchUnit> getAllActiveOrganisationUnitsSearch() {
    return organisationUnitSearchableRepository.findByIsActiveIsTrue();
  }

  /**
   * Returns a list of Organisation units whose ouId matches a value in the param list.
   */
  public List<PortalOrganisationUnit> getOrganisationUnitsByIdIn(Iterable<Integer> organisationUnitList) {
    return IterableUtils.toList(organisationUnitRepository.findAllById(organisationUnitList));
  }

  /**
   * Returns a list of Organisation units whose ouId matches a value in the param list.
   */
  public List<PortalOrganisationUnit> getOrganisationUnitsByOrganisationUnitIdIn(
      Iterable<OrganisationUnitId> organisationUnitList) {
    var integerIdList = IterableUtils.toList(organisationUnitList)
        .stream().map(OrganisationUnitId::asInt)
        .collect(toList());
    return getOrganisationUnitsByIdIn(integerIdList);
  }

  public List<PortalOrganisationUnitDetail> getOrganisationUnitDetails(List<PortalOrganisationUnit> unit) {
    return organisationUnitDetailRepository.findByOrganisationUnitIn(unit);
  }

  public List<OrganisationUnitDetailDto> getOrganisationUnitDetailDtos(List<PortalOrganisationUnit> organisationUnits) {
    return organisationUnitDetailRepository.findByOrganisationUnitIn(organisationUnits)
        .stream()
        .map(OrganisationUnitDetailDto::from)
        .collect(Collectors.toList());
  }

  public List<OrganisationUnitDetailDto> getOrganisationUnitDetailDtosByOrganisationUnitId(
      Collection<OrganisationUnitId> organisationUnitIds) {

    var idsAsInts = organisationUnitIds.stream()
        .map(OrganisationUnitId::asInt)
        .collect(toSet());

    return organisationUnitDetailRepository.findByOrganisationUnit_ouIdIn(idsAsInts)
        .stream()
        .map(OrganisationUnitDetailDto::from)
        .collect(Collectors.toList());
  }

  /**
   * Return a list of all organisation groups.
   */
  public List<PortalOrganisationGroup> getAllOrganisationGroups() {
    return IterableUtils.toList(organisationGroupRepository.findAll());
  }

  /**
   * Return a list of all organisation groups where the name contains the search term.
   */
  public List<PortalOrganisationGroup> getAllOrganisationGroupsWhereNameContains(String searchTerm) {
    return organisationGroupRepository.findByNameContainingIgnoreCase(searchTerm);
  }

  /**
   * Return a list of organisation groups who have a uref value matching a value in the given list.
   */
  public List<PortalOrganisationGroup> getAllOrganisationGroupsWithUrefIn(List<String> organisationGroupUref) {
    return organisationGroupRepository.findByUrefValueIn(organisationGroupUref);
  }

  /**
   * Return a list of organisation groups where their org grp id matches a value in the given list.
   */
  public List<PortalOrganisationGroup> getOrganisationGroupsWhereIdIn(List<Integer> organisationGroupId) {
    return IterableUtils.toList(organisationGroupRepository.findAllById(organisationGroupId));
  }



  /**
   * Get the PortalOrganisationGroup for the specified id.
   * @param orgGrpId id of the PortalOrganisationGroup
   * @return optional of the PortalOrganisationGroup with the specified id
   */
  public Optional<PortalOrganisationGroup> getOrganisationGroupById(Integer orgGrpId) {
    return organisationGroupRepository.findById(orgGrpId);
  }

  /**
   * Returns a list of organisation units which belong to organisation groups in the provided list.
   */
  public List<PortalOrganisationUnit> getOrganisationUnitsForOrganisationGroupsIn(
      Collection<PortalOrganisationGroup> organisationGroups) {
    return organisationUnitRepository.findByPortalOrganisationGroupIn(List.copyOf(organisationGroups));
  }

  /**
   * Returns a list of Active organisation units which belong to organisation groups in the provided list.
   */
  public List<PortalOrganisationUnit> getActiveOrganisationUnitsForOrganisationGroupsIn(
      Collection<PortalOrganisationGroup> organisationGroups) {
    return organisationUnitRepository.findByPortalOrganisationGroupInAndIsActiveIsTrue(List.copyOf(organisationGroups));
  }

  public List<PortalOrganisationSearchUnit> getSearchableOrganisationUnitsForOrganisationGroupsIn(
      Collection<PortalOrganisationGroup> organisationGroups) {
    var groupList = organisationGroups.stream()
        .map(PortalOrganisationGroup::getOrgGrpId)
        .collect(Collectors.toList());
    return organisationUnitSearchableRepository.findByGroupIdInAndIsActiveIsTrue(groupList);
  }


}
