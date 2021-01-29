package uk.co.ogauthority.pwa.govukpay.api.cardpayment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * links for payment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentLinks {

  @JsonProperty("self")
  private Link self;

  @JsonProperty("next_url")
  private Link nextUrl;

  @JsonProperty("next_url_post")
  private PostLink nextUrlPost;

  @JsonProperty("events")
  private Link events;

  @JsonProperty("refunds")
  private Link refunds;

  @JsonProperty("cancel")
  private PostLink cancel;

  @JsonProperty("capture")
  private PostLink capture;

  /**
   * self.
   *
   * @return self
   **/
  public Link getSelf() {
    return self;
  }

  /**
   * next_url.
   *
   * @return nextUrl
   **/

  public Link getNextUrl() {
    return nextUrl;
  }

  /**
   * next_url_post.
   *
   * @return nextUrlPost
   **/
  public PostLink getNextUrlPost() {
    return nextUrlPost;
  }

  /**
   * events.
   *
   * @return events
   **/
  public Link getEvents() {
    return events;
  }

  /**
   * refunds.
   *
   * @return refunds
   **/
  public Link getRefunds() {
    return refunds;
  }

  /**
   * cancel.
   *
   * @return cancel
   **/
  public PostLink getCancel() {
    return cancel;
  }

  /**
   * capture.
   *
   * @return capture
   **/
  public PostLink getCapture() {
    return capture;
  }

  @Override
  public String toString() {
    return "PaymentLinks{" +
        "self=" + self +
        ", nextUrl=" + nextUrl +
        ", nextUrlPost=" + nextUrlPost +
        ", events=" + events +
        ", refunds=" + refunds +
        ", cancel=" + cancel +
        ", capture=" + capture +
        '}';
  }
}

