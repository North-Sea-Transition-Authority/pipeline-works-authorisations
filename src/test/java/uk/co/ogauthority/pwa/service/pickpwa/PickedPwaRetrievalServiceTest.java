package uk.co.ogauthority.pwa.service.pickpwa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaAuthorisationService;

@RunWith(MockitoJUnitRunner.class)
public class PickedPwaRetrievalServiceTest {

  @Mock
  private MasterPwaAuthorisationService masterPwaAuthorisationService;

  @Mock
  private MasterPwaDetail masterPwaDetail;

  @Mock
  private MasterPwa masterPwa;

  @Mock
  private PickablePwa pickedPwa;

  private WebUserAccount webUserAccount = new WebUserAccount(1);

  private PickedPwaRetrievalService pickedPwaRetrievalService;

  @Before
  public void setup() {
    pickedPwaRetrievalService = new PickedPwaRetrievalService(
        masterPwaAuthorisationService
        );

    when(pickedPwa.getContentId()).thenReturn(100);
    when(masterPwaDetail.getMasterPwaId()).thenReturn(999);
  }

  @Test
  public void getPickablePwasWhereAuthorised_whenNoPickablePwasExist() {
    assertThat(pickedPwaRetrievalService.getPickablePwasWhereAuthorised(webUserAccount)).isEmpty();

    verify(masterPwaAuthorisationService, times(1)).getMasterPwasWhereUserIsAuthorised(webUserAccount);

  }

  @Test
  public void getPickablePwasWhereAuthorised_whenSingleMasterPwaExistsOnly() {
    when(masterPwaDetail.getReference()).thenReturn("REFERENCE");

    when(masterPwaAuthorisationService.getMasterPwasWhereUserIsAuthorised(webUserAccount)).thenReturn(
        List.of(masterPwaDetail)
    );

    var pickablePwaDtos = pickedPwaRetrievalService.getPickablePwasWhereAuthorised(webUserAccount);

    assertThat(pickablePwaDtos.size()).isEqualTo(1);
    assertThat(pickablePwaDtos.get(0).getPickablePwaString()).isEqualTo(
        PickablePwaSource.MASTER.getPickableStringPrefix() + masterPwaDetail.getMasterPwaId()
    );
    assertThat(pickablePwaDtos.get(0).getReference()).isEqualTo(masterPwaDetail.getReference());

  }

  @Test(expected = IllegalStateException.class)
  public void getPickedPwa_whenUnknownPwaSource() {
    when(pickedPwa.getPickablePwaSource()).thenReturn(PickablePwaSource.UNKNOWN);
    var masterPwa = pickedPwaRetrievalService.getPickedPwa(pickedPwa, webUserAccount);
  }

  @Test
  public void getPickedPwa_whenSourceIsMaster() {
    when(pickedPwa.getPickablePwaSource()).thenReturn(PickablePwaSource.MASTER);
    pickedPwaRetrievalService.getPickedPwa(pickedPwa, webUserAccount);
    verify(masterPwaAuthorisationService, times(1)).getMasterPwaIfAuthorised(
        pickedPwa.getContentId(),
        webUserAccount
    );
    verifyNoMoreInteractions(masterPwaAuthorisationService);
  }

}
