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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.util.FileDownloadUtils;
import uk.co.ogauthority.pwa.util.converters.ApplicationTypeUrl;


/**
 * Abstract controller which provides basic utilities for file downloading and common upload controller code.
 */
@FileUploadFrontendController
public abstract class PwaApplicationDataFileUploadAndDownloadController {

  protected final AppFileService appFileService;

  public PwaApplicationDataFileUploadAndDownloadController(AppFileService appFileService) {
    this.appFileService = appFileService;
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
                                            PwaApplication application,
                                            AppFilePurpose purpose,
                                            UploadMultipleFilesWithDescriptionForm uploadForm) {
    return createModelAndView(
        templatePath,
        ReverseRouter.route(on(purpose.getFileControllerClass()).handleUpload(
            application.getApplicationType(),
            application.getId(),
            null,
            null
        )),
        ReverseRouter.route(on(purpose.getFileControllerClass()).handleDownload(
            application.getApplicationType(),
            application.getId(),
            null,
            null
        )),
        ReverseRouter.route(on(purpose.getFileControllerClass()).handleDelete(
            application.getApplicationType(),
            application.getId(),
            null,
            null
        )),
        appFileService.getFilesLinkedToForm(uploadForm, application, purpose));
  }

  /**
   * Serves file for download.
   *
   * @param uploadedFile file we want to trigger download for
   * @return the ResponseEntity object containing the downloaded file
   */
  private ResponseEntity<Resource> serveFile(UploadedFile uploadedFile) {
    Resource resource = FileDownloadUtils.fetchFileAsStream(uploadedFile.getFileName(), uploadedFile.getFileData());
    MediaType mediaType = MediaType.parseMediaType(uploadedFile.getContentType());
    return FileDownloadUtils.getResourceAsResponse(resource, mediaType, uploadedFile.getFileName(),
        uploadedFile.getFileSize());
  }

  protected ResponseEntity<Resource> serveFile(AppFile appFile) {
    return serveFile(appFileService.getUploadedFileById(appFile.getFileId()));
  }

  public abstract FileUploadResult handleUpload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @RequestParam("file") MultipartFile file,
      PwaAppProcessingContext processingContext);

  public abstract ResponseEntity<Resource> handleDownload(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType applicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaAppProcessingContext processingContext);

  public abstract FileDeleteResult handleDelete(
      @PathVariable("applicationType") @ApplicationTypeUrl PwaApplicationType pwaApplicationType,
      @PathVariable("applicationId") Integer applicationId,
      @PathVariable("fileId") String fileId,
      PwaAppProcessingContext processingContext);

}

