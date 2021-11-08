package uk.co.ogauthority.pwa.service.feedback;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.entity.feedback.Feedback;
import uk.co.ogauthority.pwa.model.form.feedback.FeedbackForm;
import uk.co.ogauthority.pwa.repository.feedback.FeedbackRepository;
import uk.co.ogauthority.pwa.validators.feedback.FeedbackValidator;

@Service
public class FeedbackService {

  private final FeedbackValidator feedbackValidator;
  private final FeedbackRepository feedbackRepository;

  public static final Integer FEEDBACK_CHARACTER_LIMIT = 2000;

  @Autowired
  public FeedbackService(FeedbackValidator feedbackValidator,
                         FeedbackRepository feedbackRepository) {
    this.feedbackValidator = feedbackValidator;
    this.feedbackRepository = feedbackRepository;
  }

  public BindingResult validateFeedbackForm(FeedbackForm form,
                                            BindingResult bindingResult) {
    feedbackValidator.validate(form, bindingResult);
    return bindingResult;
  }


  @Transactional
  public void saveFeedback(Integer pwaApplicationDetailId,
                           FeedbackForm feedbackForm,
                           Person submittingPerson) {

    var feedbackEntity = new Feedback();
    feedbackEntity.setPwaApplicationDetailId(pwaApplicationDetailId);
    feedbackEntity.setRating(feedbackForm.getServiceRating());
    feedbackEntity.setServiceFeedback(feedbackForm.getFeedback());
    feedbackEntity.setSubmitterName(submittingPerson.getFullName());
    feedbackEntity.setSubmitterEmailAddress(submittingPerson.getEmailAddress());
    feedbackEntity.setSubmittedTimestamp(Instant.now());
    feedbackRepository.save(feedbackEntity);
  }
}
