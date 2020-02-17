package uk.co.ogauthority.pwa.energyportal.repository.organisations;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;

public interface PortalOrganisationUnitRepository extends CrudRepository<PortalOrganisationUnit, Integer> {

  @EntityGraph(attributePaths = "portalOrganisationGroup")
  List<PortalOrganisationUnit> findAll();

  List<PortalOrganisationUnit> findByNameContainingIgnoreCase(String searchTerm);

  List<PortalOrganisationUnit> findByPortalOrganisationGroupIn(List<PortalOrganisationGroup> organisationGroups);
}
