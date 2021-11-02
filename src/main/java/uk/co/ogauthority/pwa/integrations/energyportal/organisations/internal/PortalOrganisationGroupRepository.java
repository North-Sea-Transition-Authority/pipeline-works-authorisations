package uk.co.ogauthority.pwa.integrations.energyportal.organisations.internal;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;


public interface PortalOrganisationGroupRepository extends CrudRepository<PortalOrganisationGroup, Integer> {

  List<PortalOrganisationGroup> findByUrefValueIn(List<String> organisationGroupUrefValues);

  List<PortalOrganisationGroup> findByNameContainingIgnoreCase(String searchTerm);
}
