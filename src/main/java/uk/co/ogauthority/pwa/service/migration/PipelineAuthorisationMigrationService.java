package uk.co.ogauthority.pwa.service.migration;

import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
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
import uk.co.ogauthority.pwa.repository.migration.MigrationMasterPwaRepository;
import uk.co.ogauthority.pwa.repository.migration.MigrationPwaConsentRepository;
import uk.co.ogauthority.pwa.repository.migration.ProcessedPwaConsentMigrationRepository;
import uk.co.ogauthority.pwa.service.masterpwa.MasterPwaManagementService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;

@Service
public class PipelineAuthorisationMigrationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineAuthorisationMigrationService.class);

  private final MigrationMasterPwaRepository migrationMasterPwaRepository;

  private final MigrationPwaConsentRepository migrationPwaConsentRepository;

  private final ProcessedPwaConsentMigrationRepository processedPwaConsentMigrationRepository;

  private final PwaConsentService pwaConsentService;

  private final MasterPwaManagementService masterPwaManagementService;

  private final Clock clock;

  private final EntityManager entityManager;

  public PipelineAuthorisationMigrationService(MigrationMasterPwaRepository migrationMasterPwaRepository,
                                               MigrationPwaConsentRepository migrationPwaConsentRepository,
                                               ProcessedPwaConsentMigrationRepository processedPwaConsentMigrationRepository,
                                               PwaConsentService pwaConsentService,
                                               MasterPwaManagementService masterPwaManagementService,
                                               @Qualifier("utcClock") Clock clock,
                                               EntityManager entityManager) {
    this.migrationMasterPwaRepository = migrationMasterPwaRepository;
    this.migrationPwaConsentRepository = migrationPwaConsentRepository;
    this.processedPwaConsentMigrationRepository = processedPwaConsentMigrationRepository;
    this.pwaConsentService = pwaConsentService;
    this.masterPwaManagementService = masterPwaManagementService;
    this.clock = clock;
    this.entityManager = entityManager;
  }

  @Transactional
  public MasterPwaDetail migrate(MigrationMasterPwa migrationMasterPwa) {
    LOGGER.info("Starting PWA migration padId:" + migrationMasterPwa.getPadId());

    // blow up if we find that the migration has already happened
    if (processedMigrationsOfLinkedConsentsExist(migrationMasterPwa)) {
      throw new MigrationFailedException(
          "one or more ProcessedPwaConsentMigrationFound linked to pad_id:" + migrationMasterPwa.getPadId()
      );
    }

    var masterPwaDetail = masterPwaManagementService.createMasterPwa(
        MasterPwaDetailStatus.CONSENTED,
        migrationMasterPwa.getReference());

    LOGGER.debug("About to save processed consents for padId:" + migrationMasterPwa.getPadId());
    processedPwaConsentMigrationRepository.saveAll(
        createPwaConsents(masterPwaDetail.getMasterPwa(), migrationMasterPwa)
    );

    LOGGER.info("PWA migration Complete padId:" + migrationMasterPwa.getPadId());
    return masterPwaDetail;
  }

  private Set<ProcessedPwaConsentMigration> createPwaConsents(MasterPwa masterPwa,
                                                              MigrationMasterPwa migrationMasterPwa) {
    LOGGER.debug("Creating linked consents for padId:" + migrationMasterPwa.getPadId());
    Set<ProcessedPwaConsentMigration> processedPwaConsentMigrations = new HashSet<>();
    var initialPwaConsent = createInitialPwaConsent(masterPwa, migrationMasterPwa);
    processedPwaConsentMigrations.add(createProcessedPwaConsentMigration(
        initialPwaConsent,
        migrationPwaConsentRepository.findById(migrationMasterPwa.getPadId())
            .orElseThrow(
                () -> new MigrationFailedException("Cannot find standard migration consent from migration master"))
    ));
    LOGGER.debug("Created linked consents for padId:" + migrationMasterPwa.getPadId());

    var consentsToMigrate = migrationPwaConsentRepository.findByMigrationMasterPwa(migrationMasterPwa)
        .stream()
        .filter(migrationPwaConsent -> migrationMasterPwa.getPadId() != migrationPwaConsent.getPadId())
        .collect(toList());

    for (MigrationPwaConsent pwaConsentToMigrate : consentsToMigrate) {

      LOGGER.debug("Start Processing padId:" + pwaConsentToMigrate.getPadId());

      var consent = createDepositOrVariationPwaConsent(masterPwa, pwaConsentToMigrate);
      processedPwaConsentMigrations.add(
          createProcessedPwaConsentMigration(consent, pwaConsentToMigrate)
      );
      LOGGER.debug("Complete Processing padId:" + pwaConsentToMigrate.getPadId());
    }

    return processedPwaConsentMigrations;

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

    if (Integer.valueOf(0).equals(pwaConsentToMigrate.getVariationNumber())) {
      throw new MigrationFailedException(
          "Migration candidate looks like an INITIAL_PWA but this should already exist" + pwaConsentToMigrate.toString());
    }

    if (Objects.isNull(pwaConsentToMigrate.getVariationNumber())) {
      pwaConsentType = PwaConsentType.DEPOSIT_CONSENT;
    } else {
      pwaConsentType = PwaConsentType.VARIATION;
    }

    return createMigratedPwaConsent(masterPwa, pwaConsentToMigrate, pwaConsentType);
  }

  private PwaConsent createInitialPwaConsent(MasterPwa masterPwa, MigrationMasterPwa migrationMasterPwa) {
    if (!Integer.valueOf(0).equals(migrationMasterPwa.getVariationNumber())) {
      throw new MigrationFailedException(
          "Migration candidate looks like an INITIAL_PWA but this should already exist" + migrationMasterPwa.toString());
    }

    return createMigratedPwaConsent(masterPwa, migrationMasterPwa, PwaConsentType.INITIAL_PWA);

  }

  private PwaConsent createMigratedPwaConsent(MasterPwa masterPwa,
                                              MigratablePwaConsent migratablePwaConsent,
                                              PwaConsentType pwaConsentType) {
    return pwaConsentService.createPwaConsentWithoutApplication(
        masterPwa,
        migratablePwaConsent.getReference(),
        pwaConsentType,
        migratablePwaConsent.getConsentedInstant(),
        true
    );
  }


  private boolean processedMigrationsOfLinkedConsentsExist(MigrationMasterPwa migrationMasterPwa) {

    Set<MigrationPwaConsent> pwasConsentsToMigrate = new HashSet<>(
        migrationPwaConsentRepository.findByMigrationMasterPwa(migrationMasterPwa));

    return processedPwaConsentMigrationRepository.existsByMigrationPwaConsentIn(pwasConsentsToMigrate);
  }

  /*
   * Skeleton implementation until we have the authorisation model done
   * */
  public List<MigrationMasterPwa> getMasterPwasWhereUserIsAuthorisedAndNotMigrated(WebUserAccount webUserAccount) {
    // TODO authorisation
    return entityManager.createQuery("SELECT " +
        " mmp " +
        "FROM MigrationMasterPwa mmp " +
        "WHERE NOT EXISTS ( " +
        "  SELECT 1 " +
        "  FROM ProcessedPwaConsentMigration ppcm " +
        "  WHERE mmp.padId = ppcm.migrationPwaConsent.padId " +
        ")" +
        "", MigrationMasterPwa.class)
        .getResultList();
  }

  public MigrationMasterPwa getMasterPwaWhereUserIsAuthorisedAndNotMigratedByPadId(WebUserAccount webUserAccount,
                                                                                   int padId) {
    try {
      return entityManager.createQuery("SELECT " +
          " mmp " +
          "FROM MigrationMasterPwa mmp " +
          "WHERE NOT EXISTS ( " +
          "  SELECT 1 " +
          "  FROM ProcessedPwaConsentMigration ppcm " +
          "  WHERE mmp.padId = ppcm.migrationPwaConsent.padId " +
          ") " +
          "AND mmp.padId = :padId" +
          "", MigrationMasterPwa.class)
          .setParameter("padId", padId)
          .getSingleResult();
    } catch (NoResultException e) {
      throw new MigrationFailedException("Could not find migratable consent with padId:" + padId, e);
    }
  }

}
