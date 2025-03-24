package uk.co.ogauthority.pwa.features.application.tasks.supplementarydocs;

import uk.co.ogauthority.pwa.features.filemanagement.FileUploadForm;

public class SupplementaryDocumentsForm extends FileUploadForm {

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
