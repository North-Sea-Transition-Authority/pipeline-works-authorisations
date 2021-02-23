package uk.co.ogauthority.pwa.service.notify;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

/**
 * Implementation for the Test GOV.UK Notify service
 * The test implementation will not send to the actual recipient and instead send to ${email.testRecipientList}
 * if set. All emails will include "TEST EMAIL" as the first part of the subject.
 */
public class TestNotifyServiceImpl implements NotifyService {

  private final NotificationClient notificationClient;
  private final NotifyTemplateService notifyTemplateService;
  private final List<String> testRecipientList;
  private final EmailValidator emailValidator;
  private static final Logger LOGGER = LoggerFactory.getLogger(TestNotifyServiceImpl.class);
  private final String serviceName;

  public TestNotifyServiceImpl(NotifyTemplateService notifyTemplateService,
                               NotificationClient notificationClient,
                               EmailValidator emailValidator,
                               List<String> testRecipientList,
                               String serviceName
                               ) {
    this.notificationClient = notificationClient;
    this.notifyTemplateService = notifyTemplateService;
    this.emailValidator = emailValidator;
    this.testRecipientList = testRecipientList;
    this.serviceName = serviceName;
  }

  @Override
  public void sendEmail(EmailProperties emailProperties, String toEmailAddress) {
    sendEmail(emailProperties, toEmailAddress, null, null);
  }

  @Override
  public void sendEmail(EmailProperties emailProperties,
                        String toEmailAddress,
                        String reference,
                        String emailReplyToId) {

    Optional<String> templateId = notifyTemplateService.getTemplateIdFromName(emailProperties.getTemplateName());

    if (templateId.isPresent()) {

      // Set the TEST_EMAIL personalisation when in the development service
      Map<String, String> personalisation = emailProperties.getEmailPersonalisation();
      personalisation.put("TEST_EMAIL", "yes");
      personalisation.put("SERVICE_NAME", serviceName);

      // If we have test recipients send the email to each
      testRecipientList.stream()
          .filter(testRecipient -> testRecipient.length() > 0)
          .forEach(testRecipient -> {
            if (emailValidator.isValid(testRecipient)) {
              try {
                notificationClient.sendEmail(templateId.get(), testRecipient, personalisation, reference, emailReplyToId);
              } catch (NotificationClientException e) {
                LOGGER.error("Error occurred in NotificationClient: {}", ExceptionUtils.getStackTrace(e));
              }
            } else {
              // TODO PWA-591 metric logging for email failures
              LOGGER.error("Email validation prevented email being sent to: {}", testRecipient);
            }
          });

    } else {
      LOGGER.error("Could not find template ID for template with name {}", emailProperties.getTemplateName());
    }

  }
}
