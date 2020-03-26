package uk.co.ogauthority.pwa.config.fileupload;

public enum DeleteOutcomeType {
  SUCCESS("File has successfully been deleted"),
  INTERNAL_SERVER_ERROR("Unexpected error");

  private final String errorMessage;

  DeleteOutcomeType(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
