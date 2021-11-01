package uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings;

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
