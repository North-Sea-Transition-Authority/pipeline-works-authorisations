package uk.co.ogauthority.pwa.pay.prototype.api.v1.model.cardPayment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import uk.co.ogauthority.pwa.pay.prototype.api.v1.model.cardPayment.Address;

/**
 * A structure representing the payment card.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDetails {

  @JsonProperty("last_digits_card_number")
  private String lastDigitsCardNumber = null;

  @JsonProperty("first_digits_card_number")
  private String firstDigitsCardNumber = null;

  @JsonProperty("cardholder_name")
  private String cardholderName = null;

  @JsonProperty("expiry_date")
  private String expiryDate = null;

  @JsonProperty("billing_address")
  private Address billingAddress = null;

  @JsonProperty("card_brand")
  private String cardBrand = null;

  @JsonProperty("card_type")
  private CardTypeEnum cardType = CardTypeEnum.NULL;

  /**
   * Get lastDigitsCardNumber.
   *
   * @return lastDigitsCardNumber
   **/
  public String getLastDigitsCardNumber() {
    return lastDigitsCardNumber;
  }

  /**
   * Get firstDigitsCardNumber.
   *
   * @return firstDigitsCardNumber
   **/
  public String getFirstDigitsCardNumber() {
    return firstDigitsCardNumber;
  }

  /**
   * Get cardholderName.
   *
   * @return cardholderName
   **/
  public String getCardholderName() {
    return cardholderName;
  }

  /**
   * The expiry date of the card in MM/yy format.
   *
   * @return expiryDate
   **/
  public String getExpiryDate() {
    return expiryDate;
  }

  /**
   * Get billingAddress.
   *
   * @return billingAddress
   **/
  public Address getBillingAddress() {
    return billingAddress;
  }

  /**
   * Get cardBrand.
   *
   * @return cardBrand
   **/
  public String getCardBrand() {
    return cardBrand;
  }

  /**
   * The card type, &#x60;debit&#x60; or &#x60;credit&#x60; or &#x60;null&#x60; if not able to determine.
   *
   * @return cardType
   **/
  public CardTypeEnum getCardType() {
    return cardType;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CardDetails cardDetails = (CardDetails) o;
    return Objects.equals(this.lastDigitsCardNumber, cardDetails.lastDigitsCardNumber)
        && Objects.equals(this.firstDigitsCardNumber, cardDetails.firstDigitsCardNumber)
        && Objects.equals(this.cardholderName, cardDetails.cardholderName)
        && Objects.equals(this.expiryDate, cardDetails.expiryDate)
        && Objects.equals(this.billingAddress, cardDetails.billingAddress)
        && Objects.equals(this.cardBrand, cardDetails.cardBrand)
        && Objects.equals(this.cardType, cardDetails.cardType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lastDigitsCardNumber, firstDigitsCardNumber, cardholderName, expiryDate, billingAddress,
        cardBrand, cardType);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardDetails {\n");

    sb.append("    lastDigitsCardNumber: ").append(toIndentedString(lastDigitsCardNumber)).append("\n");
    sb.append("    firstDigitsCardNumber: ").append(toIndentedString(firstDigitsCardNumber)).append("\n");
    sb.append("    cardholderName: ").append(toIndentedString(cardholderName)).append("\n");
    sb.append("    expiryDate: ").append(toIndentedString(expiryDate)).append("\n");
    sb.append("    billingAddress: ").append(toIndentedString(billingAddress)).append("\n");
    sb.append("    cardBrand: ").append(toIndentedString(cardBrand)).append("\n");
    sb.append("    cardType: ").append(toIndentedString(cardType)).append("\n");
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

