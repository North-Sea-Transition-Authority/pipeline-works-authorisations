package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;

public class AppFileDtoRepositoryImpl implements AppFileDtoRepository {

  private final EntityManager entityManager;

  @Autowired
  public AppFileDtoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<UploadedFileView> findAllAsFileViewByAppAndPurposeAndFileLinkStatus(PwaApplication application,
                                                                                  AppFilePurpose purpose,
                                                                                  ApplicationFileLinkStatus linkStatus) {

    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView(" +
            "  uf.fileId" +
            ", uf.fileName" +
            ", uf.fileSize" +
            ", af.description" +
            ", uf.uploadDatetime" +
            ", '#' " + //link updated after construction as requires reverse router
            ") " +
            "FROM AppFile af " +
            "JOIN UploadedFile uf ON af.fileId = uf.fileId " +
            "WHERE uf.status = :fileStatus " +
            "AND af.pwaApplication = :app " +
            "AND af.purpose = :purpose " +
            "AND (af.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')",
        UploadedFileView.class)
        .setParameter("app", application)
        .setParameter("purpose", purpose)
        .setParameter("fileStatus", FileUploadStatus.CURRENT)
        .setParameter("fileLinkStatus", linkStatus)
        .getResultList();

  }

  @Override
  public UploadedFileView findAsFileViewByAppAndFileIdAndPurposeAndFileLinkStatus(PwaApplication application,
                                                                                  String fileId,
                                                                                  AppFilePurpose purpose,
                                                                                  ApplicationFileLinkStatus linkStatus) {
    return entityManager.createQuery("" +
            "SELECT new uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView(" +
            "  uf.fileId" +
            ", uf.fileName" +
            ", uf.fileSize" +
            ", af.description" +
            ", uf.uploadDatetime" +
            ", '#' " + //link updated after construction as requires reverse router
            ") " +
            "FROM AppFile af " +
            "JOIN UploadedFile uf ON af.fileId = uf.fileId " +
            "WHERE af.fileId = :fileId " +
            "AND uf.status = :fileStatus " +
            "AND af.pwaApplication = :app " +
            "AND af.purpose = :purpose " +
            "AND (af.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')",
        UploadedFileView.class)
        .setParameter("app", application)
        .setParameter("purpose", purpose)
        .setParameter("fileId", fileId)
        .setParameter("fileStatus", FileUploadStatus.CURRENT)
        .setParameter("fileLinkStatus", linkStatus)
        .getSingleResult();
  }

  @Override
  public List<AppFile> findAllByAppAndFilePurposeAndIdNotIn(PwaApplication application,
                                                            AppFilePurpose purpose,
                                                            Iterable<Integer> appFileIdsToExclude) {
    return entityManager.createQuery("" +
        "SELECT af " +
        "FROM AppFile af " +
        "WHERE af.pwaApplication = :app " +
        "AND af.purpose = :purpose " +
        "AND af.id NOT IN (:appFileIdsToExclude)", AppFile.class)
        .setParameter("app", application)
        .setParameter("purpose", purpose)
        .setParameter("appFileIdsToExclude", appFileIdsToExclude)
        .getResultList();
  }

  @Override
  public List<AppFile> findAllCurrentFilesByAppAndFilePurposeAndFileLinkStatus(PwaApplication application,
                                                                               AppFilePurpose purpose,
                                                                               ApplicationFileLinkStatus applicationFileLinkStatus) {
    return entityManager.createQuery("" +
        "SELECT af " +
        "FROM AppFile af " +
        "JOIN UploadedFile uf ON af.fileId = uf.fileId " +
        "WHERE uf.status = :fileStatus " +
        "AND af.pwaApplication = :app " +
        "AND af.purpose = :purpose " +
        "AND (af.fileLinkStatus = :fileLinkStatus OR :fileLinkStatus = '" + ApplicationFileLinkStatus.ALL + "')" +
        "", AppFile.class)
        .setParameter("app", application)
        .setParameter("purpose", purpose)
        .setParameter("fileStatus", FileUploadStatus.CURRENT)
        .setParameter("fileLinkStatus", applicationFileLinkStatus)
        .getResultList();
  }
}
