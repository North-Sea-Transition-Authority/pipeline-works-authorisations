package uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.internal.DevukFacilityRepository;

@Service
public class DevukFacilityService {

  private final DevukFacilityRepository devukFacilityRepository;

  @Autowired
  public DevukFacilityService(DevukFacilityRepository devukFacilityRepository) {
    this.devukFacilityRepository = devukFacilityRepository;
  }

  public List<DevukFacility> getFacilities(String searchTerm) {
    return devukFacilityRepository.findAllByFacilityNameContainsIgnoreCase(PageRequest.of(0, 15), searchTerm);
  }

  public List<DevukFacility> getFacilitiesInIds(List<String> ids) {
    var intIds = ids.stream()
        .filter(NumberUtils::isParsable)
        .map(Integer::parseInt)
        .collect(Collectors.toList());
    return devukFacilityRepository.findAllByIdIn(intIds);
  }

}
