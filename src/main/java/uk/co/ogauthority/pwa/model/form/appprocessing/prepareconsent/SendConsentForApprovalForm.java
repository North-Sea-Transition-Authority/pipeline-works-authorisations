package uk.co.ogauthority.pwa.model.form.appprocessing.prepareconsent;

import javax.validation.constraints.NotBlank;

public class SendConsentForApprovalForm {

  @NotBlank(message = "Enter some email cover letter text")
  private String coverLetterText;

  public String getCoverLetterText() {
    return coverLetterText;
  }

  public void setCoverLetterText(String coverLetterText) {
    this.coverLetterText = coverLetterText;
  }

}
