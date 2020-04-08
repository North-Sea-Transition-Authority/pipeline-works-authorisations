package uk.co.ogauthority.pwa.service.fileupload;

import java.util.function.BiConsumer;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.PwaApplicationFile;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class PwaApplicationFileService {

  private final FileUploadService fileUploadService;

  @Autowired
  public PwaApplicationFileService(FileUploadService fileUploadService) {
    this.fileUploadService = fileUploadService;
  }

  /**
   * When handling uploads, persist file and allow callers to provide after upload action e.g link to application concept
   *
   * @param afterUploadAction String: fileId, PwaApplicationDetail linked detail
   */
  @Transactional
  public FileUploadResult processApplicationFileUpload(MultipartFile file,
                                                       WebUserAccount user,
                                                       PwaApplicationDetail pwaApplicationDetail,
                                                       BiConsumer<String, PwaApplicationDetail> afterUploadAction) {
    var result = fileUploadService.processUpload(file, user);
    if (result.isValid()) {
      afterUploadAction.accept(result.getFileId().orElse(null), pwaApplicationDetail);
    }
    return result;
  }


  /**
   * When handling file deletes, save file deletion and allow callers to provide after delete action e.g remove app concept link
   *
   * @param afterDeleteAction String: fileId PwaApplicationDetail linked detail
   */
  @Transactional
  public FileDeleteResult processApplicationFileDelete(String fileId, PwaApplicationDetail applicationDetail,
                                                       WebUserAccount user,
                                                       BiConsumer<String, PwaApplicationDetail> afterDeleteAction) {
    var result = fileUploadService.deleteUploadedFile(fileId, user);
    if (result.isValid()) {
      afterDeleteAction.accept(fileId, applicationDetail);
    }
    return result;

  }

  public UploadedFile getUploadedFile(PwaApplicationFile pwaApplicationFile) {
    return fileUploadService.getFileById(pwaApplicationFile.getFileId());
  }
}
