package uk.co.ogauthority.pwa.features.filemanagement;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class AppFileManagementService {

  private final FileManagementService fileManagementService;
  private final FileService fileService;

  public AppFileManagementService(
      FileManagementService fileManagementService,
      FileService fileService
  ) {
    this.fileManagementService = fileManagementService;
    this.fileService = fileService;
  }

  public void saveFiles(
      FileUploadForm fileUploadForm,
      PwaApplication pwaApplication,
      FileDocumentType fileDocumentType
  ) {
    fileManagementService.saveFiles(
        fileUploadForm.getUploadedFiles(),
        getUsageId(pwaApplication),
        getUsageType(),
        fileDocumentType
    );
  }

  public FileUploadForm mapFilesToForm(
      FileUploadForm fileUploadForm,
      PwaApplication pwaApplication,
      FileDocumentType fileDocumentType
  ) {
    return fileManagementService.mapFilesToForm(
        fileUploadForm,
        getUsageId(pwaApplication),
        getUsageType(),
        fileDocumentType
    );
  }

  public FileUploadComponentAttributes getFileUploadComponentAttributes(
      List<UploadedFileForm> existingFiles,
      FileDocumentType fileDocumentType,
      PwaApplication pwaApplication
  ) {
    var controller = AppFileManagementRestController.class;

    return fileManagementService.getFileUploadComponentAttributesBuilder(existingFiles, fileDocumentType)
        .withUploadUrl(ReverseRouter.route(on(FileManagementRestController.class).upload(null)))
        .withDownloadUrl(ReverseRouter.route(on(controller).download(pwaApplication.getId(), null)))
        .withDeleteUrl(ReverseRouter.route(on(controller).delete(pwaApplication.getId(), null)))
        .build();
  }

  public void throwIfFileDoesNotBelongToApplication(UploadedFile uploadedFile, PwaApplication pwaApplication) {
    fileManagementService.throwIfFileDoesNotBelongToUsageType(
        uploadedFile,
        getUsageId(pwaApplication),
        getUsageType(),
        null
    );
  }

  public List<UploadedFile> getUploadedFiles(PwaApplication pwaApplication, FileDocumentType documentType) {
    return fileService.findAll(getUsageId(pwaApplication), getUsageType(), documentType.name());
  }

  public ResponseStatusException getFileNotFoundException(UUID fileId, PwaApplication pwaApplication) {
    return fileManagementService.getFileNotFoundException(fileId, getUsageType(), getUsageId(pwaApplication));
  }

  public List<UploadedFileView> getUploadedFileViews(PwaApplication pwaApplication, FileDocumentType fileDocumentType) {
    return getUploadedFiles(pwaApplication, fileDocumentType).stream()
        .map(uploadedFile -> createUploadedFileView(uploadedFile, pwaApplication))
        .toList();
  }

  private UploadedFileView createUploadedFileView(UploadedFile uploadedFile, PwaApplication pwaApplication) {
    return new UploadedFileView(
        null,
        uploadedFile.getName(),
        uploadedFile.getContentLength(),
        uploadedFile.getDescription(),
        uploadedFile.getUploadedAt(),
        ReverseRouter.route(on(PadFileManagementRestController.class).download(pwaApplication.getId(), uploadedFile.getId()))
    );
  }

  private String getUsageId(PwaApplication pwaApplication) {
    return pwaApplication.getId().toString();
  }

  private String getUsageType() {
    return PwaApplication.class.getSimpleName();
  }

}
