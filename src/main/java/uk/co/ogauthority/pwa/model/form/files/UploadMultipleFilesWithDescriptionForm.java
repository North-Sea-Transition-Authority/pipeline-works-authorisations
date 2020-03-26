package uk.co.ogauthority.pwa.model.form.files;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;


public abstract class UploadMultipleFilesWithDescriptionForm {

  @Valid
  List<UploadFileWithDescriptionForm> uploadedFileWithDescriptionForms = new ArrayList<>();

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
