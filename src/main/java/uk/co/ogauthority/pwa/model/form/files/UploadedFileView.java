package uk.co.ogauthority.pwa.model.form.files;

import java.time.Instant;
import java.util.Objects;

/**
 * Simple class to represent an UploadedFile Entity.
 */
public class UploadedFileView {

  private String fileId;
  private String fileName;
  private String fileSize;
  private String fileDescription;
  private Instant fileUploadedTime;
  private String fileUrl;

  public UploadedFileView(String fileId, String fileName, String fileSize, String fileDescription,
                          Instant fileUploadedTime, String fileUrl) {
    this.fileId = fileId;
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.fileDescription = fileDescription;
    this.fileUploadedTime = fileUploadedTime;
    this.fileUrl = fileUrl;
  }

  public UploadedFileView(String fileId, String fileName, String fileSize, String fileDescription,
                          Instant fileUploadedTime) {
    this(fileId, fileName, fileSize, fileDescription, fileUploadedTime, null);
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileSize() {
    return fileSize;
  }

  public void setFileSize(String fileSize) {
    this.fileSize = fileSize;
  }

  public String getFileDescription() {
    return fileDescription;
  }

  public void setFileDescription(String fileDescription) {
    this.fileDescription = fileDescription;
  }

  public Instant getFileUploadedTime() {
    return fileUploadedTime;
  }

  public void setFileUploadedTime(Instant fileUploadedTime) {
    this.fileUploadedTime = fileUploadedTime;
  }

  public String getFileUrl() {
    return fileUrl;
  }

  public void setFileUrl(String fileUrl) {
    this.fileUrl = fileUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;

    }
    UploadedFileView that = (UploadedFileView) o;
    return Objects.equals(fileId, that.fileId)
        && Objects.equals(fileName, that.fileName)
        && Objects.equals(fileSize, that.fileSize)
        && Objects.equals(fileDescription, that.fileDescription)
        && Objects.equals(fileUploadedTime, that.fileUploadedTime)
        && Objects.equals(fileUrl, that.fileUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fileId, fileName, fileSize, fileDescription, fileUploadedTime, fileUrl);
  }
}
