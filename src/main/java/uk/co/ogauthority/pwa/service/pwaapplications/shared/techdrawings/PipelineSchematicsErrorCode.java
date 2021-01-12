package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;

public enum PipelineSchematicsErrorCode {

  TECHNICAL_DRAWINGS("allPipelinesAdded" + FieldValidationErrorCodes.INVALID.getCode()),
  ADMIRALTY_CHART("NotEmpty.uploadedFileWithDescriptionForms");

  private final String errorCode;

  PipelineSchematicsErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
