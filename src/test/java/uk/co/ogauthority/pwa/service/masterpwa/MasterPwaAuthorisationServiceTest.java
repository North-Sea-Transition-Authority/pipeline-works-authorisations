package uk.co.ogauthority.pwa.service.masterpwa;


import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.repository.masterpwa.MasterPwaDetailRepository;
import uk.co.ogauthority.pwa.repository.masterpwa.MasterPwaRepository;

@RunWith(MockitoJUnitRunner.class)
public class MasterPwaAuthorisationServiceTest {

  private static final int MASTER_PWA_ID = 10;

  @Mock
  private MasterPwaRepository masterPwaRepository;

  @Mock
  private MasterPwaDetailRepository masterPwaDetailRepository;

  private MasterPwaAuthorisationService masterPwaAuthorisationService;

  private WebUserAccount webUserAccount = new WebUserAccount(1);

  @Mock
  private MasterPwa masterPwa;

  @Before
  public void setup() {
    masterPwaAuthorisationService = new MasterPwaAuthorisationService(
        masterPwaRepository,
        masterPwaDetailRepository
    );

    when(masterPwaRepository.findById(MASTER_PWA_ID)).thenReturn(Optional.of(masterPwa));
  }


  @Test
  public void getMasterPwaIfAuthorised_verifyServiceInteractions() {
    masterPwaAuthorisationService.getMasterPwaIfAuthorised(MASTER_PWA_ID, webUserAccount);
    verify(masterPwaRepository, times(1)).findById(MASTER_PWA_ID);
    verifyNoMoreMockInteractions();
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getMasterPwaIfAuthorised_whenMasterPwaNotFound() {
    masterPwaAuthorisationService.getMasterPwaIfAuthorised(9999, webUserAccount);
  }

  private void verifyNoMoreMockInteractions(){
    verifyNoMoreInteractions(masterPwaRepository, masterPwaDetailRepository);
  }




}
