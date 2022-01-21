package uk.co.ogauthority.pwa.features.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.features.email.emailproperties.feedback.FeedbackFailedToSendEmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackEmailServiceTest {

  @Mock
  private NotifyService notifyService;

  private FeedbackEmailService feedbackEmailService;

  @Before
  public void setup() {
    feedbackEmailService = new FeedbackEmailService(notifyService);
  }

  @Test
  public void sendFeedbackFailedToSendEmail() {
    var feedbackContent = "testContent";
    var emailAddress = "test@test.com";
    var recipientName = "testRecipient";

    feedbackEmailService.sendFeedbackFailedToSendEmail(feedbackContent, emailAddress, recipientName);

    ArgumentCaptor<EmailProperties> emailCaptor = ArgumentCaptor.forClass(EmailProperties.class);
    verify(notifyService, times(1)).sendEmail(emailCaptor.capture(), eq(emailAddress));
    FeedbackFailedToSendEmailProperties emailProperties = (FeedbackFailedToSendEmailProperties) emailCaptor.getValue();

    final var expectedEmailProperties = new HashMap<String, String>();
    expectedEmailProperties.put("FEEDBACK_CONTENT", feedbackContent);
    expectedEmailProperties.put("RECIPIENT_FULL_NAME", recipientName);
    expectedEmailProperties.put("TEST_EMAIL", "no");
    assertThat(emailProperties.getEmailPersonalisation()).containsExactlyInAnyOrderEntriesOf(expectedEmailProperties);
  }

}