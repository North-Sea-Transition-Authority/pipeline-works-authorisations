package uk.co.ogauthority.pwa.service.pickpwa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaAuthorisationService;
import uk.co.ogauthority.pwa.service.migration.MigrationDataAccessor;
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
  private MigrationDataAccessor migrationDataAccessor;

  @Mock
  private MasterPwa masterPwa;

  @Mock
  private PickablePwa pickedPwa;

  private WebUserAccount webUserAccount = new WebUserAccount(1);

  private PickedPwaRetrievalAndMigrationService pickedPwaRetrievalAndMigrationService;

  @Before
  public void setup() {
    pickedPwaRetrievalAndMigrationService = new PickedPwaRetrievalAndMigrationService(
        masterPwaAuthorisationService,
        pipelineAuthorisationMigrationService,
        migrationDataAccessor
        );

    migrationMasterPwa = new MigrationMasterPwa();
    migrationMasterPwa.setPadId(1);
    migrationMasterPwa.setReference("REFERENCE");

    when(pickedPwa.getContentId()).thenReturn(100);
    when(masterPwaDetail.getMasterPwaId()).thenReturn(999);
    when(masterPwaDetail.getMasterPwa()).thenReturn(masterPwa);
  }

  @Test
  public void getPickablePwasWhereAuthorised_whenNoPickablePwasExist() {
    assertThat(pickedPwaRetrievalAndMigrationService.getPickablePwasWhereAuthorised(webUserAccount)).isEmpty();

    verify(masterPwaAuthorisationService, times(1)).getMasterPwasWhereUserIsAuthorised(webUserAccount);
    verify(migrationDataAccessor, times(1)).getMasterPwasWhereUserIsAuthorisedAndNotMigrated(
        webUserAccount);
  }

  @Test
  public void getPickablePwasWhereAuthorised_whenSingleMigrationPwaExistsOnly() {

    // TODO PWA-69 requires update/removal when migrations are no longer supported in app.
    when(migrationDataAccessor.getMasterPwasWhereUserIsAuthorisedAndNotMigrated(
        webUserAccount)).thenReturn(
        List.of(migrationMasterPwa)
    );

    var pickablePwaDtos = pickedPwaRetrievalAndMigrationService.getPickablePwasWhereAuthorised(webUserAccount);

    assertThat(pickablePwaDtos.size()).isEqualTo(0);


  }

  @Test
  public void getPickablePwasWhereAuthorised_whenSingleMasterPwaExistsOnly() {
    when(masterPwaDetail.getReference()).thenReturn("REFERENCE");

    when(masterPwaAuthorisationService.getMasterPwasWhereUserIsAuthorised(webUserAccount)).thenReturn(
        List.of(masterPwaDetail)
    );

    var pickablePwaDtos = pickedPwaRetrievalAndMigrationService.getPickablePwasWhereAuthorised(webUserAccount);

    assertThat(pickablePwaDtos.size()).isEqualTo(1);
    assertThat(pickablePwaDtos.get(0).getPickablePwaString()).isEqualTo(
        PickablePwaSource.MASTER.getPickableStringPrefix() + masterPwaDetail.getMasterPwaId()
    );
    assertThat(pickablePwaDtos.get(0).getReference()).isEqualTo(masterPwaDetail.getReference());

  }

  @Test(expected = IllegalStateException.class)
  public void getOrMigratePickedPwa_whenUnknownPwaSource() {
    when(pickedPwa.getPickablePwaSource()).thenReturn(PickablePwaSource.UNKNOWN);
    var masterPwa = pickedPwaRetrievalAndMigrationService.getOrMigratePickedPwa(pickedPwa, webUserAccount);
  }

  @Test
  public void getOrMigratePickedPwa_whenSourceIsMaster() {
    when(pickedPwa.getPickablePwaSource()).thenReturn(PickablePwaSource.MASTER);
    pickedPwaRetrievalAndMigrationService.getOrMigratePickedPwa(pickedPwa, webUserAccount);
    verify(masterPwaAuthorisationService, times(1)).getMasterPwaIfAuthorised(
        pickedPwa.getContentId(),
        webUserAccount
    );
    verifyNoMoreInteractions(masterPwaAuthorisationService, pipelineAuthorisationMigrationService);
  }

  @Test
  public void getOrMigratePickedPwa_whenSourceIsMigration() {
    when(pickedPwa.getPickablePwaSource()).thenReturn(PickablePwaSource.MIGRATION);
    when(migrationDataAccessor.getMasterPwaWhereUserIsAuthorisedAndNotMigratedByPadId(
        webUserAccount,
        pickedPwa.getContentId()
    )).thenReturn(migrationMasterPwa);

    when(pipelineAuthorisationMigrationService.migrate(migrationMasterPwa)).thenReturn(masterPwaDetail);

    InOrder orderVerifier = inOrder(pipelineAuthorisationMigrationService, masterPwaAuthorisationService, migrationDataAccessor);

    pickedPwaRetrievalAndMigrationService.getOrMigratePickedPwa(pickedPwa, webUserAccount);

    orderVerifier.verify(migrationDataAccessor, times(1))
        .getMasterPwaWhereUserIsAuthorisedAndNotMigratedByPadId(
            webUserAccount,
            pickedPwa.getContentId()
        );

    orderVerifier.verify(pipelineAuthorisationMigrationService, times(1))
        .migrate(migrationMasterPwa);

    orderVerifier.verifyNoMoreInteractions();
  }
}
