package uk.co.ogauthority.pwa.features.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.features.email.emailproperties.feedback.FeedbackFailedToSendEmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;

@ExtendWith(MockitoExtension.class)
class FeedbackEmailServiceTest {

  @Mock
  private EmailService emailService;

  private FeedbackEmailService feedbackEmailService;

  @BeforeEach
  void setup() {
    feedbackEmailService = new FeedbackEmailService(emailService);
  }

  @Test
  void sendFeedbackFailedToSendEmail() {
    var feedbackContent = "testContent";
    var emailAddress = "test@test.com";
    var recipientName = "testRecipient";

    feedbackEmailService.sendFeedbackFailedToSendEmail(feedbackContent, emailAddress, recipientName);

    ArgumentCaptor<EmailProperties> emailCaptor = ArgumentCaptor.forClass(EmailProperties.class);
    verify(emailService, times(1))
        .sendEmail(emailCaptor.capture(), refEq(EmailRecipient.directEmailAddress(emailAddress)), eq(""));
    FeedbackFailedToSendEmailProperties emailProperties = (FeedbackFailedToSendEmailProperties) emailCaptor.getValue();

    final var expectedEmailProperties = new HashMap<String, String>();
    expectedEmailProperties.put("FEEDBACK_CONTENT", feedbackContent);
    expectedEmailProperties.put("RECIPIENT_FULL_NAME", recipientName);
    expectedEmailProperties.put("TEST_EMAIL", "no");
    assertThat(emailProperties.getEmailPersonalisation()).containsExactlyInAnyOrderEntriesOf(expectedEmailProperties);
  }

}