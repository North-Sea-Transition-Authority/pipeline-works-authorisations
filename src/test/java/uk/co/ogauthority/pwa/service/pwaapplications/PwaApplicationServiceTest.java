package uk.co.ogauthority.pwa.service.pwaapplications;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationServiceTest {

  @Mock
  private PwaApplicationRepository pwaApplicationRepository;

  private PwaApplicationService pwaApplicationService;

  private PwaApplication pwaApplication;

  @Before
  public void setUp() {
    pwaApplicationService = new PwaApplicationService(
        pwaApplicationRepository
    );

    pwaApplication = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL).getPwaApplication();
    when(pwaApplicationRepository.findById(any())).thenReturn(Optional.of(pwaApplication));

  }

  @Test
  public void getApplicationFromId_verifyServiceInteraction() {

    pwaApplicationService.getApplicationFromId(1);
    verify(pwaApplicationRepository, times(1)).findById(1);
  }


  @Test(expected = PwaEntityNotFoundException.class)
  public void getApplicationFromId_noApplicationFound() {
    when(pwaApplicationRepository.findById(any())).thenReturn(Optional.empty());
    pwaApplicationService.getApplicationFromId(1);

  }

}
