package uk.co.ogauthority.pwa.service.fileupload;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.file.AppFileRepository;

/**
 * Service to handle file management for/linking files to PWA applications.
 */
@Service
public class AppFileService {

  private final AppFileRepository appFileRepository;

  @Autowired
  public AppFileService(AppFileRepository appFileRepository) {
    this.appFileRepository = appFileRepository;
  }

  @Transactional
  public void processInitialUpload(FileUploadResponse response, PwaApplication application, AppFilePurpose purpose) {
    if (response.getError() == null) {
      String fileId = String.valueOf(response.getFileId());
      var appFile = new AppFile(application, fileId, purpose, ApplicationFileLinkStatus.FULL);
      appFileRepository.save(appFile);
    }
  }

  /**
   * Delete an individual file for an application.
   *
   * @param appFile file being deleted
   */
  @Transactional
  public void processFileDeletion(AppFile appFile) {
    appFileRepository.delete(appFile);
  }

  public AppFile getAppFileByPwaApplicationAndFileId(PwaApplication application,
                                                     String fileId) {
    return appFileRepository.findByPwaApplicationAndFileId(application, fileId)
        .orElseThrow(() -> new PwaEntityNotFoundException(String.format(
            "Couldn't find an AppFile for app with ID: %s and fileId: %s",
            application.getId(),
            fileId)));
  }

  public List<AppFile> getFilesByIdIn(PwaApplication application,
                                      AppFilePurpose purpose,
                                      Collection<String> fileIds) {
    return appFileRepository.findAllByPwaApplicationAndPurposeAndFileIdIn(application, purpose, fileIds);
  }

}
