package uk.co.ogauthority.pwa.model.form.pwaapplications.shared;

import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;

import java.util.Set;

public class PermanentDepositDrawingsForm extends UploadMultipleFilesWithDescriptionForm {

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
