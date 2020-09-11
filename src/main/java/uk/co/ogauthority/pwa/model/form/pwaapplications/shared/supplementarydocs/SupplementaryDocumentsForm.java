package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.supplementarydocs;

import javax.validation.constraints.NotNull;
import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;

public class SupplementaryDocumentsForm extends UploadMultipleFilesWithDescriptionForm {

  @NotNull(message = "Select yes if you want to upload documents")
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
