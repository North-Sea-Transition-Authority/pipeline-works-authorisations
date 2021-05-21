package uk.co.ogauthority.pwa.energyportal.service.organisations;


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
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnitDetail;
import uk.co.ogauthority.pwa.energyportal.repository.organisations.PortalOrganisationGroupRepository;
import uk.co.ogauthority.pwa.energyportal.repository.organisations.PortalOrganisationUnitDetailRepository;
import uk.co.ogauthority.pwa.energyportal.repository.organisations.PortalOrganisationUnitRepository;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitDetailDto;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;

/**
 * API to interact with Portal Organisations. This service should not be polluted with business logic, and
 * should simply perform Read operations.
 */
@Service
public class PortalOrganisationsAccessor {
  private final PortalOrganisationGroupRepository organisationGroupRepository;
  private final PortalOrganisationUnitRepository organisationUnitRepository;
  private final PortalOrganisationUnitDetailRepository organisationUnitDetailRepository;

  @Autowired
  public PortalOrganisationsAccessor(PortalOrganisationGroupRepository organisationGroupRepository,
                                     PortalOrganisationUnitRepository organisationUnitRepository,
                                     PortalOrganisationUnitDetailRepository organisationUnitDetailRepository) {
    this.organisationGroupRepository = organisationGroupRepository;
    this.organisationUnitRepository = organisationUnitRepository;
    this.organisationUnitDetailRepository = organisationUnitDetailRepository;
  }

  /**
   * Find an organisation unit with matching ouId.
   *
   * @param ouId search org unit id
   * @return portal organisation unit wrapped in optional
   */
  public Optional<PortalOrganisationUnit> getOrganisationUnitById(Integer ouId) {
    return organisationUnitRepository.findById(ouId);
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
  public List<PortalOrganisationUnit> findActiveOrganisationUnitsWhereNameContains(String searchString, Pageable pageable) {
    return organisationUnitRepository.findByNameContainingIgnoreCaseAndIsActiveIsTrue(searchString, pageable);
  }

  /**
   * Returns a list of all active organisation units.
   */
  public List<PortalOrganisationUnit> getAllActiveOrganisationUnits() {
    return organisationUnitRepository.findByIsActiveIsTrue();
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

}
