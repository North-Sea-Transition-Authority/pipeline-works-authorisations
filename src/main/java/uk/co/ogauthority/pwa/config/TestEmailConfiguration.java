package uk.co.ogauthority.pwa.config;

import java.util.List;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyTemplateService;
import uk.co.ogauthority.pwa.integrations.govuknotify.TestNotifyServiceImpl;
import uk.gov.service.notify.NotificationClient;

/**
 * Configuration class for non-production profiles that should be using test email functionality.
 */
@Configuration
@Profile({"test-email", "development", "integration-test"})
public class TestEmailConfiguration {

  /**
   * Bean to return the test GOV.UK notify implementation which will send
   * emails to the test recipient instead of the actual recipient.
   *
   * @param notifyTemplateService An instance of the template service
   * @param notificationClient A GOV.UK notification client
   * @param emailValidator Email Validator
   * @param testRecipientList List of test recipients to send the emails to
   * @param serviceName the name of the service the email is being sent from
   * @return an instantiated TestNotifyServiceImpl
   */
  @Bean
  @ConditionalOnProperty(name = "email.mode", havingValue = "test")
  public TestNotifyServiceImpl testNotifyService(NotifyTemplateService notifyTemplateService,
                                                 NotificationClient notificationClient,
                                                 EmailValidator emailValidator,
                                                 // NB: the ":" means the default value will be an empty string when not specified
                                                 @Value("#{'${email.testRecipientList:}'.split(';')}") List<String> testRecipientList,
                                                 @Value("${service.full-name}") String serviceName
  ) {
    return new TestNotifyServiceImpl(notifyTemplateService, notificationClient, emailValidator, testRecipientList, serviceName);
  }
}
