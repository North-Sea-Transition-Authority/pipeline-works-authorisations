package uk.co.ogauthority.pwa.model.form.files;

import java.time.Instant;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;

/**
 * Form which allows a single file with a mandatory description to be uploaded.
 */
public class UploadFileWithDescriptionForm {

  private String uploadedFileId;

  @NotEmpty(message = "File must have a description")
  private String uploadedFileDescription;

  private Instant uploadedFileInstant;

  public UploadFileWithDescriptionForm() {
  }

  public UploadFileWithDescriptionForm(String fileId, String fileDescription, Instant uploadedFileInstant) {
    this.uploadedFileId = fileId;
    this.uploadedFileDescription = fileDescription;
    this.uploadedFileInstant = uploadedFileInstant;
  }

  public String getUploadedFileId() {
    return uploadedFileId;
  }

  public void setUploadedFileId(String uploadedFileId) {
    this.uploadedFileId = uploadedFileId;
  }

  public String getUploadedFileDescription() {
    return uploadedFileDescription;
  }

  public void setUploadedFileDescription(String uploadedFileDescription) {
    this.uploadedFileDescription = uploadedFileDescription;
  }

  public Instant getUploadedFileInstant() {
    return uploadedFileInstant;
  }

  public void setUploadedFileInstant(Instant uploadedFileInstant) {
    this.uploadedFileInstant = uploadedFileInstant;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UploadFileWithDescriptionForm that = (UploadFileWithDescriptionForm) o;
    return Objects.equals(uploadedFileId, that.uploadedFileId)
        && Objects.equals(uploadedFileDescription, that.uploadedFileDescription)
        && Objects.equals(uploadedFileInstant, that.uploadedFileInstant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uploadedFileId, uploadedFileDescription, uploadedFileInstant);
  }
}
