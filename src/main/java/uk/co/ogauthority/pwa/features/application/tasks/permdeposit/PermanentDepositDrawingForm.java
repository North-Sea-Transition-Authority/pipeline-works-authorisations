package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import java.util.Set;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;


public class PermanentDepositDrawingForm extends UploadMultipleFilesWithDescriptionForm {

  private Set<String> selectedDeposits;
  private String reference;


  public Set<String> getSelectedDeposits() {
    return selectedDeposits;
  }

  public void setSelectedDeposits(Set<String> selectedDeposits) {
    this.selectedDeposits = selectedDeposits;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

}
