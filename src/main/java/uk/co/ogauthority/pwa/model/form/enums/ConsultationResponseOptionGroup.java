package uk.co.ogauthority.pwa.model.form.enums;

import java.util.List;
import java.util.Optional;

public enum ConsultationResponseOptionGroup {

  CONTENT(List.of(
      ConsultationResponseOption.CONFIRMED,
      ConsultationResponseOption.REJECTED),
      "What is your response on this application?",
      "Response",
      10),

  ADVICE(List.of(
      ConsultationResponseOption.PROVIDE_ADVICE,
      ConsultationResponseOption.NO_ADVICE),
      "What is your response on this application?",
      "Response",
      20),

  EIA_REGS(List.of(
      ConsultationResponseOption.EIA_AGREE,
      ConsultationResponseOption.EIA_DISAGREE,
      ConsultationResponseOption.EIA_NOT_RELEVANT),
      "What is your response to the NSTA’s grant of consent under the EIA regulations?",
      "EIA response",
      30),

  HABITATS_REGS(List.of(
      ConsultationResponseOption.HABITATS_AGREE,
      ConsultationResponseOption.HABITATS_DISAGREE,
      ConsultationResponseOption.HABITATS_NOT_RELEVANT),
      "What is your response to the NSTA’s grant of consent under the Habitats regulations?",
      "Habitats response",
      40);

  private final List<ConsultationResponseOption> options;
  private final String questionText;
  private final String responseLabel;
  private final int displayOrder;

  ConsultationResponseOptionGroup(List<ConsultationResponseOption> options,
                                  String questionText,
                                  String responseLabel,
                                  int displayOrder) {
    this.options = options;
    this.questionText = questionText;
    this.responseLabel = responseLabel;
    this.displayOrder = displayOrder;
  }

  public List<ConsultationResponseOption> getOptions() {
    return options;
  }

  public String getQuestionText() {
    return questionText;
  }

  public String getResponseLabel() {
    return responseLabel;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public Optional<ConsultationResponseOption> getResponseOptionNumber(int optionNumber) {
    try {
      return Optional.of(options.get(optionNumber - 1));
    } catch (IndexOutOfBoundsException e) {
      return Optional.empty();
    }
  }

}
