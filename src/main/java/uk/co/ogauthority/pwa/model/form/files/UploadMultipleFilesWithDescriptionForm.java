package uk.co.ogauthority.pwa.model.form.files;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;


public abstract class UploadMultipleFilesWithDescriptionForm {

  @Valid
  // Full validation implies that the list requires at least one element
  @NotEmpty(groups = {MandatoryUploadValidation.class}, message = "You must upload at least one file")
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
}
