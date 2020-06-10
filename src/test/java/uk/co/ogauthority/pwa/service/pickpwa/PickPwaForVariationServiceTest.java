package uk.co.ogauthority.pwa.service.pickpwa;

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
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.workflow.PwaApplicationCreationService;

@RunWith(MockitoJUnitRunner.class)
public class PickPwaForVariationServiceTest {

  @Mock
  private PickedPwaRetrievalService pickedPwaRetrievalService;

  @Mock
  private PwaApplicationCreationService pwaApplicationCreationService;

  @Mock
  private PickablePwa pickablePwa;

  @Mock
  private MasterPwa masterPwa;

  private WebUserAccount webUserAccount = new WebUserAccount(1);

  private PickPwaForVariationService pickPwaForVariationService;

  @Before
  public void setup() {
    pickPwaForVariationService = new PickPwaForVariationService(
        pickedPwaRetrievalService,
        pwaApplicationCreationService
    );

    when(pwaApplicationCreationService.createVariationPwaApplication(any(), any(), any())).thenReturn(new PwaApplicationDetail());
    when(pickedPwaRetrievalService.getPickedPwa(any(), any())).thenReturn(masterPwa);
  }

  @Test
  public void createPwaVariationApplicationForPickedPwa_verifyServiceInteractions(){

    pickPwaForVariationService.createPwaVariationApplicationForPickedPwa(pickablePwa, PwaApplicationType.CAT_1_VARIATION, webUserAccount);
    verify(pickedPwaRetrievalService, times(1)).getPickedPwa(pickablePwa, webUserAccount);
    verify(pwaApplicationCreationService, times(1)).createVariationPwaApplication(webUserAccount, masterPwa, PwaApplicationType.CAT_1_VARIATION);
  }

}
