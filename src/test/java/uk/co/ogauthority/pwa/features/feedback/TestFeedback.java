package uk.co.ogauthority.pwa.features.feedback;

import java.time.Instant;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackManagementServiceFeedback;

public class TestFeedback implements FeedbackManagementServiceFeedback {

  private final Integer id;
  private final String submitterName;
  private final String submitterEmail;
  private final String serviceRating;
  private final String serviceImprovement;
  private final Instant givenDatetime;
  private final Integer transactionId;
  private final String transactionReference;
  private final String transactionLink;

  public TestFeedback(Integer id, String submitterName, String submitterEmail, String serviceRating,
                      String serviceImprovement, Instant givenDatetime, Integer transactionId,
                      String transactionReference, String transactionLink) {
    this.id = id;
    this.submitterName = submitterName;
    this.submitterEmail = submitterEmail;
    this.serviceRating = serviceRating;
    this.serviceImprovement = serviceImprovement;
    this.givenDatetime = givenDatetime;
    this.transactionId = transactionId;
    this.transactionReference = transactionReference;
    this.transactionLink = transactionLink;
  }

  public Integer getId() {
    return id;
  }

  public String getSubmitterName() {
    return submitterName;
  }

  public String getSubmitterEmail() {
    return submitterEmail;
  }

  public String getServiceRating() {
    return serviceRating;
  }

  public String getComment() {
    return serviceImprovement;
  }

  public Instant getGivenDatetime() {
    return givenDatetime;
  }

  public Integer getTransactionId() {
    return transactionId;
  }

  public String getTransactionReference() {
    return transactionReference;
  }

  public String getTransactionLink() {
    return transactionLink;
  }

}