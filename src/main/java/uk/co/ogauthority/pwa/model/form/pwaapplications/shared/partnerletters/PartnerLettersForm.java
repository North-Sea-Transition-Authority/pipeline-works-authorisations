package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters;


import uk.co.ogauthority.pwa.model.form.files.UploadMultipleFilesWithDescriptionForm;

public class PartnerLettersForm extends UploadMultipleFilesWithDescriptionForm {

  private Boolean partnerLettersRequired;
  private Boolean partnerLettersConfirmed;


  public Boolean getPartnerLettersRequired() {
    return partnerLettersRequired;
  }

  public void setPartnerLettersRequired(Boolean partnerLettersRequired) {
    this.partnerLettersRequired = partnerLettersRequired;
  }

  public Boolean getPartnerLettersConfirmed() {
    return partnerLettersConfirmed;
  }

  public void setPartnerLettersConfirmed(Boolean partnerLettersConfirmed) {
    this.partnerLettersConfirmed = partnerLettersConfirmed;
  }

}
