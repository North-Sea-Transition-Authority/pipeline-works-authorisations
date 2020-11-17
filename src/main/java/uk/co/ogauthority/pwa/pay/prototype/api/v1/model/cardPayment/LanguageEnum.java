package uk.co.ogauthority.pwa.pay.prototype.api.v1.model.cardPayment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Which language your users will see on the payment pages when they make a payment.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum LanguageEnum {
  EN("en"),

  CY("cy");

  private String value;

  LanguageEnum(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static LanguageEnum fromValue(String text) {
    for (LanguageEnum b : LanguageEnum.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }


}
