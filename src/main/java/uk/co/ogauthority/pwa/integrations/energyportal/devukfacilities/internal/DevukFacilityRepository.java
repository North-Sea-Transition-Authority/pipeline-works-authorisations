package uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.internal;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external.DevukFacility;

@Repository
public interface DevukFacilityRepository extends CrudRepository<DevukFacility, Integer> {

  // Large number of results
  // Pagination is required to prevent extremely long request duration, and memory usage.
  List<DevukFacility> findAllByFacilityNameContainsIgnoreCase(Pageable pageRequest, String facilityName);

  List<DevukFacility> findAllByIdIn(List<Integer> ids);

}
