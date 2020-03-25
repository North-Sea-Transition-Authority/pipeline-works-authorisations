package uk.co.ogauthority.pwa.service.fileupload;

import java.sql.Blob;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.rowset.serial.SerialBlob;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.config.fileupload.DeleteOutcomeType;
import uk.co.ogauthority.pwa.config.fileupload.FileDeleteResult;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadProperties;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadResult;
import uk.co.ogauthority.pwa.config.fileupload.UploadErrorType;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.files.FileUploadStatus;
import uk.co.ogauthority.pwa.model.entity.files.UploadedFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.repository.files.UploadedFileRepository;
import uk.co.ogauthority.pwa.service.util.FileDownloadUtils;

@Service
public class FileUploadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadService.class);

  private final FileUploadProperties fileUploadProperties;
  private final UploadedFileRepository uploadedFileRepository;
  private final VirusCheckService virusCheckService;
  private final List<String> allowedExtensions;

  @Autowired
  public FileUploadService(FileUploadProperties fileUploadProperties,
                           UploadedFileRepository uploaded,
                           VirusCheckService virusCheckService) {
    this.fileUploadProperties = fileUploadProperties;
    this.uploadedFileRepository = uploaded;
    this.allowedExtensions = fileUploadProperties.getAllowedExtensions();
    this.virusCheckService = virusCheckService;
  }


  /**
   * This is just temporary, the precise files being downloaded will vary based on context/security etc and
   * that logic should be in its own service.
   */
  public List<UploadedFileView> getAllUploadedFileViews() {
    return IterableUtils.toList(uploadedFileRepository.findAll())
        .stream()
        .filter(file -> FileUploadStatus.CURRENT.equals(file.getStatus()))
        .map(uploadedFile -> {
          String fileSize = FileDownloadUtils.fileSizeFormatter(uploadedFile.getFileSize());
          return new UploadedFileView(uploadedFile.getFileId(), uploadedFile.getFileName(), fileSize, "",
              uploadedFile.getUploadDatetime());
        })
        .collect(Collectors.toList());
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
  public FileUploadResult processUpload(MultipartFile file, AuthenticatedUserAccount user) {
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
      uploadedFileRepository.save(uploadedFile);
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


  public FileDeleteResult deleteUploadedFile(String fileId, AuthenticatedUserAccount lastUpdatedByWua) {
    UploadedFile file = getFileById(fileId);
    return processDelete(file, lastUpdatedByWua);
  }

  private FileDeleteResult processDelete(UploadedFile file, WebUserAccount lastUpdatedByWua) {
    try {
      deleteFile(file, lastUpdatedByWua);
      return FileDeleteResult.generateSuccessfulFileDeleteResult(file.getFileId(), DeleteOutcomeType.SUCCESS);
    } catch (Exception e) {
      LOGGER.error("Failed to delete file: " + file.getFileName(), e);
      return FileDeleteResult.generateFailedFileDeleteResult(file.getFileId(), DeleteOutcomeType.INTERNAL_SERVER_ERROR);
    }
  }

  private void deleteFile(UploadedFile file, WebUserAccount lastUpdatedByWua) {
    file.setStatus(FileUploadStatus.DELETED);
    file.setLastUpdatedByWuaId(lastUpdatedByWua.getWuaId());
    uploadedFileRepository.save(file);
  }
}
