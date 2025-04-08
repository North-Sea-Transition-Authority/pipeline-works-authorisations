package uk.co.ogauthority.pwa.features.feedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.features.email.emailproperties.feedback.FeedbackFailedToSendEmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;

@Service
public class FeedbackEmailService {

  private final EmailService emailService;

  @Autowired
  public FeedbackEmailService(EmailService emailService) {
    this.emailService = emailService;
  }

  void sendFeedbackFailedToSendEmail(String feedbackContent, String emailAddress, String recipientName) {
    var emailProperties = new FeedbackFailedToSendEmailProperties(feedbackContent, recipientName);
    emailService.sendEmail(emailProperties, EmailRecipient.directEmailAddress(emailAddress), "");
  }

}