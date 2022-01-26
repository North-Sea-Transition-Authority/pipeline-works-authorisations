package uk.co.ogauthority.pwa.features.feedback;

import java.time.Clock;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.fivium.feedbackmanagementservice.client.CannotSendFeedbackException;
import uk.co.fivium.feedbackmanagementservice.client.FeedbackClientService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.model.enums.ServiceContactDetail;
import uk.co.ogauthority.pwa.model.form.feedback.FeedbackForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.validators.feedback.FeedbackValidator;

@Service
public class FeedbackService {

  public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone((ZoneId.systemDefault()));

  private static final Logger LOGGER = LoggerFactory.getLogger(FeedbackService.class);

  private final FeedbackValidator feedbackValidator;
  private final FeedbackClientService feedbackClientService;
  private final PwaApplicationDetailRepository pwaApplicationDetailRepository;
  private final CaseLinkService caseLinkService;
  private final FeedbackEmailService feedbackEmailService;
  private final String serviceName;
  private final Clock utcClock;

  public static final Integer FEEDBACK_CHARACTER_LIMIT = 2000;

  @Autowired
  public FeedbackService(FeedbackValidator feedbackValidator,
                         FeedbackClientService feedbackClientService,
                         PwaApplicationDetailRepository pwaApplicationDetailRepository,
                         CaseLinkService caseLinkService,
                         FeedbackEmailService feedbackEmailService,
                         @Value("${fms.service.name}") String serviceName,
                         @Qualifier("utcClock") Clock utcClock) {
    this.feedbackValidator = feedbackValidator;
    this.feedbackClientService = feedbackClientService;
    this.pwaApplicationDetailRepository = pwaApplicationDetailRepository;
    this.caseLinkService = caseLinkService;
    this.feedbackEmailService = feedbackEmailService;
    this.serviceName = serviceName;
    this.utcClock = utcClock;
  }

  public BindingResult validateFeedbackForm(FeedbackForm form,
                                            BindingResult bindingResult) {
    feedbackValidator.validate(form, bindingResult);
    return bindingResult;
  }

  public void saveFeedback(Integer pwaApplicationDetailId, FeedbackForm feedbackForm, Person submittingPerson) {
    var feedback = new Feedback();
    setCommonFeedbackAttributes(feedback, feedbackForm, submittingPerson);

    var applicationDetail = pwaApplicationDetailRepository
        .findByIdAndTipFlagIsTrue(pwaApplicationDetailId)
        .orElseThrow(() -> new EntityNotFoundException(
            String.format("Unable to find application detail with id: %s", pwaApplicationDetailId))
        );
    feedback.setTransactionId(applicationDetail.getPwaApplication().getId());
    feedback.setTransactionReference(applicationDetail.getPwaApplicationRef());
    feedback.setTransactionLink(caseLinkService.generateCaseManagementLink(applicationDetail.getPwaApplication()));

    saveToClient(feedback);
  }

  public void saveFeedback(FeedbackForm feedbackForm, Person submittingPerson) {
    var feedback = new Feedback();
    setCommonFeedbackAttributes(feedback, feedbackForm, submittingPerson);
    saveToClient(feedback);
  }

  private void saveToClient(Feedback feedback) {
    try {
      feedbackClientService.saveFeedback(feedback);
    } catch (CannotSendFeedbackException e) {
      feedbackEmailService.sendFeedbackFailedToSendEmail(getFeedbackContent(feedback),
          ServiceContactDetail.TECHNICAL_SUPPORT.getEmailAddress(),
          ServiceContactDetail.TECHNICAL_SUPPORT.getServiceName());
      LOGGER.warn(String.format("Feedback failed to send: %s", e.getMessage()));
    }
  }

  private void setCommonFeedbackAttributes(Feedback feedback, FeedbackForm feedbackForm, Person submittingPerson) {
    feedback.setSubmitterName(submittingPerson.getFullName());
    feedback.setSubmitterEmail(submittingPerson.getEmailAddress());
    feedback.setServiceRating(feedbackForm.getServiceRating().name());
    feedback.setComment(feedbackForm.getFeedback());
    feedback.setGivenDatetime(utcClock.instant());
  }

  private String getFeedbackContent(Feedback feedback) {
    var feedbackContent = "Submitter name: " + feedback.getSubmitterName() +
        "\nSubmitter email: " + feedback.getSubmitterEmail() +
        "\nService rating: " + feedback.getServiceRating();

    if (feedback.getComment() != null) {
      feedbackContent += "\nService improvement: " + feedback.getComment();
    }

    feedbackContent += "\nDate and time: " + formatter.format(feedback.getGivenDatetime()) +
        "\nService name: " + serviceName;

    if (feedback.getTransactionId() != null) {
      feedbackContent += "\nTransaction ID: " + feedback.getTransactionId() +
          "\nTransaction reference: " + feedback.getTransactionReference() +
          "\nTransaction link: " + feedback.getTransactionLink();
    }
    return feedbackContent;
  }

}