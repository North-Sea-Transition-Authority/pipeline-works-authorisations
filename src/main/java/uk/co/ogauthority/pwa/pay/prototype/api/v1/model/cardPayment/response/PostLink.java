package uk.co.ogauthority.pwa.pay.prototype.api.v1.model.cardPayment.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * A POST link related to a payment
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostLink {

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("params")
  private Map<String, Object> params = null;

  @JsonProperty("href")
  private String href = null;

  @JsonProperty("method")
  private String method = null;

  public PostLink type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public PostLink params(Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public PostLink putParamsItem(String key, Object paramsItem) {
    if (this.params == null) {
      this.params = new HashMap<String, Object>();
    }
    this.params.put(key, paramsItem);
    return this;
  }

   /**
   * Get params
   * @return params
  **/
  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

   /**
   * Get href
   * @return href
  **/
  public String getHref() {
    return href;
  }

   /**
   * Get method
   * @return method
  **/
  public String getMethod() {
    return method;
  }

  @Override
  public String toString() {
    return "PostLink{" +
        "type='" + type + '\'' +
        ", params=" + params +
        ", href='" + href + '\'' +
        ", method='" + method + '\'' +
        '}';
  }
}

