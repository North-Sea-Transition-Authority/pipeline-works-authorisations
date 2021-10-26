package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;

import java.util.List;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;

public class PartnerLettersView {

  private final Boolean partnerLettersRequired;
  private final Boolean partnerLettersConfirmed;
  private final List<UploadedFileView> uploadedLetterFileViews;

  public PartnerLettersView(Boolean partnerLettersRequired, Boolean partnerLettersConfirmed,
                            List<UploadedFileView> uploadedLetterFileViews) {
    this.partnerLettersRequired = partnerLettersRequired;
    this.partnerLettersConfirmed = partnerLettersConfirmed;
    this.uploadedLetterFileViews = uploadedLetterFileViews;
  }


  public Boolean getPartnerLettersRequired() {
    return partnerLettersRequired;
  }

  public Boolean getPartnerLettersConfirmed() {
    return partnerLettersConfirmed;
  }

  public List<UploadedFileView> getUploadedLetterFileViews() {
    return uploadedLetterFileViews;
  }
}
