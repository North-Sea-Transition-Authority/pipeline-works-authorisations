package uk.co.ogauthority.pwa.integrations.govukpay.api.cardpayment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.integrations.govukpay.api.cardpayment.LanguageEnum;

/**
 * GetPaymentResult.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetPaymentResult {

  @JsonProperty("amount")
  private Long amount;

  @JsonProperty("description")
  private String description;

  @JsonProperty("reference")
  private String reference;

  @JsonProperty("language")
  private LanguageEnum language;

  @JsonProperty("metadata")
  private Map<String, String> metadata;

  @JsonProperty("email")
  private String email;

  @JsonProperty("state")
  private PaymentState state;

  @JsonProperty("payment_id")
  private String paymentId;

  @JsonProperty("payment_provider")
  private String paymentProvider;

  @JsonProperty("created_date")
  private String createdDate;

  @JsonProperty("refund_summary")
  private RefundSummary refundSummary;

  @JsonProperty("settlement_summary")
  private PaymentSettlementSummary settlementSummary;

  @JsonProperty("card_details")
  private CardDetails cardDetails;

  @JsonProperty("delayed_capture")
  private Boolean delayedCapture;

  @JsonProperty("moto")
  private Boolean moto;

  @JsonProperty("corporate_card_surcharge")
  private Long corporateCardSurcharge;

  @JsonProperty("total_amount")
  private Long totalAmount;

  @JsonProperty("fee")
  private Long fee;

  @JsonProperty("net_amount")
  private Long netAmount;

  @JsonProperty("provider_id")
  private String providerId;

  @JsonProperty("return_url")
  private String returnUrl;

  @JsonProperty("_links")
  private PaymentLinks links;

  @JsonProperty("card_brand")
  private String cardBrand;

  public GetPaymentResult amount(Long amount) {
    this.amount = amount;
    return this;
  }

  /**
   * Get amount.
   *
   * @return amount
   **/
  public Long getAmount() {
    return amount;
  }

  public void setAmount(Long amount) {
    this.amount = amount;
  }

  public GetPaymentResult description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description.
   *
   * @return description
   **/
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public GetPaymentResult reference(String reference) {
    this.reference = reference;
    return this;
  }

  /**
   * Get reference.
   *
   * @return reference
   **/
  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public GetPaymentResult language(LanguageEnum language) {
    this.language = language;
    return this;
  }

  /**
   * Get language.
   *
   * @return language
   **/
  public LanguageEnum getLanguage() {
    return language;
  }

  public void setLanguage(LanguageEnum language) {
    this.language = language;
  }

  public GetPaymentResult metadata(Map<String, String> metadata) {
    this.metadata = metadata;
    return this;
  }

  public GetPaymentResult putMetadataItem(String key, String metadataItem) {
    if (this.metadata == null) {
      this.metadata = new HashMap<String, String>();
    }
    this.metadata.put(key, metadataItem);
    return this;
  }

  /**
   * Get metadata.
   *
   * @return metadata
   **/
  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public GetPaymentResult email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email.
   *
   * @return email
   **/
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public GetPaymentResult state(PaymentState state) {
    this.state = state;
    return this;
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
   * Get paymentId.
   *
   * @return paymentId
   **/
  public String getPaymentId() {
    return paymentId;
  }

  /**
   * Get paymentProvider.
   * example = "worldpay"
   *
   * @return paymentProvider
   **/
  public String getPaymentProvider() {
    return paymentProvider;
  }

  /**
   * Get createdDate.
   * example = "2016-01-21T17:15:000Z"
   *
   * @return createdDate
   **/
  public String getCreatedDate() {
    return createdDate;
  }

  /**
   * Get refundSummary.
   *
   * @return refundSummary
   **/
  public RefundSummary getRefundSummary() {
    return refundSummary;
  }

  /**
   * Get settlementSummary.
   *
   * @return settlementSummary
   **/
  public PaymentSettlementSummary getSettlementSummary() {
    return settlementSummary;
  }

  /**
   * Get cardDetails.
   *
   * @return cardDetails
   **/
  public CardDetails getCardDetails() {
    return cardDetails;
  }

  /**
   * delayed capture flag.
   *
   * @return delayedCapture
   **/
  public Boolean isDelayedCapture() {
    return delayedCapture;
  }

  /**
   * Mail Order / Telephone Order (MOTO) payment flag.
   *
   * @return moto
   **/
  public Boolean isMoto() {
    return moto;
  }

  /**
   * Get corporateCardSurcharge.
   *
   * @return corporateCardSurcharge
   **/
  public Long getCorporateCardSurcharge() {
    return corporateCardSurcharge;
  }

  /**
   * Get totalAmount.
   *
   * @return totalAmount
   **/
  public Long getTotalAmount() {
    return totalAmount;
  }

  /**
   * processing fee taken by the GOV.UK Pay platform, in pence.
   * Only available depending on payment service provider.
   *
   * @return fee
   **/
  public Long getFee() {
    return fee;
  }

  /**
   * amount including all surcharges and less all fees, in pence.
   * Only available depending on payment service provider.
   *
   * @return netAmount
   **/
  public Long getNetAmount() {
    return netAmount;
  }

  /**
   * Get providerId.
   *
   * @return providerId
   **/
  public String getProviderId() {
    return providerId;
  }

  /**
   * Get returnUrl.
   *
   * @return returnUrl
   **/
  public String getReturnUrl() {
    return returnUrl;
  }

  public GetPaymentResult links(PaymentLinks links) {
    this.links = links;
    return this;
  }

  /**
   * Get links.
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
   * Card Brand.
   * example = "Visa"
   *
   * @return cardBrand
   **/
  public String getCardBrand() {
    return cardBrand;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetPaymentResult getPaymentResult = (GetPaymentResult) o;
    return Objects.equals(this.amount, getPaymentResult.amount)
        && Objects.equals(this.description, getPaymentResult.description)
        && Objects.equals(this.reference, getPaymentResult.reference)
        && Objects.equals(this.language, getPaymentResult.language)
        && Objects.equals(this.metadata, getPaymentResult.metadata)
        && Objects.equals(this.email, getPaymentResult.email)
        && Objects.equals(this.state, getPaymentResult.state)
        && Objects.equals(this.paymentId, getPaymentResult.paymentId)
        && Objects.equals(this.paymentProvider, getPaymentResult.paymentProvider)
        && Objects.equals(this.createdDate, getPaymentResult.createdDate)
        && Objects.equals(this.refundSummary, getPaymentResult.refundSummary)
        && Objects.equals(this.settlementSummary, getPaymentResult.settlementSummary)
        && Objects.equals(this.cardDetails, getPaymentResult.cardDetails)
        && Objects.equals(this.delayedCapture, getPaymentResult.delayedCapture)
        && Objects.equals(this.moto, getPaymentResult.moto)
        && Objects.equals(this.corporateCardSurcharge, getPaymentResult.corporateCardSurcharge)
        && Objects.equals(this.totalAmount, getPaymentResult.totalAmount)
        && Objects.equals(this.fee, getPaymentResult.fee)
        && Objects.equals(this.netAmount, getPaymentResult.netAmount)
        && Objects.equals(this.providerId, getPaymentResult.providerId)
        && Objects.equals(this.returnUrl, getPaymentResult.returnUrl)
        && Objects.equals(this.links, getPaymentResult.links)
        && Objects.equals(this.cardBrand, getPaymentResult.cardBrand);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, description, reference, language, metadata, email, state, paymentId, paymentProvider,
        createdDate, refundSummary, settlementSummary, cardDetails, delayedCapture, moto, corporateCardSurcharge,
        totalAmount, fee, netAmount, providerId, returnUrl, links, cardBrand);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetPaymentResult {\n");

    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    reference: ").append(toIndentedString(reference)).append("\n");
    sb.append("    language: ").append(toIndentedString(language)).append("\n");
    sb.append("    metadata: ").append(toIndentedString(metadata)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    paymentId: ").append(toIndentedString(paymentId)).append("\n");
    sb.append("    paymentProvider: ").append(toIndentedString(paymentProvider)).append("\n");
    sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
    sb.append("    refundSummary: ").append(toIndentedString(refundSummary)).append("\n");
    sb.append("    settlementSummary: ").append(toIndentedString(settlementSummary)).append("\n");
    sb.append("    cardDetails: ").append(toIndentedString(cardDetails)).append("\n");
    sb.append("    delayedCapture: ").append(toIndentedString(delayedCapture)).append("\n");
    sb.append("    moto: ").append(toIndentedString(moto)).append("\n");
    sb.append("    corporateCardSurcharge: ").append(toIndentedString(corporateCardSurcharge)).append("\n");
    sb.append("    totalAmount: ").append(toIndentedString(totalAmount)).append("\n");
    sb.append("    fee: ").append(toIndentedString(fee)).append("\n");
    sb.append("    netAmount: ").append(toIndentedString(netAmount)).append("\n");
    sb.append("    providerId: ").append(toIndentedString(providerId)).append("\n");
    sb.append("    returnUrl: ").append(toIndentedString(returnUrl)).append("\n");
    sb.append("    links: ").append(toIndentedString(links)).append("\n");
    sb.append("    cardBrand: ").append(toIndentedString(cardBrand)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

