package uk.co.ogauthority.pwa.integrations.energyportal.accounts;

import org.springframework.stereotype.Component;
import uk.co.fivium.energyportal.starter.LogCorrelationIdSupplier;
import uk.co.ogauthority.pwa.integrations.epa.correlationid.CorrelationIdUtil;

@Component
class EnergyPortalAccountsLogCorrelationIdSupplier implements LogCorrelationIdSupplier {

  @Override
  public String get() {
    return CorrelationIdUtil.getCorrelationIdFromMdc();
  }
}
