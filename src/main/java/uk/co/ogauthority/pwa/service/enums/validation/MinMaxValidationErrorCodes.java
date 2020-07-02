package uk.co.ogauthority.pwa.service.enums.validation;

public enum MinMaxValidationErrorCodes {

  MIN_LARGER_THAN_MAX(".minLargerThanMax"),
  NOT_POSITIVE(".notPositive"),
  NOT_INTEGER(".notInteger"),
  INVALID_DECIMAL_PLACE(".invalidDecimalPlace");

  private String code;

  MinMaxValidationErrorCodes(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
