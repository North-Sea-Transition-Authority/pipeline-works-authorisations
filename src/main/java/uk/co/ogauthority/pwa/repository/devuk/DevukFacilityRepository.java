package uk.co.ogauthority.pwa.repository.devuk;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;

@Repository
public interface DevukFacilityRepository extends CrudRepository<DevukFacility, Integer> {

  // Large number of results
  // Pagination is required to prevent extremely long request duration, and memory usage.
  List<DevukFacility> findAllByFacilityNameContainsIgnoreCase(Pageable pageRequest, String facilityName);

}
