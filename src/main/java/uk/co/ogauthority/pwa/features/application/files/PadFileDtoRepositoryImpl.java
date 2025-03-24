package uk.co.ogauthority.pwa.features.application.files;

import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

public class PadFileDtoRepositoryImpl implements PadFileDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PadFileDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<PadFile> findAllByAppDetailAndFilePurposeAndIdNotIn(PwaApplicationDetail detail,
                                                                  ApplicationDetailFilePurpose purpose,
                                                                  Iterable<Integer> padFileIdsToExclude) {
    return entityManager.createQuery("" +
        "SELECT pf " +
        "FROM PadFile pf " +
        "WHERE pf.pwaApplicationDetail = :pwaAppDetail " +
        "AND pf.purpose = :purpose " +
        "AND pf.id NOT IN (:padFileIdsToExclude)", PadFile.class)
        .setParameter("pwaAppDetail", detail)
        .setParameter("purpose", purpose)
        .setParameter("padFileIdsToExclude", padFileIdsToExclude)
        .getResultList();
  }

  @Override
  public List<PadFile> findAllCurrentFilesByAppDetailAndFilePurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                         ApplicationDetailFilePurpose purpose,
                                                                         ApplicationFileLinkStatus applicationFileLinkStatus) {
    return entityManager.createQuery("" +
        "SELECT pf " +
        "FROM PadFile pf " +
        "WHERE pf.pwaApplicationDetail = :pwaAppDetail " +
        "AND pf.purpose = :purpose " +
        "AND (pf.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')" +
        "", PadFile.class)
        .setParameter("pwaAppDetail", detail)
        .setParameter("purpose", purpose)
        .setParameter("fileLinkStatus", applicationFileLinkStatus)
        .getResultList();
  }
}
