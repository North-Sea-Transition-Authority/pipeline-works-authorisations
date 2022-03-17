package uk.co.ogauthority.pwa.config.fileupload;

public enum DeleteOutcomeType {
  SUCCESS("File has successfully been deleted"),
  INTERNAL_SERVER_ERROR("Unexpected error"),
  NOT_FIRST_VERSION("File has not been deleted because other versions might be relying on it");

  private final String errorMessage;

  DeleteOutcomeType(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
