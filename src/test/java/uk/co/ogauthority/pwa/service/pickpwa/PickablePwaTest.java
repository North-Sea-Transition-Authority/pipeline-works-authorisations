package uk.co.ogauthority.pwa.service.pickpwa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;

@RunWith(MockitoJUnitRunner.class)
public class PickablePwaTest {

  private static final String MASTER_PWA_PREFIX = "MASTER_PWA/";
  private static final String MIGRATION_PWA_PREFIX = "MIGRATION_PWA/";
  private static final int CONTENT_ID = 1;

  private static final int MIGRATION_PWA_ID = 10;
  private static final int MASTER_PWA_ID = 20;

  @Mock
  private MigrationMasterPwa migrationMasterPwa;

  @Mock
  private MasterPwaDetail masterPwaDetail;

  @Before
  public void setup(){
    when(migrationMasterPwa.getPadId()).thenReturn(MIGRATION_PWA_ID);
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

  @Test
  public void pickablePwa_usingInputStringOnly_whenMigrationPrefix(){
    var constructorString = MIGRATION_PWA_PREFIX + CONTENT_ID;
    var pickablePwa = new PickablePwa(constructorString);
    assertThat(pickablePwa.getContentId()).isEqualTo(CONTENT_ID);
    assertThat(pickablePwa.getPickablePwaSource()).isEqualTo(PickablePwaSource.MIGRATION);
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

  @Test
  public void pickablePwa_usingMigrationMasterPwaOnly(){
    var pickablePwa = new PickablePwa(migrationMasterPwa);
    assertThat(pickablePwa.getContentId()).isEqualTo(MIGRATION_PWA_ID);
    assertThat(pickablePwa.getPickablePwaSource()).isEqualTo(PickablePwaSource.MIGRATION);
    assertThat(pickablePwa.getPickablePwaString()).isEqualTo(MIGRATION_PWA_PREFIX + migrationMasterPwa.getPadId());
  }
}
