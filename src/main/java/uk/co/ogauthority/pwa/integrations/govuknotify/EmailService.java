package uk.co.ogauthority.pwa.integrations.govuknotify;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MailMergeField;
import uk.co.fivium.digitalnotificationlibrary.core.notification.MergedTemplate;
import uk.co.fivium.digitalnotificationlibrary.core.notification.NotificationLibraryClient;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailNotification;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.config.ServiceProperties;
import uk.co.ogauthority.pwa.integrations.epa.correlationid.CorrelationIdUtil;

@Service
public class EmailService {

  private static final String SERVICE_NAME_KEY = "SERVICE_NAME";
  private static final String TEST_EMAIL_KEY = "TEST_EMAIL";

  private final NotificationLibraryClient notificationLibraryClient;
  private final String serviceName;

  public EmailService(NotificationLibraryClient notificationLibraryClient, ServiceProperties serviceProperties) {
    this.notificationLibraryClient = notificationLibraryClient;
    this.serviceName = serviceProperties.getServiceName();
  }

  public EmailNotification sendEmail(
      EmailProperties emailProperties,
      EmailRecipient emailRecipient,
      String domainId
  ) {
    return notificationLibraryClient.sendEmail(
        getTemplate(emailProperties),
        emailRecipient,
        DomainReference.from(domainId, emailProperties.getDomainTypeName()),
        CorrelationIdUtil.getCorrelationIdFromMdc()
    );
  }

  private MergedTemplate getTemplate(EmailProperties emailProperties) {
    return notificationLibraryClient.getTemplate(emailProperties.getTemplateId())
        .withMailMergeField(SERVICE_NAME_KEY, serviceName)
        .withMailMergeFields(mapToMergeFieldSet(emailProperties.getEmailPersonalisation()))
        .merge();
  }

  private Set<MailMergeField> mapToMergeFieldSet(Map<String, String> fields) {
    var fieldSet = fields.entrySet().stream()
        .map(e -> new MailMergeField(e.getKey(), e.getValue()))
        .collect(Collectors.toSet());

    if (notificationLibraryClient.isRunningTestMode()) {
      fieldSet.add(new MailMergeField(TEST_EMAIL_KEY, "yes"));
    }

    return fieldSet;
  }

}
