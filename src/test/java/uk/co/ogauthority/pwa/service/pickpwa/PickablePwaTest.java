package uk.co.ogauthority.pwa.service.pickpwa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;

@RunWith(MockitoJUnitRunner.class)
public class PickablePwaTest {

  private static final String MASTER_PWA_PREFIX = PickablePwaSource.MASTER.getPickableStringPrefix();
  private static final int CONTENT_ID = 1;

  private static final int MASTER_PWA_ID = 20;

  @Mock
  private MasterPwaDetail masterPwaDetail;

  @Before
  public void setup(){
    when(masterPwaDetail.getMasterPwaId()).thenReturn(MASTER_PWA_ID);
  }


  @Test
  public void pickablePwa_usingInputStringOnly_whenMasterPrefix(){
    var constructorString = MASTER_PWA_PREFIX + CONTENT_ID;
    var pickablePwa = new PickablePwa(constructorString);
    assertThat(pickablePwa.getContentId()).isEqualTo(CONTENT_ID);
    assertThat(pickablePwa.getPickablePwaSource()).isEqualTo(PickablePwaSource.MASTER);
    assertThat(pickablePwa.getPickablePwaString()).isEqualTo(constructorString);

  }

  @Test(expected = IllegalArgumentException.class)
  public void pickablePwa_usingInputStringOnly_whenUnsupportedFormat(){
    var constructorString = "RANDOM_STRING/1/1/1";
    new PickablePwa(constructorString);
  }

  @Test
  public void pickablePwa_usingMasterPwaDetailOnly(){
    var pickablePwa = new PickablePwa(masterPwaDetail);
    assertThat(pickablePwa.getContentId()).isEqualTo(MASTER_PWA_ID);
    assertThat(pickablePwa.getPickablePwaSource()).isEqualTo(PickablePwaSource.MASTER);
    assertThat(pickablePwa.getPickablePwaString()).isEqualTo(MASTER_PWA_PREFIX + masterPwaDetail.getMasterPwaId());
  }


}
