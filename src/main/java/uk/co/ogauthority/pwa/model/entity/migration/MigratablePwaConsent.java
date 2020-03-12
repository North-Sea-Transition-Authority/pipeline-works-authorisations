package uk.co.ogauthority.pwa.model.entity.migration;

import java.time.Instant;

public interface MigratablePwaConsent {

  int getPadId();

  Integer getVariationNumber();

  String getReference();

  Instant getConsentedInstant();
}
