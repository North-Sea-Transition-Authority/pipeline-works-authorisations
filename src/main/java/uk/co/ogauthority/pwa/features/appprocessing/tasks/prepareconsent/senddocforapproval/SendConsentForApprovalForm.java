package uk.co.ogauthority.pwa.features.appprocessing.tasks.prepareconsent.senddocforapproval;

public class SendConsentForApprovalForm {

  private String coverLetterText;

  private Boolean parallelConsentsReviewedIfApplicable;

  public String getCoverLetterText() {
    return coverLetterText;
  }

  public void setCoverLetterText(String coverLetterText) {
    this.coverLetterText = coverLetterText;
  }

  public Boolean getParallelConsentsReviewedIfApplicable() {
    return parallelConsentsReviewedIfApplicable;
  }

  public void setParallelConsentsReviewedIfApplicable(Boolean parallelConsentsReviewedIfApplicable) {
    this.parallelConsentsReviewedIfApplicable = parallelConsentsReviewedIfApplicable;
  }
}
