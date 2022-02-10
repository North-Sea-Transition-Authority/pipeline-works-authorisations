package uk.co.ogauthority.pwa.model.form.publicnotice;

import java.util.Objects;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadMultipleFilesWithDescriptionForm;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeRequestReason;

public class PublicNoticeDraftForm extends UploadMultipleFilesWithDescriptionForm {

  private String coverLetterText;
  private PublicNoticeRequestReason reason;
  private String reasonDescription;


  public String getCoverLetterText() {
    return coverLetterText;
  }

  public void setCoverLetterText(String coverLetterText) {
    this.coverLetterText = coverLetterText;
  }

  public PublicNoticeRequestReason getReason() {
    return reason;
  }

  public void setReason(PublicNoticeRequestReason reason) {
    this.reason = reason;
  }

  public String getReasonDescription() {
    return reasonDescription;
  }

  public void setReasonDescription(String reasonDescription) {
    this.reasonDescription = reasonDescription;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicNoticeDraftForm that = (PublicNoticeDraftForm) o;
    return Objects.equals(coverLetterText, that.coverLetterText)
        && reason == that.reason
        && Objects.equals(reasonDescription, that.reasonDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(coverLetterText, reason, reasonDescription);
  }
}
