package uk.co.ogauthority.pwa.model.enums.consultations;

public enum ConsultationResponseDocumentType {

  DEFAULT("Provide documents to support your response (optional)", ""),
  SECRETARY_OF_STATE_DECISION("Provide a copy of the Secretary of State's decision (optional)",
      "This is required if the Secretary of State has agreed or not agreed to the grant of consent.");

  private final String questionText;
  private final String questionGuidance;

  ConsultationResponseDocumentType(String questionText, String questionGuidance) {

    this.questionText = questionText;
    this.questionGuidance = questionGuidance;
  }

  public String getQuestionText() {
    return questionText;
  }

  public String getQuestionGuidance() {
    return questionGuidance;
  }

}
