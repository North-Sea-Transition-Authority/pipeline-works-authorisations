package uk.co.ogauthority.pwa.repository.pwaapplications.shared.file;

import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
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
                                                                                        ApplicationFilePurpose purpose,
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

}
