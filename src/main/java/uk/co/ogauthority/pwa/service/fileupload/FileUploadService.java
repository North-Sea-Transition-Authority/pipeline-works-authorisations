package uk.co.ogauthority.pwa.service.fileupload;

import com.google.common.base.Stopwatch;
import jakarta.transaction.Transactional;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.ogauthority.pwa.config.MetricsProvider;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadProperties;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.config.fileupload.UploadErrorType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.exception.TempFileException;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFileOld;
import uk.co.ogauthority.pwa.repository.files.UploadedFileRepositoryOld;
import uk.co.ogauthority.pwa.service.images.ImageScalingService;
import uk.co.ogauthority.pwa.util.MetricTimerUtils;

@Service
public class FileUploadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadService.class);

  private final FileUploadProperties fileUploadProperties;
  private final UploadedFileRepositoryOld uploadedFileRepositoryOld;
  private final VirusCheckService virusCheckService;
  private final ImageScalingService imageScalingService;
  private final MetricsProvider metricsProvider;

  public enum ScaleImage { YES, NO }

  @Autowired
  public FileUploadService(FileUploadProperties fileUploadProperties,
                           UploadedFileRepositoryOld uploaded,
                           VirusCheckService virusCheckService,
                           ImageScalingService imageScalingService,
                           MetricsProvider metricsProvider) {
    this.fileUploadProperties = fileUploadProperties;
    this.uploadedFileRepositoryOld = uploaded;
    this.virusCheckService = virusCheckService;
    this.imageScalingService = imageScalingService;
    this.metricsProvider = metricsProvider;
  }

  public UploadFileWithDescriptionForm createUploadFileWithDescriptionFormFromView(UploadedFileView uploadedFileView) {
    var form = new UploadFileWithDescriptionForm();
    form.setUploadedFileDescription(uploadedFileView.getFileDescription());
    form.setUploadedFileId(uploadedFileView.getFileId());
    form.setUploadedFileInstant(uploadedFileView.getFileUploadedTime());
    return form;
  }

  public UploadedFileOld getFileById(String fileId) {
    return uploadedFileRepositoryOld.findById(fileId).orElseThrow(
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
    return processFileUpload(file, user, fileUploadProperties.getAllowedExtensions(), ScaleImage.NO);
  }

  @Transactional
  public FileUploadResult processImageUpload(MultipartFile file, WebUserAccount user) {
    return processFileUpload(file, user, fileUploadProperties.getAllowedImageExtensions(), ScaleImage.YES);
  }

  private FileUploadResult processFileUpload(MultipartFile file,
                                             WebUserAccount user,
                                             List<String> allowedExtensions,
                                             ScaleImage scaleImage) {

    var stopwatch = Stopwatch.createStarted();

    String fileId = generateFileId();
    String filename = sanitiseFilename(Objects.requireNonNull(file.getOriginalFilename()));

    if (!isFileExtensionAllowed(filename, allowedExtensions)) {
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
      UploadedFileOld uploadedFile = new UploadedFileOld(fileId, filename, blob, file.getContentType(),
          file.getSize(), Instant.now(), user.getWuaId(), user.getWuaId(), FileUploadStatus.CURRENT);

      if (uploadedFile.getContentType().contains("image") && scaleImage == ScaleImage.YES) {
        var scaledImageBaos = imageScalingService.scaleImage(uploadedFile);
        Blob scaledBlob = new SerialBlob(scaledImageBaos.toByteArray());
        uploadedFile.setScaledImageData(scaledBlob);
        uploadedFileRepositoryOld.save(uploadedFile);
        scaledBlob.free();
        scaledImageBaos.close();
      } else {
        uploadedFileRepositoryOld.save(uploadedFile);
      }

      blob.free();

      var timerLogMessage = String.format("File '%s' uploaded. UF_ID = %s Uploading WUA id = %s",
          uploadedFile.getFileName(), uploadedFile.getFileId(), user.getWuaId());
      MetricTimerUtils.recordTime(stopwatch, LOGGER, metricsProvider.getFileUploadTimer(), timerLogMessage);

      return FileUploadResult.generateSuccessfulFileUploadResult(uploadedFile.getFileId(), filename, file.getSize(),
          file.getContentType());

    } catch (Exception e) {
      LOGGER.error("Failed to upload file: " + filename, e);
      return FileUploadResult.generateFailedFileUploadResult(filename, file, UploadErrorType.INTERNAL_SERVER_ERROR);
    }
  }




  private boolean isFileExtensionAllowed(String filename, List<String> allowedExtensions) {
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
    UploadedFileOld file = getFileById(fileId);
    return processDelete(file, lastUpdatedByWua);
  }

  private FileDeleteResult processDelete(UploadedFileOld file, WebUserAccount lastUpdatedByWua) {
    try {
      deleteFile(file, lastUpdatedByWua);
      return FileDeleteResult.generateSuccessfulFileDeleteResult(file.getFileId());
    } catch (Exception e) {
      LOGGER.error("Failed to delete file: " + file.getFileName(), e);
      return FileDeleteResult.generateFailedFileDeleteResult(file.getFileId());
    }
  }

  private void deleteFile(UploadedFileOld file, WebUserAccount lastUpdatedByWua) {
    file.setStatus(FileUploadStatus.DELETED);
    file.setLastUpdatedByWuaId(lastUpdatedByWua.getWuaId());
    uploadedFileRepositoryOld.save(file);
  }

  public List<UploadedFileOld> getFilesByIds(Collection<String> fileIds) {
    return uploadedFileRepositoryOld.getAllByFileIdIn(fileIds);
  }

  /**
   * Creates a temporary file from an UploadedFile object. Consumers must delete the
   * temporary file after they have finished using it.
   */
  public File createTempFile(UploadedFileOld uploadedFile) {

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
