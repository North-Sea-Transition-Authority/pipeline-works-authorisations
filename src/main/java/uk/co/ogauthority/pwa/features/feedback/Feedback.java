package uk.co.ogauthority.pwa.features.feedback;

import java.time.Instant;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackManagementServiceFeedback;

public class Feedback implements FeedbackManagementServiceFeedback {

  private String submitterName;
  private String submitterEmail;
  private String serviceRating;
  private String comment;
  private Instant givenDatetime;
  private Integer transactionId;
  private String transactionReference;
  private String transactionLink;

  public String getSubmitterName() {
    return submitterName;
  }

  public void setSubmitterName(String submitterName) {
    this.submitterName = submitterName;
  }

  public String getSubmitterEmail() {
    return submitterEmail;
  }

  public void setSubmitterEmail(String submitterEmail) {
    this.submitterEmail = submitterEmail;
  }


  public String getServiceRating() {
    return serviceRating;
  }

  public void setServiceRating(String serviceRating) {
    this.serviceRating = serviceRating;
  }


  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }


  public Instant getGivenDatetime() {
    return givenDatetime;
  }

  public void setGivenDatetime(Instant givenDatetime) {
    this.givenDatetime = givenDatetime;
  }


  public Integer getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(Integer transactionId) {
    this.transactionId = transactionId;
  }


  public String getTransactionReference() {
    return transactionReference;
  }

  public void setTransactionReference(String transactionReference) {
    this.transactionReference = transactionReference;
  }

  public String getTransactionLink() {
    return transactionLink;
  }

  public void setTransactionLink(String transactionLink) {
    this.transactionLink = transactionLink;
  }
}