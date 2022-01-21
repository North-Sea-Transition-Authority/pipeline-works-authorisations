package uk.co.ogauthority.pwa.features.feedback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.email.emailproperties.feedback.FeedbackFailedToSendEmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;

@Service
public class FeedbackEmailService {

  private final NotifyService notifyService;

  @Autowired
  public FeedbackEmailService(NotifyService notifyService) {
    this.notifyService = notifyService;
  }

  void sendFeedbackFailedToSendEmail(String feedbackContent, String emailAddress, String recipientName) {
    var emailProperties = new FeedbackFailedToSendEmailProperties(feedbackContent, recipientName);
    notifyService.sendEmail(emailProperties, emailAddress);
  }

}