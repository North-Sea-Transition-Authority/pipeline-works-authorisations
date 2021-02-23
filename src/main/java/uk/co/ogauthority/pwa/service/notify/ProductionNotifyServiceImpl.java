package uk.co.ogauthority.pwa.service.notify;

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
 * Implementation for the Production GOV.UK Notify service
 * The production implementation will send to the actual recipient
 */
public class ProductionNotifyServiceImpl implements NotifyService {

  private final NotificationClient notificationClient;
  private final NotifyTemplateService notifyTemplateService;
  private final EmailValidator emailValidator;
  private static final Logger LOGGER = LoggerFactory.getLogger(ProductionNotifyServiceImpl.class);
  private final String serviceName;

  public ProductionNotifyServiceImpl(NotifyTemplateService notifyTemplateService,
                                     NotificationClient notificationClient,
                                     EmailValidator emailValidator,
                                     String serviceName) {
    this.notificationClient = notificationClient;
    this.notifyTemplateService = notifyTemplateService;
    this.emailValidator = emailValidator;
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

    try {

      Optional<String> templateId = notifyTemplateService.getTemplateIdFromName(emailProperties.getTemplateName());

      if (templateId.isPresent()) {

        Map<String, String> personalisation = emailProperties.getEmailPersonalisation();
        personalisation.put("SERVICE_NAME", serviceName);

        if (emailValidator.isValid(toEmailAddress)) {
          notificationClient.sendEmail(templateId.get(), toEmailAddress, personalisation, reference, emailReplyToId);
        } else {
          // TODO PWA-591 metric logging for email failures
          LOGGER.error("Email validation prevented email being sent to: {}", toEmailAddress);
        }

      } else {
        LOGGER.error("Could not find template ID for template with name {}", emailProperties.getTemplateName());
      }
    } catch (NotificationClientException e) {
      LOGGER.error("Error occurred in NotificationClient: {}", ExceptionUtils.getStackTrace(e));
    }

  }
}
