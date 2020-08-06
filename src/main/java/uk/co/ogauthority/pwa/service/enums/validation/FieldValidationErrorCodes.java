package uk.co.ogauthority.pwa.service.enums.validation;

public enum FieldValidationErrorCodes {

  BEFORE_TODAY(".beforeToday"),
  BEFORE_SOME_DATE(".beforeDate"),
  AFTER_TODAY(".afterToday"),
  AFTER_SOME_DATE(".afterDate"),
  INVALID(".invalid"),
  MAX_LENGTH_EXCEEDED(".maxLengthExceeded"),
  REQUIRED(".required"),
  OUT_OF_TARGET_RANGE(".outOfTargetRange"),
  NOT_UNIQUE(".notUnique"),
  EXCEEDED_MAXIMUM_FILE_UPLOAD_COUNT(".exceedsMaximumFileUploadCount");

  private final String code;

  FieldValidationErrorCodes(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public String errorCode(String fieldName) {
    return fieldName + this.getCode();
  }
}
