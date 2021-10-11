package uk.co.ogauthority.pwa.service.feedback;

import uk.co.ogauthority.pwa.model.enums.feedback.ServiceFeedbackRating;
import uk.co.ogauthority.pwa.model.form.feedback.FeedbackForm;

public class FeedbackTestUtil {

  private FeedbackTestUtil() {
    throw new IllegalStateException("FeedbackTestUtil is a utility class and should not be instantiated");
  }

  public static FeedbackForm getValidFeedbackForm() {
    var form = new FeedbackForm();
    form.setServiceRating(ServiceFeedbackRating.VERY_SATISFIED);
    form.setFeedback("feedback");
    return form;
  }

}
