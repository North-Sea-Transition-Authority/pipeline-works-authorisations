package uk.co.ogauthority.pwa.service.asbuilt.view;

import java.time.Instant;
import java.time.LocalDate;
import uk.co.ogauthority.pwa.util.DateUtils;

/**
 * A class to contains user displayable info about an individual as-built pipeline notification submission within a notification group.
 */
public class AsBuiltNotificationView {

  private final String asBuiltGroupReference;
  private final String pipelineNumber;
  private final String pipelineTypeDisplay;
  private final String submittedByPersonName;
  private final String submittedByPersonEmail;
  private final Instant submittedOnInstant;
  private String submittedOnInstantDisplay;
  private final String asBuiltNotificationStatusDisplay;
  private final LocalDate dateWorkCompleted;
  private String dateWorkCompletedDisplay;
  private final LocalDate expectedWorkCompletedDate;
  private String expectedWorkCompletedDateDisplay;
  private final LocalDate dateBroughtIntoUse;
  private String dateBroughtIntoUseDisplay;
  private final String ogaSubmissionReason;
  private final String submissionLink;

  AsBuiltNotificationView(String asBuiltGroupReference,
                          String pipelineNumber,
                          String pipelineTypeDisplay,
                          String submittedByPersonName,
                          String submittedByPersonEmail,
                          Instant submittedOnInstant,
                          String asBuiltNotificationStatusDisplay,
                          LocalDate dateWorkCompleted,
                          LocalDate expectedWorkCompletedDate,
                          LocalDate dateBroughtIntoUse,
                          String ogaSubmissionReason,
                          String submissionLink) {
    this.asBuiltGroupReference = asBuiltGroupReference;
    this.pipelineNumber = pipelineNumber;
    this.pipelineTypeDisplay = pipelineTypeDisplay;
    this.submittedByPersonName = submittedByPersonName;
    this.submittedByPersonEmail = submittedByPersonEmail;
    this.submittedOnInstant = submittedOnInstant;
    this.asBuiltNotificationStatusDisplay = asBuiltNotificationStatusDisplay;
    this.dateWorkCompleted = dateWorkCompleted;
    this.expectedWorkCompletedDate = expectedWorkCompletedDate;
    this.dateBroughtIntoUse = dateBroughtIntoUse;
    this.ogaSubmissionReason = ogaSubmissionReason;
    this.submissionLink = submissionLink;
  }

  public String getAsBuiltGroupReference() {
    return asBuiltGroupReference;
  }

  public String getPipelineNumber() {
    return pipelineNumber;
  }

  public String getPipelineTypeDisplay() {
    return pipelineTypeDisplay;
  }

  public String getSubmittedByPersonName() {
    return submittedByPersonName;
  }

  public String getSubmittedByPersonEmail() {
    return submittedByPersonEmail;
  }

  public Instant getSubmittedOnInstant() {
    return submittedOnInstant;
  }

  public String getSubmittedOnInstantDisplay() {
    return DateUtils.formatDateTime(submittedOnInstant);
  }

  public String getAsBuiltNotificationStatusDisplay() {
    return asBuiltNotificationStatusDisplay;
  }

  public LocalDate getDateWorkCompleted() {
    return dateWorkCompleted;
  }

  public String getDateWorkCompletedDisplay() {
    return DateUtils.formatDate(dateWorkCompleted);
  }

  public LocalDate getExpectedWorkCompletedDate() {
    return expectedWorkCompletedDate;
  }

  public String getExpectedWorkCompletedDateDisplay() {
    return DateUtils.formatDate(expectedWorkCompletedDate);
  }

  public LocalDate getDateBroughtIntoUse() {
    return dateBroughtIntoUse;
  }

  public String getDateBroughtIntoUseDisplay() {
    return DateUtils.formatDate(dateBroughtIntoUse);
  }

  public String getOgaSubmissionReason() {
    return ogaSubmissionReason;
  }

  public String getSubmissionLink() {
    return submissionLink;
  }

}
