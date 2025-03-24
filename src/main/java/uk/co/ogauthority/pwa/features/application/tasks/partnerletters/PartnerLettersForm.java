package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;


import uk.co.ogauthority.pwa.features.filemanagement.FileUploadForm;

public class PartnerLettersForm extends FileUploadForm {

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
