package uk.co.ogauthority.pwa.service.notify;

import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.notify.NotifyCallback;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailDeliveryFailedEmailProps;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

@Service
public class NotifyCallbackService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotifyCallbackService.class);
  public static final String AUTHORIZATION_SCHEME = "Bearer ";

  private final NotifyService notifyService;
  private final NotificationClientApi notificationClient;

  private final String ogaConsentsMailboxEmail;
  private final String callbackToken;

  private final Set<NotifyCallback.NotifyCallbackStatus> failureStatuses =
      Set.of(NotifyCallback.NotifyCallbackStatus.PERMANENT_FAILURE, NotifyCallback.NotifyCallbackStatus.TEMPORARY_FAILURE);

  @Autowired
  public NotifyCallbackService(NotifyService notifyService,
                               NotificationClientApi notificationClient,
                               @Value("${oga.consents.email}") String ogaConsentsMailboxEmail,
                               @Value("${email.notifyCallbackToken}") String callbackToken) {
    this.notifyService = notifyService;
    this.notificationClient = notificationClient;
    this.ogaConsentsMailboxEmail = ogaConsentsMailboxEmail;
    this.callbackToken = callbackToken;
  }

  public void handleCallback(NotifyCallback notifyCallback) {

    if (failureStatuses.contains(notifyCallback.getStatus())) {

      // TODO PWA-591 log metrics for callback failures
      LOGGER.info("The Notify provider could not deliver the message to the email address {}.", notifyCallback.getTo());

      // if an email failed and the failed email wasn't going to the OGA mailbox, notify OGA
      if (!notifyCallback.getTo().equals(ogaConsentsMailboxEmail)) {

        try {

          Notification failedEmail = notificationClient.getNotificationById(notifyCallback.getId());

          var failedEmailProperties = new EmailDeliveryFailedEmailProps(
              failedEmail.getEmailAddress().orElseThrow(() -> new NotificationClientException("Failed email address cannot be retrieved")),
              failedEmail.getSubject().orElse(""),
              failedEmail.getBody()
          );

          notifyService.sendEmail(failedEmailProperties, ogaConsentsMailboxEmail);

          throw new NotificationClientException("");

        } catch (NotificationClientException e) {
          LOGGER.error("Couldn't retrieve email notification from GOV.UK: ", e);
        }

      } else {
        // otherwise we failed to email the OGA mailbox
        // TODO PWA-591 log metric
        LOGGER.error("Email send to the OGA consents mailbox failed {}", notifyCallback.toString());
      }
    }
  }

  public boolean isTokenValid(String bearerToken) {
    return StringUtils.stripStart(bearerToken, AUTHORIZATION_SCHEME).equals(callbackToken);
  }
}
