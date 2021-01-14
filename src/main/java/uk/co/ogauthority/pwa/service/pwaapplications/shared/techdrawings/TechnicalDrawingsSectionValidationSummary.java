package uk.co.ogauthority.pwa.service.pwaapplications.shared.techdrawings;

public class TechnicalDrawingsSectionValidationSummary {

  private final boolean isComplete;
  private final String errorMessage;

  private TechnicalDrawingsSectionValidationSummary(boolean isComplete, String errorMessage) {
    this.isComplete = isComplete;
    this.errorMessage = errorMessage;
  }

  public static TechnicalDrawingsSectionValidationSummary createValidSummary() {
    return new TechnicalDrawingsSectionValidationSummary(true, null);
  }

  public static TechnicalDrawingsSectionValidationSummary createInvalidSummary(String errorMessage) {
    return new TechnicalDrawingsSectionValidationSummary(false, errorMessage);
  }


  public boolean isComplete() {
    return isComplete;
  }

  public String getErrorMessage() {
    return errorMessage;
  }
}
