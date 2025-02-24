package uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.internal.DevukFacilityRepository;

@ExtendWith(MockitoExtension.class)
class DevukFacilityServiceTest {

  @Mock
  private DevukFacilityRepository devukFacilityRepository;

  private DevukFacilityService devukFacilityService;

  @BeforeEach
  void setUp() {
    devukFacilityService = new DevukFacilityService(devukFacilityRepository);
  }

  @Test
  void getFacilities_serviceInteraction() {
    var facility = new DevukFacility();
    when(devukFacilityRepository.findAllByFacilityNameContainsIgnoreCase(any(), any())).thenReturn(List.of(facility));
    var result = devukFacilityService.getFacilities("");
    assertThat(result).containsExactly(facility);
  }

  @Test
  void getFacilitiesInIds_ensureOnlyIntegersPassed() {
    List<String> idList = List.of("1", "two", "3");
    devukFacilityService.getFacilitiesInIds(idList);
    verify(devukFacilityRepository, times(1)).findAllByIdIn(List.of(1, 3));
  }
}