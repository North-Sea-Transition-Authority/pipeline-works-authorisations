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
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@Service
public class PadFileManagementService {

  private final FileManagementService fileManagementService;
  private final FileService fileService;

  public PadFileManagementService(
      FileManagementService fileManagementService,
      FileService fileService
  ) {
    this.fileManagementService = fileManagementService;
    this.fileService = fileService;
  }

  public void saveFiles(
      FileUploadForm fileUploadForm,
      PwaApplicationDetail pwaApplicationDetail,
      FileDocumentType fileDocumentType
  ) {
    fileManagementService.saveFiles(
        fileUploadForm.getUploadedFiles(),
        getUsageId(pwaApplicationDetail),
        getUsageType(),
        fileDocumentType
    );
  }

  public FileUploadForm mapFilesToForm(
      FileUploadForm fileUploadForm,
      PwaApplicationDetail pwaApplicationDetail,
      FileDocumentType fileDocumentType
  ) {
    return fileManagementService.mapFilesToForm(
        fileUploadForm,
        getUsageId(pwaApplicationDetail),
        getUsageType(),
        fileDocumentType
    );
  }

  public FileUploadComponentAttributes getFileUploadComponentAttributes(
      List<UploadedFileForm> existingFiles,
      FileDocumentType fileDocumentType,
      PwaApplicationDetail pwaApplicationDetail
  ) {
    var controller = PadFileManagementRestController.class;

    return fileManagementService.getFileUploadComponentAttributesBuilder(existingFiles, fileDocumentType)
        .withUploadUrl(ReverseRouter.route(on(FileManagementRestController.class).upload(null)))
        .withDownloadUrl(ReverseRouter.route(on(controller).download(pwaApplicationDetail.getId(), null)))
        .withDeleteUrl(ReverseRouter.route(on(controller).delete(pwaApplicationDetail.getId(), null)))
        .build();
  }

  public void throwIfFileDoesNotBelongToApplicationDetail(UploadedFile uploadedFile, PwaApplicationDetail pwaApplicationDetail) {
    fileManagementService.throwIfFileDoesNotBelongToUsageType(
        uploadedFile,
        getUsageId(pwaApplicationDetail),
        getUsageType(),
        null
    );
  }

  public List<UploadedFile> getUploadedFiles(PwaApplicationDetail pwaApplicationDetail, FileDocumentType documentType) {
    return fileService.findAll(getUsageId(pwaApplicationDetail), getUsageType(), documentType.name());
  }

  public void copyUploadedFiles(PwaApplicationDetail oldApplicationDetail,
                                PwaApplicationDetail newApplicationDetail,
                                FileDocumentType fileDocumentType) {
    getUploadedFiles(
        oldApplicationDetail,
        fileDocumentType
    ).forEach(uploadedFile ->
        fileService.copy(
            uploadedFile,
            usageBuilder -> fileManagementService.buildFileUsage(
                usageBuilder,
                fileDocumentType.name(),
                getUsageId(newApplicationDetail),
                getUsageType()
            )
        )
    );
  }

  public ResponseStatusException getFileNotFoundException(UUID fileId, PwaApplicationDetail pwaApplicationDetail) {
    return fileManagementService.getFileNotFoundException(fileId, getUsageType(), getUsageId(pwaApplicationDetail));
  }

  public List<UploadedFileView> getUploadedFileViews(PwaApplicationDetail pwaApplicationDetail, FileDocumentType fileDocumentType) {
    return getUploadedFiles(pwaApplicationDetail, fileDocumentType).stream()
        .map(uploadedFile -> createUploadedFileView(uploadedFile, pwaApplicationDetail))
        .toList();
  }

  private UploadedFileView createUploadedFileView(UploadedFile uploadedFile, PwaApplicationDetail pwaApplicationDetail) {
    return new UploadedFileView(
        null,
        uploadedFile.getName(),
        uploadedFile.getContentLength(),
        uploadedFile.getDescription(),
        uploadedFile.getUploadedAt(),
        ReverseRouter.route(on(PadFileManagementRestController.class).download(pwaApplicationDetail.getId(), uploadedFile.getId()))
    );
  }

  private String getUsageId(PwaApplicationDetail pwaApplicationDetail) {
    return pwaApplicationDetail.getId().toString();
  }

  private String getUsageType() {
    return PwaApplicationDetail.class.getSimpleName();
  }

}
