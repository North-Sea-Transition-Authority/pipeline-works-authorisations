package uk.co.ogauthority.pwa.integrations.govukpay;

import java.util.Map;

/**
 * returns view of single govuk payment journey.
 */
public final class GovPayPaymentJourneyData {

  private final String paymentId;

  private final GovPayPaymentJourneyState govPayPaymentJourneyState;

  private final Long amount;

  private final String description;

  private final String reference;

  private final Map<String, String> metadata;

  private final String email; // where confirmation email was sent if payment completed in service

  private final String paymentProvider;

  private final String createdDate;

  private final String refundStatus;

  private final String settlementStatus;

  private final Long corporateCardSurcharge;

  private final Long totalAmount;

  private final Long fee;

  private final Long netAmount;

  private final String providerId;

  private final String returnUrl;

  public GovPayPaymentJourneyData(String paymentId,
                                  GovPayPaymentJourneyState govPayPaymentJourneyState,
                                  Long amount,
                                  String description,
                                  String reference,
                                  Map<String, String> metadata,
                                  String email,
                                  String paymentProvider,
                                  String createdDate,
                                  String refundStatus,
                                  String settlementStatus,
                                  Long corporateCardSurcharge,
                                  Long totalAmount,
                                  Long fee,
                                  Long netAmount,
                                  String providerId,
                                  String returnUrl) {
    this.paymentId = paymentId;
    this.govPayPaymentJourneyState = govPayPaymentJourneyState;
    this.amount = amount;
    this.description = description;
    this.reference = reference;
    this.metadata = metadata;
    this.email = email;
    this.paymentProvider = paymentProvider;
    this.createdDate = createdDate;
    this.refundStatus = refundStatus;
    this.settlementStatus = settlementStatus;
    this.corporateCardSurcharge = corporateCardSurcharge;
    this.totalAmount = totalAmount;
    this.fee = fee;
    this.netAmount = netAmount;
    this.providerId = providerId;
    this.returnUrl = returnUrl;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public GovPayPaymentJourneyState getPaymentJourneyState() {
    return govPayPaymentJourneyState;
  }

  public Long getAmount() {
    return amount;
  }

  public String getDescription() {
    return description;
  }

  public String getReference() {
    return reference;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public String getEmail() {
    return email;
  }

  public String getPaymentProvider() {
    return paymentProvider;
  }

  public String getCreatedDate() {
    return createdDate;
  }

  public String getRefundStatus() {
    return refundStatus;
  }

  public String getSettlementStatus() {
    return settlementStatus;
  }

  public Long getCorporateCardSurcharge() {
    return corporateCardSurcharge;
  }

  public Long getTotalAmount() {
    return totalAmount;
  }

  public Long getFee() {
    return fee;
  }

  public Long getNetAmount() {
    return netAmount;
  }

  public String getProviderId() {
    return providerId;
  }

  public String getReturnUrl() {
    return returnUrl;
  }
}
