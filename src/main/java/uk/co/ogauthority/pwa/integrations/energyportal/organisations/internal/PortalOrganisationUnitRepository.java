package uk.co.ogauthority.pwa.integrations.energyportal.organisations.internal;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;

public interface PortalOrganisationUnitRepository extends CrudRepository<PortalOrganisationUnit, Integer> {

  @EntityGraph(attributePaths = "portalOrganisationGroup")
  List<PortalOrganisationUnit> findByIsActiveIsTrue();

  List<PortalOrganisationUnit> findByNameContainingIgnoreCaseAndIsActiveIsTrue(String searchTerm, Pageable pageable);

  List<PortalOrganisationUnit> findByPortalOrganisationGroupIn(List<PortalOrganisationGroup> organisationGroups);

  List<PortalOrganisationUnit> findByPortalOrganisationGroupInAndIsActiveIsTrue(List<PortalOrganisationGroup> organisationGroups);
}
