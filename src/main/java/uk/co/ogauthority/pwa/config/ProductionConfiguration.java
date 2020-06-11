package uk.co.ogauthority.pwa.config;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.co.ogauthority.pwa.service.notify.NotifyTemplateService;
import uk.co.ogauthority.pwa.service.notify.ProductionNotifyServiceImpl;
import uk.gov.service.notify.NotificationClient;

/**
 * Configuration class for the "production" profile.
 */
@Configuration
@Profile("production")
public class ProductionConfiguration {

  /**
   * Bean to return the production GOV.UK notify implementation if the matches condition
   * inside ProdNotifyCondition evaluates to true
   * @param notifyTemplateService An instance of the template service
   * @param notificationClient A GOV.UK notification client
   * @param emailValidator Email Validator
   * @param serviceName the name of the service the email is being sent from
   * @return an instantiated ProductionNotifyServiceImpl
   */
  @Bean
  @ConditionalOnProperty(name = "email.mode", havingValue = "production")
  public ProductionNotifyServiceImpl productionNotifyService(NotifyTemplateService notifyTemplateService,
                                                             NotificationClient notificationClient,
                                                             EmailValidator emailValidator,
                                                             @Value("${service.name}") String serviceName) {
    return new ProductionNotifyServiceImpl(notifyTemplateService, notificationClient, emailValidator, serviceName);
  }
}
