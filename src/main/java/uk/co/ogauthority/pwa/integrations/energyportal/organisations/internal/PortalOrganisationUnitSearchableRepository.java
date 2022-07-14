package uk.co.ogauthority.pwa.integrations.energyportal.organisations.internal;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationSearchUnit;

public interface PortalOrganisationUnitSearchableRepository extends CrudRepository<PortalOrganisationSearchUnit, Integer> {

  List<PortalOrganisationSearchUnit> findByIsActiveIsTrue();

  List<PortalOrganisationSearchUnit> findByGroupIdInAndIsActiveIsTrue(List<Integer> groupId);

  List<PortalOrganisationSearchUnit> findByOrgSearchableUnitNameContainingIgnoreCaseAndIsActiveIsTrue(String searchTerm, Pageable pageable);
}
