package uk.co.ogauthority.pwa.govukpay;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * returned by govuk pay client on on successful new card payment journey creation.
 */
public final class GovPayNewCardPaymentResult {

  private final GovPayPaymentJourneyState govPayPaymentJourneyState;

  private final String paymentId;

  // GET endpoint on application service that allows receives a user completing external payment journey
  // maybe not successfully
  private final String returnToServiceAfterJourneyCompleteUrl;

  // GET endpoint on external service that allows a user to complete the started payment journey
  private final String startExternalPaymentJourneyUrl;

  private final LocalDateTime createdDate;

  private final Map<String, String> metadata;

  GovPayNewCardPaymentResult(String paymentId,
                             GovPayPaymentJourneyState govPayPaymentJourneyState,
                             String returnToServiceAfterJourneyCompleteUrl,
                             String startExternalPaymentJourneyUrl,
                             LocalDateTime createdDate,
                             Map<String, String> metadata) {
    this.paymentId = paymentId;
    this.govPayPaymentJourneyState = govPayPaymentJourneyState;
    this.returnToServiceAfterJourneyCompleteUrl = returnToServiceAfterJourneyCompleteUrl;
    this.startExternalPaymentJourneyUrl = startExternalPaymentJourneyUrl;
    this.createdDate = createdDate;
    this.metadata =  Collections.unmodifiableMap(metadata);
  }

  public GovPayPaymentJourneyState getPaymentJourneyState() {
    return govPayPaymentJourneyState;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public String getStartExternalPaymentJourneyUrl() {
    return startExternalPaymentJourneyUrl;
  }

  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public String getReturnToServiceAfterJourneyCompleteUrl() {
    return returnToServiceAfterJourneyCompleteUrl;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

}

