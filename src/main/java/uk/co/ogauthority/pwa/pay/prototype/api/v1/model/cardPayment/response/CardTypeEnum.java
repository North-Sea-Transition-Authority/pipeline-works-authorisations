package uk.co.ogauthority.pwa.pay.prototype.api.v1.model.cardPayment.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The card type, &#x60;debit&#x60; or &#x60;credit&#x60; or &#x60;null&#x60; if not able to determine.
 */
public enum CardTypeEnum {
  DEBIT("debit"),

  CREDIT("credit"),

  NULL("null");

  private String value;

  CardTypeEnum(String value) {
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
  public static CardTypeEnum fromValue(String text) {
    for (CardTypeEnum b : CardTypeEnum.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
