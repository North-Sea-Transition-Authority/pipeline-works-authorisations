package uk.co.ogauthority.pwa.service.migration;

import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.MigrationFailedException;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwa.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.migration.MigratablePwaConsent;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationPwaConsent;
import uk.co.ogauthority.pwa.model.entity.migration.ProcessedPwaConsentMigration;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentType;
import uk.co.ogauthority.pwa.repository.migration.MigrationPwaConsentRepository;
import uk.co.ogauthority.pwa.repository.migration.ProcessedPwaConsentMigrationRepository;
import uk.co.ogauthority.pwa.service.masterpwa.MasterPwaManagementService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;

/**
 * Migrate legacy PWAs into new system.
 */
@Service
public class PipelineAuthorisationMigrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineAuthorisationMigrationService.class);

  private final MigrationPwaConsentRepository migrationPwaConsentRepository;

  private final ProcessedPwaConsentMigrationRepository processedPwaConsentMigrationRepository;

  private final PwaConsentService pwaConsentService;

  private final MasterPwaManagementService masterPwaManagementService;

  private final Clock clock;

  public PipelineAuthorisationMigrationService(MigrationPwaConsentRepository migrationPwaConsentRepository,
                                               ProcessedPwaConsentMigrationRepository processedPwaConsentMigrationRepository,
                                               PwaConsentService pwaConsentService,
                                               MasterPwaManagementService masterPwaManagementService,
                                               @Qualifier("utcClock") Clock clock) {
    this.migrationPwaConsentRepository = migrationPwaConsentRepository;
    this.processedPwaConsentMigrationRepository = processedPwaConsentMigrationRepository;
    this.pwaConsentService = pwaConsentService;
    this.masterPwaManagementService = masterPwaManagementService;
    this.clock = clock;
  }

  @Transactional
  public MasterPwaDetail migrate(MigrationMasterPwa migrationMasterPwa) {

    LOGGER.info("Migration Start: padId:" + migrationMasterPwa.getPadId());

    // blow up if we find that the migration has already happened
    if (processedMigrationsOfLinkedConsentsExist(migrationMasterPwa)) {
      throw new MigrationFailedException(
          "One or more ProcessedPwaConsentMigrationFound linked to pad_id:" + migrationMasterPwa.getPadId()
      );
    }

    var masterPwaDetail = masterPwaManagementService.createMasterPwa(
        MasterPwaDetailStatus.CONSENTED,
        migrationMasterPwa.getReference());

    LOGGER.debug(String.format("Migration step: padId: %s About to save all processed consents",  migrationMasterPwa.getPadId()));
    processedPwaConsentMigrationRepository.saveAll(
        createPwaConsents(masterPwaDetail.getMasterPwa(), migrationMasterPwa)
    );

    LOGGER.info("Migration Complete: padId:" + migrationMasterPwa.getPadId());
    return masterPwaDetail;
  }

  private Set<ProcessedPwaConsentMigration> createPwaConsents(MasterPwa masterPwa,
                                                              MigrationMasterPwa migrationMasterPwa) {
    /*
     * Migrating consents:
     * 1. Create the INITIAL_PWA consent from the migration master
     * 2. add this to the processed migrations list to log work done and avoid duplicating migration work
     * 3. find all the consents related to the migrations master, this will include a record for the INITIAL PWA already migrated
     * 4. remove the INITIAL PWA consent from the consentsToMigrate list as it is already done
     * 5. loop over remaining "migratable" consents which should be deposit or variation consents only,
     *    creating each one in the system, and adding a ProcessedPwaConsentMigration to the list.
     * 6. return this list of processed consents, each links to the migrated PWA and newly created PWAConsent
     */
    LOGGER.debug("Creating consents for master pwa padId:" + migrationMasterPwa.getPadId());

    Set<ProcessedPwaConsentMigration> processedPwaConsentMigrations = new HashSet<>();

    var initialPwaConsent = createInitialPwaConsent(masterPwa, migrationMasterPwa);
    var processedInitialPwaMigrationConsent = createProcessedPwaConsentMigration(
        initialPwaConsent,
        getMigrationPwaConsentOrError(migrationMasterPwa)
    );

    processedPwaConsentMigrations.add(processedInitialPwaMigrationConsent);

    LOGGER.debug("Created initial pwa consent for padId:" + migrationMasterPwa.getPadId());

    var consentsToMigrate = migrationPwaConsentRepository.findByMigrationMasterPwa(migrationMasterPwa)
        .stream()
        .filter(migrationPwaConsent -> migrationMasterPwa.getPadId() != migrationPwaConsent.getPadId())
        .collect(toList());

    for (MigrationPwaConsent pwaConsentToMigrate : consentsToMigrate) {

      LOGGER.debug("Start processing linked consent padId:" + pwaConsentToMigrate.getPadId());

      var consent = createDepositOrVariationPwaConsent(masterPwa, pwaConsentToMigrate);
      processedPwaConsentMigrations.add(
          createProcessedPwaConsentMigration(consent, pwaConsentToMigrate)
      );
      LOGGER.debug("Completed Processing linked consent padId:" + pwaConsentToMigrate.getPadId());
    }

    return processedPwaConsentMigrations;

  }

  private MigrationPwaConsent getMigrationPwaConsentOrError(MigrationMasterPwa migrationMasterPwa) {
    return migrationPwaConsentRepository.findById(migrationMasterPwa.getPadId())
        .orElseThrow(
            () -> new MigrationFailedException("Cannot find standard migration consent from migration master")
        );
  }

  private ProcessedPwaConsentMigration createProcessedPwaConsentMigration(PwaConsent pwaConsent,
                                                                          MigrationPwaConsent migrationPwaConsent) {
    var processedConsent = new ProcessedPwaConsentMigration();
    processedConsent.setMigratedTimestamp(clock.instant());
    processedConsent.setMigrationPwaConsent(migrationPwaConsent);
    processedConsent.setPwaConsent(pwaConsent);
    return processedConsent;

  }

  private PwaConsent createDepositOrVariationPwaConsent(MasterPwa masterPwa,
                                                        MigrationPwaConsent pwaConsentToMigrate) {

    PwaConsentType pwaConsentType;

    if (initialPwaDetected(pwaConsentToMigrate)) {
      throw new MigrationFailedException(
          "Migration candidate looks like an INITIAL_PWA but this should already exist " + pwaConsentToMigrate.toString());
    }

    if (Objects.isNull(pwaConsentToMigrate.getVariationNumber())) {
      pwaConsentType = PwaConsentType.DEPOSIT_CONSENT;
    } else {
      pwaConsentType = PwaConsentType.VARIATION;
    }

    return createMigratedPwaConsent(masterPwa, pwaConsentToMigrate, pwaConsentType);
  }

  private PwaConsent createInitialPwaConsent(MasterPwa masterPwa, MigrationMasterPwa migrationMasterPwa) {
    if (!initialPwaDetected(migrationMasterPwa)) {
      throw new MigrationFailedException(
          "Migration candidate does not look like an INITIAL_PWA " + migrationMasterPwa.toString());
    }

    return createMigratedPwaConsent(masterPwa, migrationMasterPwa, PwaConsentType.INITIAL_PWA);

  }

  private boolean initialPwaDetected(MigratablePwaConsent migratablePwaConsent) {
    return Integer.valueOf(0).equals(migratablePwaConsent.getVariationNumber());
  }

  private PwaConsent createMigratedPwaConsent(MasterPwa masterPwa,
                                              MigratablePwaConsent migratablePwaConsent,
                                              PwaConsentType pwaConsentType) {
    return pwaConsentService.createPwaConsentWithoutApplication(
        masterPwa,
        migratablePwaConsent.getReference(),
        pwaConsentType,
        migratablePwaConsent.getConsentedInstant(),
        migratablePwaConsent.getVariationNumber(),
        true
    );
  }


  private boolean processedMigrationsOfLinkedConsentsExist(MigrationMasterPwa migrationMasterPwa) {

    Set<MigrationPwaConsent> pwasConsentsToMigrate = new HashSet<>(
        migrationPwaConsentRepository.findByMigrationMasterPwa(migrationMasterPwa));

    return processedPwaConsentMigrationRepository.existsByMigrationPwaConsentIn(pwasConsentsToMigrate);
  }


}
