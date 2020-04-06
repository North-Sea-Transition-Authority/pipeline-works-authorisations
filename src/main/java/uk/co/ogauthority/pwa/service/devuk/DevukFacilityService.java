package uk.co.ogauthority.pwa.service.devuk;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.repository.devuk.DevukFacilityRepository;

@Service
public class DevukFacilityService {

  private final DevukFacilityRepository devukFacilityRepository;

  @Autowired
  public DevukFacilityService(DevukFacilityRepository devukFacilityRepository) {
    this.devukFacilityRepository = devukFacilityRepository;
  }

  // TODO: PWA-385 Change method to take a string, passing in to repository call.
  public List<DevukFacility> getFacilities() {
    return devukFacilityRepository.findAllByFacilityNameContainsIgnoreCase(PageRequest.of(0, 15), "");
  }

}
