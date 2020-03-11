package uk.co.ogauthority.pwa.service.pickpwa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;
import uk.co.ogauthority.pwa.service.masterpwa.MasterPwaAuthorisationService;
import uk.co.ogauthority.pwa.service.migration.PipelineAuthorisationMigrationService;

@RunWith(MockitoJUnitRunner.class)
public class PickedPwaRetrievalAndMigrationServiceTest {

  @Mock
  private MasterPwaAuthorisationService masterPwaAuthorisationService;

  @Mock
  private PipelineAuthorisationMigrationService pipelineAuthorisationMigrationService;

  private MigrationMasterPwa migrationMasterPwa;

  @Mock
  private MasterPwaDetail masterPwaDetail;

  @Mock
  private PickablePwa pickedPwa;

  private WebUserAccount webUserAccount = new WebUserAccount(1);

  private PickedPwaRetrievalAndMigrationService pickedPwaRetrievalAndMigrationService;

  @Before
  public void setup() {
    pickedPwaRetrievalAndMigrationService = new PickedPwaRetrievalAndMigrationService(
        masterPwaAuthorisationService,
        pipelineAuthorisationMigrationService
    );
  }

  @Test
  public void getPickablePwasWhereAuthorised_whenNoPickablePwasExist() {
    assertThat(pickedPwaRetrievalAndMigrationService.getPickablePwasWhereAuthorised(webUserAccount)).isEmpty();

    verify(masterPwaAuthorisationService, times(1)).getMasterPwasWhereUserIsAuthorised(webUserAccount);
    verify(pipelineAuthorisationMigrationService, times(1)).getMasterPwasWhereUserIsAuthorisedAndNotMigrated(
        webUserAccount);
  }

  @Test
  public void getPickablePwasWhereAuthorised_whenSingleMigrationPwaExistsOnly() {
    migrationMasterPwa = new MigrationMasterPwa();
    migrationMasterPwa.setPadId(1);
    migrationMasterPwa.setReference("REFERENCE");

    when(pipelineAuthorisationMigrationService.getMasterPwasWhereUserIsAuthorisedAndNotMigrated(
        webUserAccount)).thenReturn(
        List.of(migrationMasterPwa)
    );

    var pickablePwaDtos = pickedPwaRetrievalAndMigrationService.getPickablePwasWhereAuthorised(webUserAccount);

    assertThat(pickablePwaDtos.size()).isEqualTo(1);
    assertThat(pickablePwaDtos.get(0).getPickablePwaString()).isEqualTo(
        PickablePwa.getMigrationPwaPrefix() + migrationMasterPwa.getPadId()
    );
    assertThat(pickablePwaDtos.get(0).getReference()).isEqualTo(migrationMasterPwa.getReference());

  }

  @Test
  public void getPickablePwasWhereAuthorised_whenSingleMasterPwaExistsOnly() {
    when(masterPwaDetail.getReference()).thenReturn("REFERENCE");
    when(masterPwaDetail.getMasterPwaId()).thenReturn(100);


    when(masterPwaAuthorisationService.getMasterPwasWhereUserIsAuthorised(webUserAccount)).thenReturn(
        List.of(masterPwaDetail)
    );

    var pickablePwaDtos = pickedPwaRetrievalAndMigrationService.getPickablePwasWhereAuthorised(webUserAccount);

    assertThat(pickablePwaDtos.size()).isEqualTo(1);
    assertThat(pickablePwaDtos.get(0).getPickablePwaString()).isEqualTo(
        PickablePwa.getMasterPwaPrefix() + masterPwaDetail.getMasterPwaId()
    );
    assertThat(pickablePwaDtos.get(0).getReference()).isEqualTo(masterPwaDetail.getReference());

  }

  @Test(expected = IllegalStateException.class)
  public void getOrMigratePickedPwa_whenUnknownPwaSource() {
    when(pickedPwa.getPickablePwaSource()).thenReturn(PickablePwaSource.UNKNOWN);
    var masterPwa = pickedPwaRetrievalAndMigrationService.getOrMigratePickedPwa(pickedPwa, webUserAccount);
  }
}
