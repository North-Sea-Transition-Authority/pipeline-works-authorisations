package uk.co.ogauthority.pwa.service.devuk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.repository.devuk.DevukFacilityRepository;

@RunWith(MockitoJUnitRunner.class)
public class DevukFacilityServiceTest {

  @Mock
  private DevukFacilityRepository devukFacilityRepository;

  private DevukFacilityService devukFacilityService;

  @Before
  public void setUp() {
    devukFacilityService = new DevukFacilityService(devukFacilityRepository);
  }

  @Test
  public void getFacilities() {
    var facility = new DevukFacility();
    when(devukFacilityRepository.findAllByFacilityNameContainsIgnoreCase(any(), any())).thenReturn(List.of(facility));
    var result = devukFacilityService.getFacilities("");
    assertThat(result).containsExactly(facility);
  }
}