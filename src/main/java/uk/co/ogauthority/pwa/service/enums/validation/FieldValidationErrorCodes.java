package uk.co.ogauthority.pwa.service.enums.validation;

public enum FieldValidationErrorCodes {

  BEFORE_TODAY(".beforeToday"),
  INVALID(".invalid"),
  MAX_LENGTH_EXCEEDED(".maxLengthExceeded"),
  REQUIRED(".required");

  private String code;

  FieldValidationErrorCodes(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
