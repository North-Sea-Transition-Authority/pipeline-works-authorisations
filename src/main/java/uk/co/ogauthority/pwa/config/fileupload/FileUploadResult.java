package uk.co.ogauthority.pwa.config.fileupload;

import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;


/**
 * This class represents a document upload response sent as a JSON object to Javascript when a file is uploaded.
 */
public class FileUploadResult {

  private final String fileId;
  private final String fileName;
  private final Long size;
  private final String contentType;
  private final UploadErrorType errorType;

  public FileUploadResult(String fileId, String fileName, long size, String contentType, UploadErrorType errorType) {
    this.fileId = fileId;
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
    this.errorType = errorType;
  }

  public static FileUploadResult generateSuccessfulFileUploadResult(String fileId,
                                                                    String fileName,
                                                                    long size,
                                                                    String contentType) {
    return new FileUploadResult(fileId, fileName, size, contentType, null);
  }

  public static FileUploadResult generateFailedFileUploadResult(String fileName, long size,
                                                                String contentType,
                                                                UploadErrorType errorType) {
    return new FileUploadResult(null, fileName, size, contentType, errorType);
  }

  public static FileUploadResult generateFailedFileUploadResult(String sanitisedfileName,
                                                                MultipartFile multipartFile,
                                                                UploadErrorType errorType) {
    return new FileUploadResult(null, sanitisedfileName, multipartFile.getSize(), multipartFile.getContentType(),
        errorType);
  }

  public static FileUploadResult generateSuccessfulFileDeleteResult(String fileId,
                                                                    String fileName,
                                                                    long size,
                                                                    String contentType) {
    return new FileUploadResult(fileId, fileName, size, contentType, null);
  }

  public static FileUploadResult generateFailedFromResult(FileUploadResult fileUploadResult,
                                                          UploadErrorType uploadErrorType) {
    return new FileUploadResult(fileUploadResult.getFileId().orElse(null),
        fileUploadResult.getFileName(),
        fileUploadResult.getSize(),
        fileUploadResult.getContentType(),
        uploadErrorType);
  }

  public Optional<String> getFileId() {
    return Optional.ofNullable(fileId);
  }

  public String getFileName() {
    return fileName;
  }

  public Long getSize() {
    return size;
  }

  public String getContentType() {
    return contentType;
  }

  public Optional<UploadErrorType> getErrorType() {
    return Optional.ofNullable(errorType);
  }

  public Optional<String> getErrorMessage() {
    return getErrorType().map(UploadErrorType::getErrorMessage);
  }

  public boolean isValid() {
    return this.errorType == null && this.fileId != null;
  }
}

