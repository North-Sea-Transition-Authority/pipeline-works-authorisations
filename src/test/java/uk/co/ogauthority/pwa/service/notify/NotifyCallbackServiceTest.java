package uk.co.ogauthority.pwa.service.notify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.notify.NotifyCallback;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailDeliveryFailedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClientApi;

@RunWith(MockitoJUnitRunner.class)
public class NotifyCallbackServiceTest {

  private static final String CALLBACK_TOKEN = "ec83c275-e593";
  private static final String BOUNCE_BACK_EMAIL_BOX = "bounceback@pwa.co.uk";

  @Mock
  private NotifyService notifyServiceMock;

  @Mock
  private NotificationClientApi notificationClientMock;

  private Notification notification;
  private NotifyCallback notifyCallback;
  private NotifyCallbackService notifyCallbackService;

  @Before
  public void setup() {

    notifyCallbackService = new NotifyCallbackService(notifyServiceMock, notificationClientMock, BOUNCE_BACK_EMAIL_BOX, CALLBACK_TOKEN);

    notifyCallback = new NotifyCallback(
        "be0a4c7d-1657-4b83-8771-2a40e7408d67",
        345235,
        NotifyCallback.NotifyCallbackStatus.DELIVERED,
        "test@test.email.co.uk",
        NotifyCallback.NotifyNotificationType.EMAIL,
        Instant.now(),
        Instant.now(),
        Instant.now()
    );

    String failedNotificationJson = "{\n" +
        "    \"id\":\"be0a4c7d-1657-4b83-8771-2a40e7408d67\",\n" +
        "    \"reference\":\"4234134\",\n" +
        "    \"status\":\"delivered\",\n" +
        "    \"email_address\":\"test@test.email.co.uk\",\n" +
        "    \"created_at\":\"2020-02-21T15:16:35.878994Z\",\n" +
        "    \"completed_at\":\"2020-02-21T15:16:38.089102Z\",\n" +
        "    \"sent_at\":\"2020-02-21T15:16:36.195227Z\",\n" +
        "    \"type\":\"email\",\n" +
        "    \"template\": {\n" +
        "\t    \"id\":\"c9b5dd30-8b96-11ea-bc55-0242ac130003\",\n" +
        "\t    \"version\":\"231\",\n" +
        "\t    \"uri\":\"www.teplate-uri.temp\"\n" +
        "\t  },\n" +
        "\t\"subject\": \"test-subject\",\n" +
        "\t\"body\": \"test-body\",\n" +
        "\t\"estimatedDelivery\": \"2020-02-21T15:20:35.878994Z\",\n" +
        "\t\"createdByName\": \"test-name\"\n" +
        "}";

    notification = new Notification(failedNotificationJson);
  }

  @Test
  public void handleCallback_emailDelivered() throws Exception {
    notifyCallbackService.handleCallback(notifyCallback);
    verifyNoInteractions(notifyServiceMock);
  }

  @Test
  public void handleCallback_failedToDeliverEmailToBounceBackBox() throws Exception {
    notifyCallback.setStatus(NotifyCallback.NotifyCallbackStatus.PERMANENT_FAILURE);
    notifyCallback.setTo(BOUNCE_BACK_EMAIL_BOX);

    notifyCallbackService.handleCallback(notifyCallback);
    verifyNoInteractions(notifyServiceMock);
  }

  @Test
  public void handleCallback_failedToDeliverEmailToWrongAddress() throws Exception {

    notifyCallback.setStatus(NotifyCallback.NotifyCallbackStatus.PERMANENT_FAILURE);
    when(notificationClientMock.getNotificationById(anyString())).thenReturn(notification);

    notifyCallbackService.handleCallback(notifyCallback);

    ArgumentCaptor<EmailProperties> emailCaptor = ArgumentCaptor.forClass(EmailProperties.class);

    verify(notifyServiceMock, times(1)).sendEmail(emailCaptor.capture(), eq(BOUNCE_BACK_EMAIL_BOX));

    EmailDeliveryFailedEmailProps failedEmail = (EmailDeliveryFailedEmailProps)emailCaptor.getValue();

    assertThat(failedEmail.getFailedEmailAddress()).isEqualTo(notification.getEmailAddress().get());
    assertThat(failedEmail.getOriginalSubject()).isEqualTo(notification.getSubject().get());
    assertThat(failedEmail.getOriginalBody()).isEqualTo(notification.getBody());
  }

  @Test
  public void isCallbackTokenMatching_unmatchingToken() {

    String bearerToken = constructBearerToken("invalid-token");

    assertThat(notifyCallbackService.isTokenValid(bearerToken)).isFalse();

  }

  private String constructBearerToken(String s) {
    return NotifyCallbackService.AUTHORIZATION_SCHEME + s;
  }

  @Test
  public void isCallbackTokenMatching_matchingToken() {

    String bearerToken = constructBearerToken(CALLBACK_TOKEN);

    assertThat(notifyCallbackService.isTokenValid(bearerToken)).isTrue();

  }

}
