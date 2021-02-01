package uk.co.ogauthority.pwa.pay.api.model.cardpayment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A link related to a payment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {

  @JsonProperty("href")
  private String href = null;

  @JsonProperty("method")
  private String method;

  /**
   * Get href.
   *
   * @return href
   **/
  public String getHref() {
    return href;
  }

  /**
   * Get method.
   *
   * @return method
   **/
  public String getMethod() {
    return method;
  }

  @Override
  public String toString() {
    return "Link{" +
        "href='" + href + '\'' +
        ", method='" + method + '\'' +
        '}';
  }
}

