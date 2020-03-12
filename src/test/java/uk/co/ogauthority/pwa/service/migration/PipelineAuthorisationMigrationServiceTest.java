package uk.co.ogauthority.pwa.service.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.exception.MigrationFailedException;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationPwaConsent;
import uk.co.ogauthority.pwa.model.entity.migration.ProcessedPwaConsentMigration;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.repository.migration.MigrationPwaConsentRepository;
import uk.co.ogauthority.pwa.repository.migration.ProcessedPwaConsentMigrationRepository;
import uk.co.ogauthority.pwa.service.masterpwa.MasterPwaManagementService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;

@RunWith(MockitoJUnitRunner.class)
public class PipelineAuthorisationMigrationServiceTest {

  private static final int MIGRATION_MASTER_PA_ID = 1;
  private static final int INITIAL_PWA_PAD_ID = 10;
  private static final int VARIATION_PWA_PAD_ID = 20;
  private static final int DEPOSIT_PWA_PAD_ID = 30;

  private static final Instant INITIAL_PWA_CONSENT_INSTANT = Instant.MIN.plus(10, ChronoUnit.DAYS);
  private static final Instant VARIATION_CONSENT_INSTANT = Instant.MIN.plus(20, ChronoUnit.DAYS);
  private static final Instant DEPOSIT_CONSENT_INSTANT = Instant.MIN.plus(30, ChronoUnit.DAYS);

  private static final String INITIAL_PWA_CONSENT_REFERENCE = "1/W/1";
  private static final String VARIATION_CONSENT_REFERENCE = "1/V/1";
  private static final String DEPOSIT_CONSENT_REFERENCE = "1/D/1";

  private static final Integer INITIAL_PWA_CONSENT_VARIATION_NUMBER = 0;
  private static final Integer VARIATION_CONSENT_VARIATION_NUMBER = 1;
  private static final Integer DEPOSIT_CONSENT_VARIATION_NUMBER = null;

  @Mock
  private MigrationPwaConsentRepository migrationPwaConsentRepository;

  @Mock
  private ProcessedPwaConsentMigrationRepository processedPwaConsentMigrationRepository;

  @Mock
  private PwaConsentService pwaConsentService;

  @Mock
  private MasterPwaManagementService masterPwaManagementService;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Captor
  private ArgumentCaptor<Collection<ProcessedPwaConsentMigration>> processedPwaConsentMigrationCaptor;

  @Mock
  private MigrationMasterPwa migrationMasterPwa;

  private MigrationPwaConsent initialPwaConsent;
  private MigrationPwaConsent depositConsent;
  private MigrationPwaConsent variationConsent;

  private MasterPwa masterPwa;
  private MasterPwaDetail masterPwaDetail;


  private PipelineAuthorisationMigrationService pipelineAuthorisationMigrationService;

  @Before
  public void setup() {
    pipelineAuthorisationMigrationService = new PipelineAuthorisationMigrationService(
        migrationPwaConsentRepository,
        processedPwaConsentMigrationRepository,
        pwaConsentService,
        masterPwaManagementService,
        clock
    );

    createMigrationData();
    when(migrationPwaConsentRepository.findByMigrationMasterPwa(migrationMasterPwa)).thenReturn(
        List.of(initialPwaConsent, variationConsent, depositConsent)
    );

    when(migrationPwaConsentRepository.findById(initialPwaConsent.getPadId())).thenReturn(
        Optional.of(initialPwaConsent));

    masterPwa = new MasterPwa(Instant.now());
    masterPwa.setId(90);
    masterPwaDetail = new MasterPwaDetail(Instant.now());
    masterPwaDetail.setId(95);
    masterPwaDetail.setMasterPwa(masterPwa);

    when(masterPwaManagementService.createMasterPwa(any(), any())).thenAnswer(
        invocation -> {
          masterPwaDetail.setMasterPwaDetailStatus(invocation.getArgument(0));
          masterPwaDetail.setReference(invocation.getArgument(1));
          return masterPwaDetail;
        }
    );
  }

  @Test
  public void migrate_verifyServiceInteraction_masterPwaManagementService() {

    pipelineAuthorisationMigrationService.migrate(migrationMasterPwa);

    verify(masterPwaManagementService, times(1)).createMasterPwa(
        MasterPwaDetailStatus.CONSENTED,
        migrationMasterPwa.getReference()
    );

    verifyNoMoreInteractions(masterPwaManagementService);

  }


  @Test(expected = MigrationFailedException.class)
  public void migrate_whenPwaMigrationMasterHasNullVariationNumber() {

    migrationMasterPwa.setVariationNumber(null);

    pipelineAuthorisationMigrationService.migrate(migrationMasterPwa);

  }

  @Test(expected = MigrationFailedException.class)
  public void migrate_whenPwaMigrationMasterHasVariationNumberNotEqualToZero() {

    migrationMasterPwa.setVariationNumber(1);

    pipelineAuthorisationMigrationService.migrate(migrationMasterPwa);

  }

  @Test(expected = MigrationFailedException.class)
  public void migrate_whenLinkedConsentMatchesInitialPwaConsent() {

    depositConsent.setVariationNumber(0);

    pipelineAuthorisationMigrationService.migrate(migrationMasterPwa);

  }

  @Test(expected = MigrationFailedException.class)
  public void migrate_whenProcessedMigrationFound() {

    when(processedPwaConsentMigrationRepository.existsByMigrationPwaConsentIn(any())).thenReturn(true);

    pipelineAuthorisationMigrationService.migrate(migrationMasterPwa);

  }

  @Test
  public void migrate_verifyServiceInteraction_processedPwaConsentMigrationRepository_whenLinkedConsents() {

    pipelineAuthorisationMigrationService.migrate(migrationMasterPwa);
    verify(processedPwaConsentMigrationRepository, times(1)).existsByMigrationPwaConsentIn(any());
    verify(processedPwaConsentMigrationRepository, times(1)).saveAll(processedPwaConsentMigrationCaptor.capture());

    Collection<ProcessedPwaConsentMigration> processedPwaConsentMigrations = processedPwaConsentMigrationCaptor.getValue();
    assertThat(processedPwaConsentMigrations.size()).isEqualTo(3);
    assertThat(processedPwaConsentMigrations).anySatisfy(pc -> {
      assertThat(pc.getMigrationPwaConsent().equals(initialPwaConsent));
    });

    assertThat(processedPwaConsentMigrations).anySatisfy(pc -> {
      assertThat(pc.getMigrationPwaConsent().equals(variationConsent));
    });

    assertThat(processedPwaConsentMigrations).anySatisfy(pc -> {
      assertThat(pc.getMigrationPwaConsent().equals(depositConsent));
    });


    verifyNoMoreInteractions(processedPwaConsentMigrationRepository);

  }

  @Test
  public void migrate_verifyServiceInteraction_pwaConsentsService_whenLinkedConsents() {

    pipelineAuthorisationMigrationService.migrate(migrationMasterPwa);

    verify(pwaConsentService, times(1)).createPwaConsentWithoutApplication(
        masterPwa,
        INITIAL_PWA_CONSENT_REFERENCE,
        PwaConsentType.INITIAL_PWA,
        INITIAL_PWA_CONSENT_INSTANT,
        INITIAL_PWA_CONSENT_VARIATION_NUMBER,
        true
    );

    verify(pwaConsentService, times(1)).createPwaConsentWithoutApplication(
        masterPwa,
        VARIATION_CONSENT_REFERENCE,
        PwaConsentType.VARIATION,
        VARIATION_CONSENT_INSTANT,
        VARIATION_CONSENT_VARIATION_NUMBER,
        true
    );

    verify(pwaConsentService, times(1)).createPwaConsentWithoutApplication(
        masterPwa,
        DEPOSIT_CONSENT_REFERENCE,
        PwaConsentType.DEPOSIT_CONSENT,
        DEPOSIT_CONSENT_INSTANT,
        DEPOSIT_CONSENT_VARIATION_NUMBER,
        true
    );

    verifyNoMoreInteractions(pwaConsentService);

  }

  @Test
  public void migrate_verifyServiceInteraction_pwaConsentsService_whenOnlyInitialPWaConsent() {

    ArgumentCaptor<ProcessedPwaConsentMigration> processedConsentArgumentCaptor = ArgumentCaptor.forClass(
        ProcessedPwaConsentMigration.class);
    when(migrationPwaConsentRepository.findByMigrationMasterPwa(migrationMasterPwa)).thenReturn(
        List.of(initialPwaConsent)
    );


    pipelineAuthorisationMigrationService.migrate(migrationMasterPwa);

    verify(pwaConsentService, times(1)).createPwaConsentWithoutApplication(
        masterPwa,
        INITIAL_PWA_CONSENT_REFERENCE,
        PwaConsentType.INITIAL_PWA,
        INITIAL_PWA_CONSENT_INSTANT,
        INITIAL_PWA_CONSENT_VARIATION_NUMBER,
        true
    );

    verifyNoMoreInteractions(pwaConsentService);

  }

  private void createMigrationData() {
    migrationMasterPwa = new MigrationMasterPwa(
        INITIAL_PWA_PAD_ID,
        MIGRATION_MASTER_PA_ID,
        null,
        INITIAL_PWA_PAD_ID,
        0,
        INITIAL_PWA_CONSENT_INSTANT,
        INITIAL_PWA_CONSENT_REFERENCE
    );

    initialPwaConsent = new MigrationPwaConsent(
        INITIAL_PWA_PAD_ID,
        MIGRATION_MASTER_PA_ID,
        null,
        migrationMasterPwa,
        INITIAL_PWA_CONSENT_VARIATION_NUMBER,
        INITIAL_PWA_CONSENT_INSTANT,
        INITIAL_PWA_CONSENT_REFERENCE
    );

    variationConsent = new MigrationPwaConsent(
        VARIATION_PWA_PAD_ID,
        MIGRATION_MASTER_PA_ID,
        null,
        migrationMasterPwa,
        VARIATION_CONSENT_VARIATION_NUMBER,
        VARIATION_CONSENT_INSTANT,
        VARIATION_CONSENT_REFERENCE
    );

    depositConsent = new MigrationPwaConsent(
        DEPOSIT_PWA_PAD_ID,
        MIGRATION_MASTER_PA_ID,
        null,
        migrationMasterPwa,
        DEPOSIT_CONSENT_VARIATION_NUMBER,
        DEPOSIT_CONSENT_INSTANT,
        DEPOSIT_CONSENT_REFERENCE
    );
  }

}
