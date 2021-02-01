/*
 * GOV.UK Pay API
 *
 * NOTE: This class is based on an auto generated file by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 */

package uk.co.ogauthority.pwa.pay.api.model.cardpayment.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;
import uk.co.ogauthority.pwa.pay.api.model.cardpayment.Address;


/**
 * PrefilledCardholderDetails.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrefilledCardholderDetails {

  private String cardholderName = null;

  private Address billingAddress = null;

  public PrefilledCardholderDetails cardholderName(String cardholderName) {
    this.cardholderName = cardholderName;
    return this;
  }

  /**
   * prefilled cardholder name.
   *
   * @return cardholderName
   **/
  public String getCardholderName() {
    return cardholderName;
  }

  public void setCardholderName(String cardholderName) {
    this.cardholderName = cardholderName;
  }

  /**
   * prefilled billing address.
   *
   * @return billingAddress
   **/
  public Address getBillingAddress() {
    return billingAddress;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PrefilledCardholderDetails prefilledCardholderDetails = (PrefilledCardholderDetails) o;
    return Objects.equals(this.cardholderName, prefilledCardholderDetails.cardholderName)
        && Objects.equals(this.billingAddress, prefilledCardholderDetails.billingAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardholderName, billingAddress);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PrefilledCardholderDetails {\n");

    sb.append("    cardholderName: ").append(toIndentedString(cardholderName)).append("\n");
    sb.append("    billingAddress: ").append(toIndentedString(billingAddress)).append("\n");
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

