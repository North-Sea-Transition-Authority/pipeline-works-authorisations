package uk.co.ogauthority.pwa.energyportal.repository.organisations;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;


public interface PortalOrganisationGroupRepository extends CrudRepository<PortalOrganisationGroup, Integer> {

  List<PortalOrganisationGroup> findByUrefValueIn(List<String> organisationGroupUrefValues);

  List<PortalOrganisationGroup> findByNameContainingIgnoreCase(String searchTerm);
}
