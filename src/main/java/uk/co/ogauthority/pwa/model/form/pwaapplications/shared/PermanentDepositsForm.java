package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;

public class PermanentDepositsForm extends UploadMultipleFilesWithDescriptionForm {


  private String selectedPipelines;


  public String getSelectedPipelines() {
    return selectedPipelines;
  }

  public void setSelectedPipelines(String selectedPipelines) {
    this.selectedPipelines = selectedPipelines;
  }
}
