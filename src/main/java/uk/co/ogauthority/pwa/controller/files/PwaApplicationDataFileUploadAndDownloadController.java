package uk.co.ogauthority.pwa.controller.files;

import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.util.FileDownloadUtils;


/**
 * Abstract controller which provides basic utilities for file downloading and common upload controller code.
 */
@FileUploadFrontendController
public abstract class PwaApplicationDataFileUploadAndDownloadController {

  /**
   * Create model and view with all file urls populated in model.
   */
  protected ModelAndView createModelAndView(String templatePath, String uploadFileUrl, String downloadUrl,
                                            String deleteUrl, List<UploadedFileView> uploadedFileViews) {
    return new ModelAndView(templatePath)
        .addObject("uploadedFileViewList", uploadedFileViews)
        .addObject("uploadUrl", uploadFileUrl)
        .addObject("downloadUrl", downloadUrl)
        .addObject("deleteUrl", deleteUrl);
  }

  /**
   * serves file for download.
   *
   * @param uploadedFile file we want to trigger download for
   * @return the ResponseEntity object containing the downloaded file
   */
  protected ResponseEntity<Resource> serveFile(UploadedFile uploadedFile) {
    Resource resource = FileDownloadUtils.fetchFileAsStream(uploadedFile.getFileName(), uploadedFile.getFileData());
    MediaType mediaType = MediaType.parseMediaType(uploadedFile.getContentType());
    return FileDownloadUtils.getResourceAsResponse(resource, mediaType, uploadedFile.getFileName(),
        uploadedFile.getFileSize());
  }
}

