package uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters;


import java.util.Objects;
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


  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PartnerLettersForm that = (PartnerLettersForm) o;
    return Objects.equals(partnerLettersRequired, that.partnerLettersRequired)
        && Objects.equals(partnerLettersConfirmed, that.partnerLettersConfirmed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(partnerLettersRequired, partnerLettersConfirmed);
  }
}
