package uk.co.ogauthority.pipelines.config;

import java.time.Clock;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.ogauthority.pipelines.auth.FoxSessionFilter;

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
  public FilterRegistrationBean<FoxSessionFilter> foxSessionFilterRegistration(FoxSessionFilter foxSessionFilter) {
    // Important - disable automatic registration fo the FoxSessionFilter. We register it manually within the WebSecurityConfig
    // If auto registration is not disabled, Spring will includes the session filter 'early' in its filter chain as
    // part of Spring Session filters but before Spring Security. This causes the FoxSessionFilter to be included in
    // requests that have disabled Spring Security (e.g. /assets/**) which can cause performance issues.
    FilterRegistrationBean<FoxSessionFilter> registration = new FilterRegistrationBean<>(foxSessionFilter);
    registration.setEnabled(false);
    return registration;
  }

}
