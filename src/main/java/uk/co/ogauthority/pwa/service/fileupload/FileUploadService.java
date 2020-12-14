package uk.co.ogauthority.pwa.service.fileupload;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Blob;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.sql.rowset.serial.SerialBlob;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadProperties;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.config.fileupload.UploadErrorType;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.exception.TempFileException;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.repository.files.UploadedFileRepository;
import uk.co.ogauthority.pwa.service.images.ImageScalingService;

@Service
public class FileUploadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadService.class);

  private final FileUploadProperties fileUploadProperties;
  private final UploadedFileRepository uploadedFileRepository;
  private final VirusCheckService virusCheckService;
  private final ImageScalingService imageScalingService;
  private final List<String> allowedExtensions;

  @Autowired
  public FileUploadService(FileUploadProperties fileUploadProperties,
                           UploadedFileRepository uploaded,
                           VirusCheckService virusCheckService,
                           ImageScalingService imageScalingService) {
    this.fileUploadProperties = fileUploadProperties;
    this.uploadedFileRepository = uploaded;
    this.allowedExtensions = fileUploadProperties.getAllowedExtensions();
    this.virusCheckService = virusCheckService;
    this.imageScalingService = imageScalingService;
  }

  public UploadFileWithDescriptionForm createUploadFileWithDescriptionFormFromView(UploadedFileView uploadedFileView) {
    var form = new UploadFileWithDescriptionForm();
    form.setUploadedFileDescription(uploadedFileView.getFileDescription());
    form.setUploadedFileId(uploadedFileView.getFileId());
    form.setUploadedFileInstant(uploadedFileView.getFileUploadedTime());
    return form;
  }

  public UploadedFile getFileById(String fileId) {
    return uploadedFileRepository.findById(fileId).orElseThrow(
        () -> new PwaEntityNotFoundException("File not found. findId: " + fileId));
  }

  private String sanitiseFilename(String filename) {
    return filename.replaceAll(fileUploadProperties.getFileNameFilter(), "_");
  }

  /**
   * Construct file upload result from multipartfile.
   *
   * @param file the multipart file
   * @param user the logged in user
   * @return the FileUploadResult object storing the details of the uploaded file
   */
  @Transactional
  public FileUploadResult processUpload(MultipartFile file, WebUserAccount user) {
    String fileId = generateFileId();
    String filename = sanitiseFilename(Objects.requireNonNull(file.getOriginalFilename()));

    if (!isFileExtensionAllowed(filename)) {
      return FileUploadResult.generateFailedFileUploadResult(filename, file, UploadErrorType.EXTENSION_NOT_ALLOWED);
    }

    if (!isFileSizeAllowed(file.getSize())) {
      return FileUploadResult.generateFailedFileUploadResult(filename, file, UploadErrorType.MAX_FILE_SIZE_EXCEEDED);
    }

    LOGGER.debug("Starting virus scan for file: {}", filename);

    if (virusCheckService.hasVirus(file)) {
      LOGGER.warn("Virus found in uploaded file: {} with fileId: {}", filename, fileId);
      return FileUploadResult.generateFailedFileUploadResult(filename, file, UploadErrorType.VIRUS_FOUND_IN_FILE);
    }

    LOGGER.debug("Completed virus scan for file: {} with fileID: {} Upload request by user: {}", filename, fileId,
        user.getWuaId());

    try {

      Blob blob = new SerialBlob(file.getBytes());
      UploadedFile uploadedFile = new UploadedFile(fileId, filename, blob, file.getContentType(),
          file.getSize(), Instant.now(), user.getWuaId(), user.getWuaId(), FileUploadStatus.CURRENT);

      if (uploadedFile.getContentType().contains("image")) {
        var scaledImageBaos = imageScalingService.scaleImage(uploadedFile);
        Blob scaledBlob = new SerialBlob(scaledImageBaos.toByteArray());
        uploadedFile.setScaledImageData(scaledBlob);
        uploadedFileRepository.save(uploadedFile);
        scaledBlob.free();
      } else {
        uploadedFileRepository.save(uploadedFile);
      }

      blob.free();

      LOGGER.debug("Completed upload of file: {} with fileId: {} Uploaded by User: {}", filename, fileId,
          user.getWuaId());

      return FileUploadResult.generateSuccessfulFileUploadResult(uploadedFile.getFileId(), filename, file.getSize(),
          file.getContentType());

    } catch (Exception e) {
      LOGGER.error("Failed to upload file: " + filename, e);
      return FileUploadResult.generateFailedFileUploadResult(filename, file, UploadErrorType.INTERNAL_SERVER_ERROR);
    }
  }

  private boolean isFileExtensionAllowed(String filename) {
    String lowercase = filename.toLowerCase();
    return allowedExtensions.stream()
        .anyMatch(lowercase::endsWith);
  }

  private boolean isFileSizeAllowed(long fileSize) {
    return fileSize <= fileUploadProperties.getMaxFileSize();
  }

  private String generateFileId() {
    return "file_" + UUID.randomUUID().toString();
  }

  @Transactional
  public FileDeleteResult deleteUploadedFile(String fileId, WebUserAccount lastUpdatedByWua) {
    UploadedFile file = getFileById(fileId);
    return processDelete(file, lastUpdatedByWua);
  }

  private FileDeleteResult processDelete(UploadedFile file, WebUserAccount lastUpdatedByWua) {
    try {
      deleteFile(file, lastUpdatedByWua);
      return FileDeleteResult.generateSuccessfulFileDeleteResult(file.getFileId());
    } catch (Exception e) {
      LOGGER.error("Failed to delete file: " + file.getFileName(), e);
      return FileDeleteResult.generateFailedFileDeleteResult(file.getFileId());
    }
  }

  private void deleteFile(UploadedFile file, WebUserAccount lastUpdatedByWua) {
    file.setStatus(FileUploadStatus.DELETED);
    file.setLastUpdatedByWuaId(lastUpdatedByWua.getWuaId());
    uploadedFileRepository.save(file);
  }

  public List<UploadedFile> getFilesByIds(Collection<String> fileIds) {
    return uploadedFileRepository.getAllByFileIdIn(fileIds);
  }

  /**
   * Creates a temporary file from an UploadedFile object. Consumers must delete the
   * temporary file after they have finished using it.
   */
  public File createTempFile(UploadedFile uploadedFile) {

    String filename = uploadedFile.getFileName().replace(" ", "");
    String extension = filename.substring(filename.lastIndexOf("."));

    File tempFile = null;
    try {
      tempFile = File.createTempFile(uploadedFile.getFileId() + filename, extension);
      var blobToSave = Optional.ofNullable(uploadedFile.getScaledImageData())
          .orElse(uploadedFile.getFileData());
      Files.copy(blobToSave.getBinaryStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return tempFile;
    } catch (Exception e) {
      if (tempFile != null && !tempFile.delete()) {
        LOGGER.error("Failed to delete temp file");
      }
      throw new TempFileException(String.format(
          "Failed to create temporary file for UploadedFile with ID: [%s]", uploadedFile.getFileId()), e);
    }

  }

}
