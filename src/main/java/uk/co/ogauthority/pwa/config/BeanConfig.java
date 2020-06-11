package uk.co.ogauthority.pwa.config;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Clock;
import javax.validation.Validation;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.auth.FoxLoginCallbackFilter;
import uk.co.ogauthority.pwa.auth.FoxSessionFilter;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class BeanConfig {

  @Bean
  public Clock utcClock() {
    return Clock.systemUTC();
  }

  @Bean
  public Clock tzClock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  public SpringValidatorAdapter groupValidator() {
    return new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());
  }

  @Bean
  public NotificationClient notificationClient(@Value("${notify.apiKey}") String apiKey,
                                               @Value("${pwa.proxy.host:#{null}}") String proxyHost,
                                               @Value("${pwa.proxy.port:#{null}}") String proxyPort) {
    Proxy proxy;
    if (proxyHost == null) {
      proxy = Proxy.NO_PROXY;
    } else {
      proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.valueOf(proxyPort)));
    }
    return new NotificationClient(apiKey, proxy);
  }

  @Bean
  public EmailValidator emailValidator() {
    return EmailValidator.getInstance();
  }

  @Bean
  public FilterRegistrationBean<FoxSessionFilter> foxSessionFilterRegistration(FoxSessionFilter foxSessionFilter) {
    // Important - disable automatic registration fo the FoxSessionFilter. We register it manually within the WebSecurityConfig
    // If auto registration is not disabled, Spring will includes the session filter 'early' in its filter chain as
    // part of Spring Session filters but before Spring Security. This causes the FoxSessionFilter to be included in
    // requests that have disabled Spring Security (e.g. /assets/**) which can cause performance issues.
    FilterRegistrationBean<FoxSessionFilter> registration = new FilterRegistrationBean<>(foxSessionFilter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  public FilterRegistrationBean<FoxLoginCallbackFilter> foxLoginCallbackFilterRegistration(FoxLoginCallbackFilter foxLoginCallbackFilter) {
    //Disable automatic registration of the security filter - this will be manually registered in security config
    FilterRegistrationBean<FoxLoginCallbackFilter> registration = new FilterRegistrationBean<>(foxLoginCallbackFilter);
    registration.setEnabled(false);
    return registration;
  }

}
