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
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
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

  public UploadedFile getUploadedFile(PwaApplicationDetail pwaApplicationDetail, UUID fileId) {
    var file = fileService.find(fileId).orElseThrow(() -> getFileNotFoundException(pwaApplicationDetail, fileId));

    throwIfFileDoesNotBelongToApplicationDetail(file, pwaApplicationDetail);
    return file;
  }

  public List<UploadedFile> getUploadedFiles(PwaApplicationDetail pwaApplicationDetail, FileDocumentType documentType) {
    return fileService.findAll(getUsageId(pwaApplicationDetail), getUsageType(), documentType.name());
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
      PwaApplicationDetail pwaApplicationDetail,
      FileDocumentType fileDocumentType
  ) {
    var controller = PadFileManagementRestController.class;

    return fileManagementService.getFileUploadComponentAttributesBuilder(existingFiles, fileDocumentType)
        .withUploadUrl(ReverseRouter.route(on(FileManagementRestController.class).upload(null)))
        .withDownloadUrl(ReverseRouter.route(on(controller).download(pwaApplicationDetail.getId(), null)))
        .withDeleteUrl(ReverseRouter.route(on(controller).delete(pwaApplicationDetail.getId(), null)))
        .build();
  }

  public FileUploadComponentAttributes getFileUploadComponentAttributesForLegacyPadFile(
      List<UploadedFileForm> existingFiles,
      PwaApplicationDetail pwaApplicationDetail,
      FileDocumentType fileDocumentType,
      ApplicationDetailFilePurpose legacyPadFilePurpose
  ) {
    var controller = PadFileManagementRestController.class;

    return fileManagementService.getFileUploadComponentAttributesBuilder(existingFiles, fileDocumentType)
        .withUploadUrl(ReverseRouter.route(on(LegacyPadFileUploadRestController.class)
                .upload(pwaApplicationDetail.getId(), legacyPadFilePurpose.name(), null)))
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
                getUsageId(newApplicationDetail),
                getUsageType(),
                fileDocumentType.name()
            )
        )
    );
  }

  public ResponseStatusException getFileNotFoundException(PwaApplicationDetail pwaApplicationDetail, UUID fileId) {
    return fileManagementService.getFileNotFoundException(fileId, getUsageType(), getUsageId(pwaApplicationDetail));
  }

  public UploadedFileView getUploadedFileView(PwaApplicationDetail pwaApplicationDetail, UUID fileId) {
    return createUploadedFileView(getUploadedFile(pwaApplicationDetail, fileId), pwaApplicationDetail);
  }

  public List<UploadedFileView> getUploadedFileViews(PwaApplicationDetail pwaApplicationDetail, FileDocumentType fileDocumentType) {
    return getUploadedFiles(pwaApplicationDetail, fileDocumentType).stream()
        .map(uploadedFile -> createUploadedFileView(uploadedFile, pwaApplicationDetail))
        .toList();
  }

  public void deleteUploadedFile(UploadedFile uploadedFile) {
    fileService.delete(uploadedFile);
  }

  private UploadedFileView createUploadedFileView(UploadedFile uploadedFile, PwaApplicationDetail pwaApplicationDetail) {
    return new UploadedFileView(
        String.valueOf(uploadedFile.getId()),
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
