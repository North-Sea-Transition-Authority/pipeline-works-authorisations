package uk.co.ogauthority.pwa.integrations.govukpay.api.cardpayment.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.integrations.govukpay.api.cardpayment.LanguageEnum;

/**
 * The Payment Request Payload.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateCardPaymentRequest {

  @JsonProperty("amount")
  private Integer amount;

  @JsonProperty("reference")
  private String reference;

  @JsonProperty("description")
  private String description;

  @JsonProperty("email")
  private String email;

  @JsonProperty("return_url")
  private String returnUrl;

  @JsonProperty("delayed_capture")
  private final Boolean delayedCapture = false;

  @JsonProperty("language")
  private final LanguageEnum language = LanguageEnum.EN;

  @JsonProperty("moto")
  private final Boolean moto = false;

  @JsonProperty("metadata")
  private final Map<String, Object> metadata = new HashMap<>();

  @JsonProperty("prefilled_cardholder_details")
  private PrefilledCardholderDetails prefilledCardholderDetails;

  /*
   * minimal data required for successful request.
   */
  public CreateCardPaymentRequest(Integer amount, String reference, String description, String returnUrl) {
    this.amount = amount;
    this.reference = reference;
    this.description = description;
    this.returnUrl = returnUrl;
  }

  public void addMetadata(Map<String, Object> metadata) {
    this.metadata.putAll(metadata);
  }

  /**
   * amount in pence.
   * minimum: 0
   * maximum: 10000000
   *
   * @return amount
   **/
  public Integer getAmount() {
    return amount;
  }

  /**
   * payment reference.
   *
   * @return reference
   **/
  public String getReference() {
    return reference;
  }

  /**
   * payment description.
   *
   * @return description
   **/
  public String getDescription() {
    return description;
  }

  /**
   * ISO-639-1 Alpha-2 code of a supported language to use on the payment pages.
   *
   * @return language
   **/
  public LanguageEnum getLanguage() {
    return language;
  }

  /**
   * email.
   *
   * @return email
   **/
  public String getEmail() {
    return email;
  }

  /**
   * service return url.
   *
   * @return returnUrl
   **/
  public String getReturnUrl() {
    return returnUrl;
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
   * Additional metadata - up to 10 name/value pairs - on the payment.
   * Each key must be between 1 and 30 characters long. The value, if a string, must be no greater than 50 characters long.
   * Other permissible value types: boolean, number.
   *
   * @return metadata
   **/
  public Map<String, Object> getMetadata() {
    return metadata;
  }

  /**
   * prefilled_cardholder_details.
   *
   * @return prefilledCardholderDetails
   **/
  public PrefilledCardholderDetails getPrefilledCardholderDetails() {
    return prefilledCardholderDetails;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateCardPaymentRequest createCardPaymentRequest = (CreateCardPaymentRequest) o;
    return Objects.equals(this.amount, createCardPaymentRequest.amount)
        && Objects.equals(this.reference, createCardPaymentRequest.reference)
        && Objects.equals(this.description, createCardPaymentRequest.description)
        && Objects.equals(this.language, createCardPaymentRequest.language)
        && Objects.equals(this.email, createCardPaymentRequest.email)
        && Objects.equals(this.returnUrl, createCardPaymentRequest.returnUrl)
        && Objects.equals(this.delayedCapture, createCardPaymentRequest.delayedCapture)
        && Objects.equals(this.moto, createCardPaymentRequest.moto)
        && Objects.equals(this.metadata, createCardPaymentRequest.metadata)
        && Objects.equals(this.prefilledCardholderDetails, createCardPaymentRequest.prefilledCardholderDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, reference, description, email, returnUrl, delayedCapture, moto, metadata,
        prefilledCardholderDetails);
  }


}

