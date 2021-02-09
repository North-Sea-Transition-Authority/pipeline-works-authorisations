package uk.co.ogauthority.pwa.govukpay.api.cardpayment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import uk.co.ogauthority.pwa.govukpay.api.cardpayment.LanguageEnum;

/**
 * CreatePaymentResult.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatePaymentResult {

  @JsonProperty("amount")
  private Long amount;

  @JsonProperty("state")
  private PaymentState state;

  @JsonProperty("description")
  private String description;

  @JsonProperty("reference")
  private String reference;

  @JsonProperty("language")
  private LanguageEnum language;

  @JsonProperty("payment_id")
  private String paymentId;

  @JsonProperty("payment_provider")
  private String paymentProvider;

  @JsonProperty("return_url")
  private String returnUrl;

  @JsonProperty("created_date")
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private LocalDateTime createdDate;

  @JsonProperty("delayed_capture")
  private Boolean delayedCapture;

  @JsonProperty("moto")
  private Boolean moto;

  @JsonProperty("_links")
  private PaymentLinks links;

  @JsonProperty("provider_id")
  private String providerId;

  @JsonProperty("metadata")
  private Map<String, String> metadata = new HashMap<>();

  @JsonProperty("email")
  private String email;

  @JsonProperty("refund_summary")
  private RefundSummary refundSummary;

  @JsonProperty("settlement_summary")
  private PaymentSettlementSummary settlementSummary;

  @JsonProperty("card_details")
  private CardDetails cardDetails;


  /**
   * The amount in pence.
   *
   * @return amount
   **/
  public Long getAmount() {
    return amount;
  }

  public void setAmount(Long amount) {
    this.amount = amount;
  }


  /**
   * Get state.
   *
   * @return state
   **/
  public PaymentState getState() {
    return state;
  }

  public void setState(PaymentState state) {
    this.state = state;
  }

  /**
   * The human-readable description you gave the payment.
   *
   * @return description
   **/
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The reference number you associated with this payment.
   *
   * @return reference
   **/
  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  /**
   * Which language your users will see on the payment pages when they make a payment.
   *
   * @return language
   **/
  public LanguageEnum getLanguage() {
    return language;
  }

  public void setLanguage(LanguageEnum language) {
    this.language = language;
  }

  /**
   * The unique identifier of the payment.
   *
   * @return paymentId
   **/
  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }


  /**
   * Get paymentProvider.
   *
   * @return paymentProvider
   **/
  public String getPaymentProvider() {
    return paymentProvider;
  }

  public void setPaymentProvider(String paymentProvider) {
    this.paymentProvider = paymentProvider;
  }


  /**
   * An HTTPS URL on your site that your user will be sent back to once they have completed their payment attempt on GOV.UK Pay.
   *
   * @return returnUrl
   **/
  public String getReturnUrl() {
    return returnUrl;
  }

  public void setReturnUrl(String returnUrl) {
    this.returnUrl = returnUrl;
  }

  /**
   * The date gov uk reports you created the payment.
   *
   * @return createdDate
   **/
  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(LocalDateTime createdDate) {
    this.createdDate = createdDate;
  }


  /**
   * Whether to [delay capturing](https://docs.payments.service.gov.uk/optional_features/delayed_capture/) this payment.
   *
   * @return delayedCapture
   **/
  public Boolean isDelayedCapture() {
    return delayedCapture;
  }

  public void setDelayedCapture(Boolean delayedCapture) {
    this.delayedCapture = delayedCapture;
  }


  /**
   * Mail Order / Telephone Order (MOTO) payment flag.
   *
   * @return moto
   **/
  public Boolean isMoto() {
    return moto;
  }

  public void setMoto(Boolean moto) {
    this.moto = moto;
  }


  /**
   * API endpoints related to the payment.
   *
   * @return links
   **/
  public PaymentLinks getLinks() {
    return links;
  }

  public void setLinks(PaymentLinks links) {
    this.links = links;
  }


  /**
   * The reference number the payment gateway associated with the payment.
   *
   * @return providerId
   **/
  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }


  /**
   * [Custom metadata](https://docs.payments.service.gov.uk/optional_features/custom_metadata/) you added to the payment.
   *
   * @return metadata
   **/
  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public CreatePaymentResult email(String email) {
    this.email = email;
    return this;
  }

  /**
   * The email address of your user.
   *
   * @return email
   **/
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public CreatePaymentResult refundSummary(RefundSummary refundSummary) {
    this.refundSummary = refundSummary;
    return this;
  }

  /**
   * Get refundSummary.
   *
   * @return refundSummary
   **/
  public RefundSummary getRefundSummary() {
    return refundSummary;
  }

  public void setRefundSummary(RefundSummary refundSummary) {
    this.refundSummary = refundSummary;
  }

  public CreatePaymentResult settlementSummary(PaymentSettlementSummary settlementSummary) {
    this.settlementSummary = settlementSummary;
    return this;
  }

  /**
   * Get settlementSummary.
   *
   * @return settlementSummary
   **/
  public PaymentSettlementSummary getSettlementSummary() {
    return settlementSummary;
  }

  public void setSettlementSummary(PaymentSettlementSummary settlementSummary) {
    this.settlementSummary = settlementSummary;
  }

  public CreatePaymentResult cardDetails(CardDetails cardDetails) {
    this.cardDetails = cardDetails;
    return this;
  }

  /**
   * Get cardDetails.
   *
   * @return cardDetails
   **/

  public CardDetails getCardDetails() {
    return cardDetails;
  }

  public void setCardDetails(CardDetails cardDetails) {
    this.cardDetails = cardDetails;
  }


  @Override
  public String toString() {
    return "CreatePaymentResult{" +
        "amount=" + amount +
        ", state=" + state +
        ", description='" + description + '\'' +
        ", reference='" + reference + '\'' +
        ", language=" + language +
        ", paymentId='" + paymentId + '\'' +
        ", paymentProvider='" + paymentProvider + '\'' +
        ", returnUrl='" + returnUrl + '\'' +
        ", createdDate='" + createdDate + '\'' +
        ", delayedCapture=" + delayedCapture +
        ", moto=" + moto +
        ", links=" + links +
        ", providerId='" + providerId + '\'' +
        ", metadata=" + metadata +
        ", email='" + email + '\'' +
        ", refundSummary=" + refundSummary +
        ", settlementSummary=" + settlementSummary +
        ", cardDetails=" + cardDetails +
        '}';
  }
}

