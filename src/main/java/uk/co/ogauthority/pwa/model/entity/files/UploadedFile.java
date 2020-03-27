package uk.co.ogauthority.pwa.model.entity.files;

import java.sql.Blob;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;


@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

  @Id
  private String fileId;

  private String fileName;

  @Lob
  @Column(updatable = false)
  private Blob fileData;

  private String contentType;

  private Long fileSize;

  private Instant uploadDatetime;

  private Integer uploadedByWuaId;

  @Column(name = "last_updated_by_wua_id")
  private Integer lastUpdatedByWuaId;

  @Enumerated(EnumType.STRING)
  private FileUploadStatus status;

  public UploadedFile() {
  }

  public UploadedFile(String fileId, String fileName) {
    this.fileId = fileId;
    this.fileName = fileName;
  }

  public UploadedFile(String fileId,
                      String fileName,
                      Blob fileData,
                      String contentType,
                      Long fileSize,
                      Instant uploadDatetime,
                      Integer uploadedByWuaId,
                      Integer lastUpdatedByWuaId,
                      FileUploadStatus status) {
    this.fileId = fileId;
    this.fileName = fileName;
    this.fileData = fileData;
    this.contentType = contentType;
    this.fileSize = fileSize;
    this.uploadDatetime = uploadDatetime;
    this.uploadedByWuaId = uploadedByWuaId;
    this.lastUpdatedByWuaId = lastUpdatedByWuaId;
    this.status = status;
  }

  public UploadedFile(String fileId,
                      String fileName,
                      String contentType,
                      Long fileSize,
                      Instant uploadDatetime,
                      FileUploadStatus status) {
    this.fileId = fileId;
    this.fileName = fileName;
    this.status = status;
    this.contentType = contentType;
    this.fileSize = fileSize;
    this.uploadDatetime = uploadDatetime;
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

  public Blob getFileData() {
    return fileData;
  }

  public void setFileData(Blob fileData) {
    this.fileData = fileData;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public void setFileSize(Long fileSize) {
    this.fileSize = fileSize;
  }

  public Instant getUploadDatetime() {
    return uploadDatetime;
  }

  public void setUploadDatetime(Instant uploadDatetime) {
    this.uploadDatetime = uploadDatetime;
  }

  public Integer getUploadedByWuaId() {
    return uploadedByWuaId;
  }

  public void setUploadedByWuaId(Integer uploadedByWuaId) {
    this.uploadedByWuaId = uploadedByWuaId;
  }

  public Integer getLastUpdatedByWuaId() {
    return lastUpdatedByWuaId;
  }

  public void setLastUpdatedByWuaId(Integer lastUpdatedByWuaId) {
    this.lastUpdatedByWuaId = lastUpdatedByWuaId;
  }

  public FileUploadStatus getStatus() {
    return status;
  }

  public void setStatus(FileUploadStatus status) {
    this.status = status;
  }
}
