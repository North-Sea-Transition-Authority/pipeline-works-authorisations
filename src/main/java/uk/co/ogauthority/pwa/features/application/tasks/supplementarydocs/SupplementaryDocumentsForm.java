package uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs;

import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;

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
