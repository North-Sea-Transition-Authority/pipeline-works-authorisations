package uk.co.ogauthority.pwa.model.form.feedback;

import java.util.Objects;
import uk.co.ogauthority.pwa.model.enums.feedback.ServiceFeedbackRating;

public class FeedbackForm {

  private ServiceFeedbackRating serviceRating;

  private String feedback;


  public FeedbackForm() {}


  public ServiceFeedbackRating getServiceRating() {
    return serviceRating;
  }

  public void setServiceRating(ServiceFeedbackRating serviceRating) {
    this.serviceRating = serviceRating;
  }

  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FeedbackForm)) {
      return false;
    }
    FeedbackForm that = (FeedbackForm) o;
    return serviceRating == that.serviceRating
        && Objects.equals(feedback, that.feedback);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        serviceRating,
        feedback
    );
  }
}
