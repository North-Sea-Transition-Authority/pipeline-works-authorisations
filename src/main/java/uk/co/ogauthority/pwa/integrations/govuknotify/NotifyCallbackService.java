package uk.co.ogauthority.pwa.integrations.govuknotify;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClientApi;
import uk.gov.service.notify.NotificationClientException;

@Service
public class NotifyCallbackService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotifyCallbackService.class);
  private static final String EMAIL_LOG_PREFIX = "PWA_EMAIL:";
  public static final String AUTHORIZATION_SCHEME = "Bearer ";

  private final NotificationClientApi notificationClient;
  private final String ogaConsentsMailboxEmail;
  private final String callbackToken;
  private final EmailService emailService;

  @Autowired
  public NotifyCallbackService(NotificationClientApi notificationClient,
                               EmailService emailService,
                               @Value("${oga.consents.email}") String ogaConsentsMailboxEmail,
                               @Value("${email.notifyCallbackToken}") String callbackToken) {
    this.notificationClient = notificationClient;
    this.emailService = emailService;
    this.ogaConsentsMailboxEmail = ogaConsentsMailboxEmail;
    this.callbackToken = callbackToken;
  }

  public void handleCallback(NotifyCallback notifyCallback) {

    if (notifyCallback.getStatus().equals(NotifyCallback.NotifyCallbackStatus.PERMANENT_FAILURE)) {

      LOGGER.info("{} The Notify provider could not deliver the message to the email address {}.",
          EMAIL_LOG_PREFIX, notifyCallback.getTo());

      // if an email failed and the failed email wasn't going to the OGA mailbox, notify OGA
      if (!notifyCallback.getTo().equals(ogaConsentsMailboxEmail)) {

        try {

          Notification failedEmail = notificationClient.getNotificationById(notifyCallback.getId());

          var failedEmailProperties = new EmailDeliveryFailedEmailProps(
              failedEmail.getEmailAddress().orElseThrow(() -> new NotificationClientException("Failed email address cannot be retrieved")),
              failedEmail.getSubject().orElse(""),
              failedEmail.getBody()
          );
          emailService.sendEmail(failedEmailProperties, EmailRecipient.directEmailAddress(ogaConsentsMailboxEmail), "");

        } catch (NotificationClientException e) {
          LOGGER.error("{} Couldn't retrieve email notification from GOV.UK: ", EMAIL_LOG_PREFIX, e);
        }

      } else {
        // otherwise we failed to email the OGA mailbox
        LOGGER.error("{} Email send to the NSTA consents mailbox failed {}", EMAIL_LOG_PREFIX, notifyCallback);
      }
    }
  }

  public boolean isTokenValid(String bearerToken) {
    return StringUtils.removeStart(bearerToken, AUTHORIZATION_SCHEME).equals(callbackToken);
  }
}
