package uk.co.ogauthority.pwa.features.filemanagement;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUsage;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadComponentAttributes;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;

@Service
public class FileManagementService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileManagementService.class);

  private final FileService fileService;

  FileManagementService(FileService fileService) {
    this.fileService = fileService;
  }

  @Transactional
  void saveFiles(
      Collection<UploadedFileForm> uploadedFileForms,
      String usageId,
      String usageType,
      FileDocumentType fileDocumentType
  ) {
    var fileIds = uploadedFileForms.stream()
        .map(UploadedFileForm::getFileId)
        .toList();

    var uploadedFiles = fileService.findAll(fileIds);

    uploadedFiles.forEach(uploadedFile ->
        throwIfFileDoesNotBelongToUsageType(uploadedFile, usageId, usageType, fileDocumentType.name()));

    var descriptions = FileUploadLibraryUtils.getFileDescriptionsByFileId(uploadedFileForms);

    for (var uploadedFile : uploadedFiles) {
      fileService.updateUsageAndDescription(
          uploadedFile,
          usageBuilder -> buildFileUsage(usageBuilder, usageId, usageType, fileDocumentType.name()),
          descriptions.get(uploadedFile.getId())
      );
    }
  }

  FileUploadForm mapFilesToForm(FileUploadForm form, String usageId, String usageType, FileDocumentType fileDocumentType) {
    var uploadedFileForms = fileService.findAll(usageId, usageType, fileDocumentType.name())
        .stream()
        .map(FileUploadLibraryUtils::asForm)
        .toList();

    form.setUploadedFiles(uploadedFileForms);

    return form;
  }

  FileUploadComponentAttributes.Builder getFileUploadComponentAttributesBuilder(
      List<UploadedFileForm> existingFiles,
      FileDocumentType fileDocumentType
  ) {
    var fileUploadAttributes = fileService.getFileUploadAttributes();

    fileDocumentType.getAllowedExtensions().ifPresent(fileUploadAttributes::withAllowedExtensions);

    return fileUploadAttributes
        .withExistingFiles(existingFiles);
  }

  void throwIfFileDoesNotBelongToUsageType(
      UploadedFile uploadedFile,
      String usageId,
      String usageType,
      @Nullable String documentType
  ) {
    if (Objects.isNull(uploadedFile.getUsageId())
        && Objects.isNull(uploadedFile.getUsageType())
        && Objects.isNull(uploadedFile.getDocumentType())) {
      return;
    }
    var logMessage = "An attempt was made to download a file not linked to the correct %s".formatted(usageType);

    if (!uploadedFile.getUsageId().equals(usageId)) {
      LOGGER.warn(logMessage);
      throw getFileNotFoundException(uploadedFile.getId(), usageType, usageId);
    }

    if (!uploadedFile.getUsageType().equals(usageType)) {
      LOGGER.warn(logMessage);
      throw getFileNotFoundException(uploadedFile.getId(), usageType, usageId);
    }

    if (documentType != null && (!uploadedFile.getDocumentType().equals(documentType))) {
      LOGGER.warn(logMessage);
      throw getFileNotFoundException(uploadedFile.getId(), usageType, usageId);
    }
  }

  ResponseStatusException getFileNotFoundException(UUID fileId, String usageType, String usageId) {
    return new ResponseStatusException(NOT_FOUND, "File %s does not exist for %s %s"
        .formatted(fileId, usageType, usageId));
  }

  FileUsage buildFileUsage(
      FileUsage.Builder fileUsageBuilder,
      String usageId,
      String usageType,
      String documentType
  ) {
    return fileUsageBuilder
        .withUsageId(usageId)
        .withUsageType(usageType)
        .withDocumentType(documentType)
        .build();
  }
}
