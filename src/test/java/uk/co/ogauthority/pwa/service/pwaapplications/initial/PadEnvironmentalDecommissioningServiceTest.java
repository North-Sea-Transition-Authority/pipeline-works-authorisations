package uk.co.ogauthority.pwa.service.pwaapplications.initial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.initial.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.repository.pwaapplications.initial.PadEnvironmentalDecommissioningRepository;

@RunWith(MockitoJUnitRunner.class)
public class PadEnvironmentalDecommissioningServiceTest {

  @Mock
  private PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository;

  private PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    padEnvironmentalDecommissioningService = new PadEnvironmentalDecommissioningService(padEnvironmentalDecommissioningRepository);
  }

  @Test
  public void noneSaved() {
    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.empty());
    PadEnvironmentalDecommissioning padEnvironmentalDecommissioning = padEnvironmentalDecommissioningService.getEnvDecomData(pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning.getId()).isNull();
  }

  @Test
  public void preExisting() {
    var existingData = new PadEnvironmentalDecommissioning();
    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(existingData));
    PadEnvironmentalDecommissioning padEnvironmentalDecommissioning = padEnvironmentalDecommissioningService.getEnvDecomData(pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning).isEqualTo(existingData);
  }
}