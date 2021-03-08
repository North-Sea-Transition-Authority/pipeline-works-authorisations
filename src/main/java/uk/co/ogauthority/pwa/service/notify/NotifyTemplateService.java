package uk.co.ogauthority.pwa.service.notify;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplateType;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.Template;

/**
 * Service to handle interactions with email templates.
 */
@Service
public class NotifyTemplateService {

  private List<Template> emailTemplateList = Collections.emptyList();
  private final NotificationClient notificationClient;
  private static final Logger LOGGER = LoggerFactory.getLogger(NotifyTemplateService.class);

  public NotifyTemplateService(NotificationClient notificationClient) {
    this.notificationClient = notificationClient;
  }

  /**
   * Get a template id by name.
   * @param templateName The name of the template to get the ID for
   * @return The ID of the template with the name templateName
   */
  protected Optional<String> getTemplateIdFromName(String templateName) {

    // Lazy load the email templates if the List is empty
    if (emailTemplateList.isEmpty()) {
      try {
        emailTemplateList = notificationClient.getAllTemplates(NotifyTemplateType.EMAIL_TEMPLATE_TYPE.getTypeName()).getTemplates();
      } catch (NotificationClientException e) {
        LOGGER.error("Error constructing NotificationClient: {}", ExceptionUtils.getStackTrace(e));
        emailTemplateList = Collections.emptyList();
      }
    }

    return emailTemplateList.stream()
      .filter(template -> template.getName().equals(templateName))
      .map(template -> template.getId().toString())
      .findFirst();

  }
}