package uk.co.ogauthority.pwa.config.epa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.fivium.energyportalapi.client.EnergyPortal;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.ogauthority.pwa.config.QueryCounter;
import uk.co.ogauthority.pwa.integrations.epa.correlationid.CorrelationIdUtil;

@Configuration
public class EnergyPortalApiBeans {

  @Bean
  EnergyPortal energyPortal(
      EnergyPortalApiConfig energyPortalApiConfig,
      QueryCounter queryCounter
  ) {
    return EnergyPortal.customConfiguration(
        energyPortalApiConfig.url(),
        energyPortalApiConfig.preSharedKey(),
        EnergyPortal.DEFAULT_REQUEST_TIMEOUT_SECONDS,
        () -> new LogCorrelationId(CorrelationIdUtil.getCorrelationIdFromMdc()),
        queryCounter
    );
  }

  @Bean
  public FieldApi fieldApi(EnergyPortal energyPortal) {
    return new FieldApi(energyPortal);
  }

  @Bean
  public UserApi userApi(EnergyPortal energyPortal) {
    return new UserApi(energyPortal);
  }

  @Bean
  public OrganisationApi organisationApi(EnergyPortal energyPortal) {
    return new OrganisationApi(energyPortal);
  }
}
