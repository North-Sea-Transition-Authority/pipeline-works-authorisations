package uk.co.ogauthority.pwa.service.migration;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.MigrationFailedException;
import uk.co.ogauthority.pwa.model.entity.migration.MigrationMasterPwa;

/* Simple service to give convenient access to migration data to other migration package services*/
@Service
public class MigrationDataAccessor {

  private final EntityManager entityManager;

  @Autowired
  MigrationDataAccessor(EntityManager entityManager) {
    this.entityManager = entityManager;
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
