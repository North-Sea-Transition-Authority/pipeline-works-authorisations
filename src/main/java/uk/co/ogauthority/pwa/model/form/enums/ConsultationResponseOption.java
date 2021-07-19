package uk.co.ogauthority.pwa.model.form.enums;

import java.util.Arrays;
import java.util.List;

public enum ConsultationResponseOption {

  CONFIRMED(
      "Confirm contentedness",
      "Provide consent conditions if they apply",
      "Consent conditions",
      "Consent conditions"
  ),
  REJECTED(
      "Rejected",
      "Why are you rejecting this application?",
      "Reason for rejecting the application",
      "Rejection reason"
  ),

  PROVIDE_ADVICE(
      "Provide advice",
      "Advice text",
      "Advice text",
      "Advice text"
  ),
  NO_ADVICE(
      "No advice",
      "Provide comments if they apply",
      "Comments",
      "Comments"
  );

  private final String labelText;
  private final String textAreaLabelText;
  private final String textAreaLengthValidationMessagePrefix;
  private final String textAreaViewLabelText;

  ConsultationResponseOption(String labelText,
                             String textAreaLabelText,
                             String textAreaLengthValidationMessagePrefix,
                             String textAreaViewLabelText) {
    this.labelText = labelText;
    this.textAreaLabelText = textAreaLabelText;
    this.textAreaLengthValidationMessagePrefix = textAreaLengthValidationMessagePrefix;
    this.textAreaViewLabelText = textAreaViewLabelText;
  }

  public String getLabelText() {
    return labelText;
  }

  public String getTextAreaLabelText() {
    return textAreaLabelText;
  }

  public String getTextAreaLengthValidationMessagePrefix() {
    return textAreaLengthValidationMessagePrefix;
  }

  public String getTextAreaViewLabelText() {
    return textAreaViewLabelText;
  }

  public static List<ConsultationResponseOption> asList() {
    return Arrays.asList(ConsultationResponseOption.values());
  }

}