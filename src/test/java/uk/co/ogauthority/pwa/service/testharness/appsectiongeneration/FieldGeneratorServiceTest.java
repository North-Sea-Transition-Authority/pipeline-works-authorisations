package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.devuk.PadField;
import uk.co.ogauthority.pwa.repository.devuk.PadFieldRepository;
import uk.co.ogauthority.pwa.service.devuk.DevukFieldService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class FieldGeneratorServiceTest {


  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  @Mock
  private PadFieldRepository padFieldRepository;

  @Mock
  private DevukFieldService devukFieldService;

  FieldGeneratorService fieldGeneratorService;

  @Before
  public void setup(){
    fieldGeneratorService = new FieldGeneratorService(
        pwaApplicationDetailService, padFieldRepository, devukFieldService);
  }


  @Test
  public void generatePadFields_verifyEntitySaved()  {

    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);

    fieldGeneratorService.generatePadFields(pwaApplicationDetail);

    verify(padFieldRepository).save(any(PadField.class));
  }

  
  
  
}
