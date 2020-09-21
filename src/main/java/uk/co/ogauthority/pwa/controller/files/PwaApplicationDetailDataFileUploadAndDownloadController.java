package uk.co.ogauthority.pwa.controller.files;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.PadFile;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.util.FileDownloadUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


/**
 * Abstract controller which provides basic utilities for file downloading and common upload controller code.
 */
@FileUploadFrontendController
public abstract class PwaApplicationDetailDataFileUploadAndDownloadController {

  protected final PadFileService padFileService;

  public PwaApplicationDetailDataFileUploadAndDownloadController(PadFileService padFileService) {
    this.padFileService = padFileService;
  }

  /**
   * Create model and view with all file urls populated in model.
   */
  protected ModelAndView createModelAndView(String templatePath,
                                            String uploadFileUrl,
                                            String downloadUrl,
                                            String deleteUrl,
                                            List<UploadedFileView> uploadedFileViews) {
    return new ModelAndView(templatePath)
        .addObject("uploadedFileViewList", uploadedFileViews)
        .addObject("uploadUrl", uploadFileUrl)
        .addObject("downloadUrl", downloadUrl)
        .addObject("deleteUrl", deleteUrl);
  }

  protected ModelAndView createModelAndView(String templatePath,
                                            PwaApplicationDetail detail,
                                            ApplicationDetailFilePurpose purpose,
                                            UploadMultipleFilesWithDescriptionForm uploadForm) {
    return createModelAndView(
        templatePath,
        ReverseRouter.route(on(purpose.getFileControllerClass()).handleUpload(
            detail.getPwaApplicationType(),
            detail.getMasterPwaApplicationId(),
            null,
            null
        )),
        ReverseRouter.route(on(purpose.getFileControllerClass()).handleDownload(
            detail.getPwaApplicationType(),
            detail.getMasterPwaApplicationId(),
            null,
            null
        )),
        ReverseRouter.route(on(purpose.getFileControllerClass()).handleDelete(
            detail.getPwaApplicationType(),
            detail.getMasterPwaApplicationId(),
            null,
            null
        )),
        padFileService.getFilesLinkedToForm(uploadForm, detail, purpose));
  }

  /**
   * Serves file for download.
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

  protected ResponseEntity<Resource> serveFile(PadFile padFile) {
    return serveFile(padFileService.getUploadedFileById(padFile.getFileId()));
  }

  public abstract FileUploadResult handleUpload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @RequestParam("file") MultipartFile file,
      PwaApplicationContext applicationContext);

  public abstract ResponseEntity<Resource> handleDownload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext);

  public abstract FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaApplicationContext applicationContext);

}

