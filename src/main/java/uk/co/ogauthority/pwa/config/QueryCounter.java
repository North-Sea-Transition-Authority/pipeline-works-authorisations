package uk.co.ogauthority.pwa.config;

import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportalapi.client.QueryListener;
import uk.co.ogauthority.pwa.integrations.epa.metrics.EnergyPortalQueryCounter;

@Component
public class QueryCounter implements QueryListener {

  private final EnergyPortalQueryCounter energyPortalQueryCounter;

  QueryCounter(EnergyPortalQueryCounter energyPortalQueryCounter) {
    this.energyPortalQueryCounter = energyPortalQueryCounter;
  }

  @Override
  public void onRequest(GraphQLQueryRequest request) {
    energyPortalQueryCounter.incrementEpa();
  }
}
