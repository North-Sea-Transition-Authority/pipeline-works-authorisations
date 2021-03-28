package uk.co.ogauthority.pwa.service.pickpwa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.service.masterpwas.ConsentedMasterPwaService;
import uk.co.ogauthority.pwa.service.masterpwas.NonConsentedPwaService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;

@RunWith(MockitoJUnitRunner.class)
public class PickedPwaRetrievalServiceTest {

  private static final int MASTER_PWA_ID = 1;

  @Mock
  private ConsentedMasterPwaService consentedMasterPwaService;

  @Mock
  private NonConsentedPwaService nonConsentedPwaService;

  @Mock
  private PwaHolderTeamService pwaHolderTeamService;


  @Mock
  private MasterPwaDetail masterPwaDetail;


  private WebUserAccount webUserAccount = new WebUserAccount(1);

  private PickedPwaRetrievalService pickedPwaRetrievalService;

  @Before
  public void setup() {
    pickedPwaRetrievalService = new PickedPwaRetrievalService(
        consentedMasterPwaService,
        nonConsentedPwaService,
        pwaHolderTeamService);

    when(masterPwaDetail.getMasterPwaId()).thenReturn(999);
  }

  @Test
  public void getPickablePwasWhereAuthorised_whenNoPickablePwasExist() {
    var options = pickedPwaRetrievalService.getPickablePwaOptions(webUserAccount);
    assertThat(options.getConsentedPickablePwas()).isEmpty();
    assertThat(options.getNonconsentedPickablePwas()).isEmpty();

    verify(consentedMasterPwaService, times(1)).getMasterPwaDetailsWhereAnyPortalOrgUnitsHolder(any());

  }

  @Test(expected = IllegalStateException.class)
  public void getPickedConsentedPwa_whenUnknownPwaSource() {
    var masterPwa = pickedPwaRetrievalService.getPickedConsentedPwa(MASTER_PWA_ID, webUserAccount);
  }


}
