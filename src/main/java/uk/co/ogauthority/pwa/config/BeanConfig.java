package uk.co.ogauthority.pwa.config;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Validation;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Clock;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.gov.service.notify.NotificationClient;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class BeanConfig {

  @Bean
  public Clock utcClock() {
    return Clock.systemUTC();
  }

  @Bean
  @Primary
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
    Proxy proxy = createProxy(proxyHost, proxyPort);

    return new NotificationClient(apiKey, proxy);
  }

  private Proxy createProxy(String proxyHost, String proxyPort) {
    Proxy proxy;
    if (proxyHost == null) {
      proxy = Proxy.NO_PROXY;
    } else {
      proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
    }

    return proxy;
  }

  @Bean
  @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE) // We need a new instance for each invocation - based on etl config
  public ClientHttpRequestFactory requestFactory(@Value("${pwa.proxy.host:#{null}}") String proxyHost,
                                                 @Value("${pwa.proxy.port:#{null}}") String proxyPort) {
    var httpRequestFactory = new SimpleClientHttpRequestFactory();
    var proxy = createProxy(proxyHost, proxyPort);
    httpRequestFactory.setProxy(proxy);
    return httpRequestFactory;
  }

  @Bean
  public EmailValidator emailValidator() {
    return EmailValidator.getInstance();
  }

  @Bean("messageSource")
  public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("messages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

  @Bean
  public MetricsProvider metricsProvider(MeterRegistry registry) {
    return new MetricsProvider(registry);
  }

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .usingDbTime()
            .build()
    );
  }
}
