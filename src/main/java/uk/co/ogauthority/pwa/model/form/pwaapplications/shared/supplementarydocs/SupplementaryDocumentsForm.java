package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.supplementarydocs;

import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;

public class SupplementaryDocumentsForm extends UploadMultipleFilesWithDescriptionForm {

  private Boolean hasFilesToUpload;

  public SupplementaryDocumentsForm() {
  }

  public Boolean getHasFilesToUpload() {
    return hasFilesToUpload;
  }

  public void setHasFilesToUpload(Boolean hasFilesToUpload) {
    this.hasFilesToUpload = hasFilesToUpload;
  }
}
