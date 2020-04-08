package uk.co.ogauthority.pwa.energyportal.service.organisations;


import java.util.List;
import java.util.Optional;
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
  public PortalOrganisationsAccessor(
      PortalOrganisationGroupRepository organisationGroupRepository,
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
   * Return a list of  all organisation units where the search term is contained within the actual name.
   *
   * @param searchString find org units with name containing this string
   * @return organisation unit Entities matching search term.
   */
  public List<PortalOrganisationUnit> findOrganisationUnitsWhereNameContains(String searchString) {
    return organisationUnitRepository.findByNameContainingIgnoreCase(searchString);
  }

  /**
   * Return a list of  all organisation units where the search term is contained within the actual name.
   *
   * @param searchString find org units with name containing this string
   * @return organisation unit Entities matching search term.
   */
  public List<PortalOrganisationUnit> findOrganisationUnitsWhereNameContains(String searchString, Pageable pageable) {
    return organisationUnitRepository.findByNameContainingIgnoreCase(searchString, pageable);
  }

  /**
   * Returns a list of all organisation units.
   */
  public List<PortalOrganisationUnit> getAllOrganisationUnits() {
    return organisationUnitRepository.findAll();
  }

  /**
   * Returns a list of Organisation units whose ouId matches a value in the param list.
   */
  public List<PortalOrganisationUnit> getOrganisationUnitsByIdIn(Iterable<Integer> organisationUnitList) {
    return IterableUtils.toList(organisationUnitRepository.findAllById(organisationUnitList));
  }

  public List<PortalOrganisationUnitDetail> getOrganisationUnitDetails(List<PortalOrganisationUnit> unit) {
    return organisationUnitDetailRepository.getAllByOrganisationUnitIn(unit);
  }

  /**
   * Return a list of all organisation groups.
   */
  public List<PortalOrganisationGroup> getAllOrganisationGroups() {
    return  IterableUtils.toList(organisationGroupRepository.findAll());
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
  public List<PortalOrganisationUnit> getOrganisationUnitsForOrganisationGroupsIn(List<PortalOrganisationGroup> organisationGroups) {
    return organisationUnitRepository.findByPortalOrganisationGroupIn(organisationGroups);
  }

}
