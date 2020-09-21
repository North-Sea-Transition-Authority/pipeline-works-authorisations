package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;

public class PadFileDtoRepositoryImpl implements PadFileDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public PadFileDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<UploadedFileView> findAllAsFileViewByAppDetailAndPurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                                        ApplicationDetailFilePurpose purpose,
                                                                                        ApplicationFileLinkStatus linkStatus) {

    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.model.form.files.UploadedFileView(" +
            "  uf.fileId" +
            ", uf.fileName" +
            ", uf.fileSize" +
            ", pf.description" +
            ", uf.uploadDatetime" +
            ", '#' " + //link updated after construction as requires reverse router
            ") " +
            "FROM PadFile pf " +
            "JOIN UploadedFile uf ON pf.fileId = uf.fileId " +
            "WHERE uf.status = :fileStatus " +
            "AND pf.pwaApplicationDetail = :pwaAppDetail " +
            "AND pf.purpose = :purpose " +
            "AND (pf.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')",
        UploadedFileView.class)
        .setParameter("pwaAppDetail", detail)
        .setParameter("purpose", purpose)
        .setParameter("fileStatus", FileUploadStatus.CURRENT)
        .setParameter("fileLinkStatus", linkStatus)
        .getResultList();

  }

  @Override
  public UploadedFileView findAsFileViewByAppDetailAndFileIdAndPurposeAndFileLinkStatus(PwaApplicationDetail detail,
                                                                                        String fileId,
                                                                                        ApplicationDetailFilePurpose purpose,
                                                                                        ApplicationFileLinkStatus linkStatus) {
    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.model.form.files.UploadedFileView(" +
            "  uf.fileId" +
            ", uf.fileName" +
            ", uf.fileSize" +
            ", pf.description" +
            ", uf.uploadDatetime" +
            ", '#' " + //link updated after construction as requires reverse router
            ") " +
            "FROM PadFile pf " +
            "JOIN UploadedFile uf ON pf.fileId = uf.fileId " +
            "WHERE pf.fileId = :fileId " +
            "AND uf.status = :fileStatus " +
            "AND pf.pwaApplicationDetail = :pwaAppDetail " +
            "AND pf.purpose = :purpose " +
            "AND (pf.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')",
        UploadedFileView.class)
        .setParameter("pwaAppDetail", detail)
        .setParameter("purpose", purpose)
        .setParameter("fileId", fileId)
        .setParameter("fileStatus", FileUploadStatus.CURRENT)
        .setParameter("fileLinkStatus", linkStatus)
        .getSingleResult();
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
        "JOIN UploadedFile uf ON pf.fileId = uf.fileId " +
        "WHERE uf.status = :fileStatus " +
        "AND pf.pwaApplicationDetail = :pwaAppDetail " +
        "AND pf.purpose = :purpose " +
        "AND (pf.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')" +
        "", PadFile.class)
        .setParameter("pwaAppDetail", detail)
        .setParameter("purpose", purpose)
        .setParameter("fileStatus", FileUploadStatus.CURRENT)
        .setParameter("fileLinkStatus", applicationFileLinkStatus)
        .getResultList();
  }
}
