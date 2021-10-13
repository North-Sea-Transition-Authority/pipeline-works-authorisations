package uk.co.ogauthority.pwa.model.enums.consultations;

public enum ConsultationResponseDocumentType {

  DEFAULT("Supporting documents", "Provide documents to support your response (optional)", ""),
  SECRETARY_OF_STATE_DECISION("Secretary of State’s decision", "Provide a copy of the Secretary of State's decision",
      "This is required if the Secretary of State has agreed or does not agree to the grant of consent. " +
          "If you do not agree in order to request for the application to be updated only no file upload is required.");

  private final String displayName;
  private final String questionText;
  private final String questionGuidance;

  ConsultationResponseDocumentType(String displayName, String questionText, String questionGuidance) {
    this.displayName = displayName;
    this.questionText = questionText;
    this.questionGuidance = questionGuidance;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getQuestionText() {
    return questionText;
  }

  public String getQuestionGuidance() {
    return questionGuidance;
  }

}
