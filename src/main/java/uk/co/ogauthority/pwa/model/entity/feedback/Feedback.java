package uk.co.ogauthority.pwa.model.entity.feedback;

import java.time.Instant;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.co.ogauthority.pwa.model.enums.feedback.ServiceFeedbackRating;

@Entity
@Table(name = "service_feedback")
public class Feedback {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer pwaApplicationDetailId;

  @Enumerated(EnumType.STRING)
  private ServiceFeedbackRating rating;

  @Column(name = "feedback")
  private String serviceFeedback;

  private String submitterName;

  private String submitterEmailAddress;

  private Instant submittedTimestamp;


  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getPwaApplicationDetailId() {
    return pwaApplicationDetailId;
  }

  public void setPwaApplicationDetailId(Integer pwaApplicationDetailId) {
    this.pwaApplicationDetailId = pwaApplicationDetailId;
  }

  public ServiceFeedbackRating getRating() {
    return rating;
  }

  public void setRating(ServiceFeedbackRating rating) {
    this.rating = rating;
  }

  public String getServiceFeedback() {
    return serviceFeedback;
  }

  public void setServiceFeedback(String serviceFeedback) {
    this.serviceFeedback = serviceFeedback;
  }

  public String getSubmitterName() {
    return submitterName;
  }

  public void setSubmitterName(String submitterName) {
    this.submitterName = submitterName;
  }

  public String getSubmitterEmailAddress() {
    return submitterEmailAddress;
  }

  public void setSubmitterEmailAddress(String submitterEmailAddress) {
    this.submitterEmailAddress = submitterEmailAddress;
  }

  public Instant getSubmittedTimestamp() {
    return submittedTimestamp;
  }

  public void setSubmittedTimestamp(Instant submittedTimestamp) {
    this.submittedTimestamp = submittedTimestamp;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }
    if (!(o instanceof Feedback)) {
      return false;
    }
    Feedback that = (Feedback) o;
    return Objects.equals(id, that.id)
        && Objects.equals(pwaApplicationDetailId, that.pwaApplicationDetailId)
        && rating == that.rating
        && Objects.equals(serviceFeedback, that.serviceFeedback)
        && Objects.equals(submitterName, that.submitterName)
        && Objects.equals(submitterEmailAddress, that.submitterEmailAddress)
        && Objects.equals(submittedTimestamp, that.submittedTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        pwaApplicationDetailId,
        rating,
        serviceFeedback,
        submitterName,
        submitterEmailAddress,
        submittedTimestamp
    );
  }

}
