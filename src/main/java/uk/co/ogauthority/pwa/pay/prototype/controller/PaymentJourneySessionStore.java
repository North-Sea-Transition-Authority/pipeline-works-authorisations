package uk.co.ogauthority.pwa.pay.prototype.controller;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;


/**
 * Session variables are not suitable for production.
 * Consider the scenario:
 * 1. User starts payment journey
 * 2. User is now on GovukPay screens,
 * 3. User is somehow logged out of PWA service
 * 4. User completes GovukPayment Journey
 * 5. User is returned to our service at the return_url
 * 6. The return url cannot be associated with an authenticated user session and application, so we dont know if the payment is completed.
 * Result is edge case where user has completed payment, but PWA thinks they have not.
 */
@SessionAttributes("paymentJourneySessionStore")
public class PaymentJourneySessionStore implements Serializable {

  private static final long serialVersionUID = 1L;


  private Set<UUID> historicalUuidSet;

  private Pair<UUID, String> currentPaymentJourney;

  private UUID activeUuid;

  public PaymentJourneySessionStore() {
    this.historicalUuidSet = new LinkedHashSet<>();
    this.init();
  }

  public void startJourney(String payUkId){
    this.currentPaymentJourney = new ImmutablePair<>(this.activeUuid, payUkId);

  }

  public void init() {
    this.activeUuid = UUID.randomUUID();
    this.historicalUuidSet.add(this.activeUuid);
  }

  public UUID getActiveUuid() {
    return activeUuid;
  }

  public void setHistoricalUUIDAsActive(UUID uuid) {
    if (!this.historicalUuidSet.contains(uuid)) {
      throw new PwaEntityNotFoundException("Could not find matching historical uuid :" + uuid);
    }
    this.activeUuid = uuid;
  }

  public List<UUID> getHistoricalUuidSet() {
    return List.of(historicalUuidSet.toArray(UUID[]::new));
  }

  public Pair<UUID, String> getCurrentPaymentJourney() {
    return currentPaymentJourney;
  }
}
