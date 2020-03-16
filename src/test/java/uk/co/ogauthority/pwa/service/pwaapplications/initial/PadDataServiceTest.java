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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.initial.PadData;
import uk.co.ogauthority.pwa.repository.pwaapplications.initial.PadDataRepository;

@RunWith(MockitoJUnitRunner.class)
public class PadDataServiceTest {

  @Mock
  private PadDataRepository padDataRepository;

  private PadDataService padDataService;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    padDataService = new PadDataService(padDataRepository);
  }

  @Test
  public void getPadData_NoneSaved() {
    when(padDataRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.empty());
    PadData padData = padDataService.getPadData(pwaApplicationDetail);
    assertThat(padData.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(padData.getId()).isNull();
  }

  @Test
  public void getPadData_PreExisting() {
    var existingData = new PadData();
    when(padDataRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(existingData));
    PadData padData = padDataService.getPadData(pwaApplicationDetail);
    assertThat(padData).isEqualTo(existingData);
  }
}