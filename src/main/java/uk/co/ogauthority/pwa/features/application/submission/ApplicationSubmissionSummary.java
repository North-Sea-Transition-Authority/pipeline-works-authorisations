package uk.co.ogauthority.pwa.features.application.submission;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;

public class ApplicationSubmissionSummary {

  private final String applicationReference;

  private final LocalDateTime submissionDateTime;

  private final String submittedBy;

  private final boolean isFirstVersion;

  public ApplicationSubmissionSummary(PwaApplication application,
                                      boolean isFirstVersion,
                                      LocalDateTime submissionDateTime,
                                      String submittedBy) {
    this.applicationReference = application.getAppReference();
    this.isFirstVersion = isFirstVersion;
    this.submissionDateTime = submissionDateTime;
    this.submittedBy = submittedBy;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public boolean getIsFirstVersion() {
    return isFirstVersion;
  }

  public LocalDateTime getSubmissionDateTime() {
    return submissionDateTime;
  }

  public String getSubmittedBy() {
    return submittedBy;
  }

  public String getFormattedSubmissionTime() {
    var stringFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    return this.submissionDateTime.format(stringFormat);
  }
}
