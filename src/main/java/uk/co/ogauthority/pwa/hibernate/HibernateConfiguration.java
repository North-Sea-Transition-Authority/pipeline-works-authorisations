package uk.co.ogauthority.pwa.hibernate;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfiguration {

  @Bean
  public HibernatePropertiesCustomizer configureStatementInspector(HibernateQueryCounter hibernateQueryCounter) {
    return properties -> properties.put(AvailableSettings.STATEMENT_INSPECTOR, hibernateQueryCounter);
  }
}
