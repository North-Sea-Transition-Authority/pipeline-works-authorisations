package uk.co.ogauthority.pwa.model.view.asbuilt;

import java.time.Instant;
import java.time.LocalDate;
import uk.co.ogauthority.pwa.util.DateUtils;

/**
 * A class to contains user displayable info about an individual as-built pipeline notification submission within a notification group.
 */
public class AsBuiltNotificationView {

  private final String pipelineNumber;
  private final String pipelineTypeDisplay;
  private final String submittedByPersonName;
  private final Instant submittedOnInstant;
  private String submittedOnInstantDisplay;
  private final String asBuiltNotificationStatusDisplay;
  private final LocalDate dateLaid;
  private String dateLaidDisplay;
  private final LocalDate expectedLaidDate;
  private String expectedLaidDateDisplay;
  private final LocalDate dateBroughtIntoUse;
  private String dateBroughtIntoUseDisplay;
  private final String submissionLink;

  public AsBuiltNotificationView(String pipelineNumber, String pipelineTypeDisplay, String submittedByPersonName,
                                 Instant submittedOnInstant, String asBuiltNotificationStatusDisplay, LocalDate dateLaid,
                                 LocalDate expectedLaidDate, LocalDate dateBroughtIntoUse, String submissionLink) {
    this.pipelineNumber = pipelineNumber;
    this.pipelineTypeDisplay = pipelineTypeDisplay;
    this.submittedByPersonName = submittedByPersonName;
    this.submittedOnInstant = submittedOnInstant;
    this.asBuiltNotificationStatusDisplay = asBuiltNotificationStatusDisplay;
    this.dateLaid = dateLaid;
    this.expectedLaidDate = expectedLaidDate;
    this.dateBroughtIntoUse = dateBroughtIntoUse;
    this.submissionLink = submissionLink;
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

  public Instant getSubmittedOnInstant() {
    return submittedOnInstant;
  }

  public String getSubmittedOnInstantDisplay() {
    return DateUtils.formatDate(submittedOnInstant);
  }

  public String getAsBuiltNotificationStatusDisplay() {
    return asBuiltNotificationStatusDisplay;
  }

  public LocalDate getDateLaid() {
    return dateLaid;
  }

  public String getDateLaidDisplay() {
    return DateUtils.formatDate(dateLaid);
  }

  public LocalDate getExpectedLaidDate() {
    return expectedLaidDate;
  }

  public String getExpectedLaidDateDisplay() {
    return DateUtils.formatDate(expectedLaidDate);
  }

  public LocalDate getDateBroughtIntoUse() {
    return dateBroughtIntoUse;
  }

  public String getDateBroughtIntoUseDisplay() {
    return DateUtils.formatDate(dateBroughtIntoUse);
  }

  public String getSubmissionLink() {
    return submissionLink;
  }

}
