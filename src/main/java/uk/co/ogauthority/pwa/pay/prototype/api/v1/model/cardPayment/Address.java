package uk.co.ogauthority.pwa.pay.prototype.api.v1.model.cardPayment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

/**
 * A structure representing the billing address of a card.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

  private String line1 = null;

  private String line2 = null;

  private String postcode = null;

  private String city = null;

  private String country = null;

  /**
   * Get line1.
   *
   * @return line1
   **/
  public String getLine1() {
    return line1;
  }

  /**
   * Get line2.
   *
   * @return line2
   **/
  public String getLine2() {
    return line2;
  }

  /**
   * Get postcode.
   *
   * @return postcode
   **/
  public String getPostcode() {
    return postcode;
  }

  /**
   * Get city.
   *
   * @return city
   **/

  public String getCity() {
    return city;
  }


  /**
   * Get country.
   *
   * @return country
   **/
  public String getCountry() {
    return country;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Address address = (Address) o;
    return Objects.equals(this.line1, address.line1)
        && Objects.equals(this.line2, address.line2)
        && Objects.equals(this.postcode, address.postcode)
        && Objects.equals(this.city, address.city)
        && Objects.equals(this.country, address.country);
  }

  @Override
  public int hashCode() {
    return Objects.hash(line1, line2, postcode, city, country);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Address {\n");

    sb.append("    line1: ").append(toIndentedString(line1)).append("\n");
    sb.append("    line2: ").append(toIndentedString(line2)).append("\n");
    sb.append("    postcode: ").append(toIndentedString(postcode)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
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

