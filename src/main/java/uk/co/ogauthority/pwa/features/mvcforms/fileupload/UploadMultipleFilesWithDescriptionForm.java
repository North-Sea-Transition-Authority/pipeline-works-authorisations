package uk.co.ogauthority.pwa.features.mvcforms.fileupload;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;


public abstract class UploadMultipleFilesWithDescriptionForm {

  @Valid
  // Full validation implies that the list requires at least one element
  @NotEmpty(groups = {MandatoryUploadValidation.class}, message = "Upload at least one file")
  List<UploadFileWithDescriptionForm> uploadedFileWithDescriptionForms;

  public UploadMultipleFilesWithDescriptionForm() {
    this.uploadedFileWithDescriptionForms = new ArrayList<>();
  }

  public List<UploadFileWithDescriptionForm> getUploadedFileWithDescriptionForms() {
    return uploadedFileWithDescriptionForms;
  }

  public void setUploadedFileWithDescriptionForms(
      List<UploadFileWithDescriptionForm> uploadedFileWithDescriptionForms) {
    this.uploadedFileWithDescriptionForms = uploadedFileWithDescriptionForms;
  }

  /**
   * Gets file forms for files that actually exist (i.e. have a file id) in order to validate.
   */
  public List<UploadFileWithDescriptionForm> getFileFormsForValidation() {
    return uploadedFileWithDescriptionForms.stream()
        .filter(f -> f.getUploadedFileId() != null)
        .collect(Collectors.toList());
  }

}
