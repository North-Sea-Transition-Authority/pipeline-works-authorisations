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
  ),

  EIA_AGREE(
      "Agree",
      "Provide consent conditions if they apply",
      "Consent conditions",
      "Consent conditions under EIA regulations",
      "Pursuant to the Offshore Oil and Gas Exploration, Production, Unloading and Storage " +
          "(Environmental Impact Assessment) Regulations 2020 , the Offshore Petroleum Regulator for " +
          "Environment and Decommissioning, acting on behalf of the Secretary of State for Business, " +
          "Energy and Industrial Strategy, agrees to the OGA’s grant of consent for the activities " +
          "described in application reference %s and a copy of the " +
          "Secretary of State’s decision is attached."
  ),
  EIA_DISAGREE(
      "Do not agree",
      "Why do you not agree to this application?",
      "Reasons for disagreement",
      "Reasons for disagreement under EIA regulations",
      "Pursuant to the Offshore Oil and Gas Exploration, Production, Unloading and Storage (Environmental " +
          "Impact Assessment) Regulations 2020 , the Offshore Petroleum Regulator for Environment and " +
          "Decommissioning, acting on behalf of the Secretary of State for Business, Energy and Industrial " +
          "Strategy, does not agree to the OGA’s grant of consent for the activities described in application " +
          "reference %s and a copy of the Secretary of State’s decision is attached."
  ),
  EIA_NOT_RELEVANT(
      "Agreement to consent not required under the EIA regulations",
      "Why is agreement not required under the EIA regulations?",
      "Reasons for agreement not required",
      "Reasons for agreement not required under EIA regulations",
      "Pursuant to the Offshore Oil and Gas Exploration, Production, Unloading and Storage (Environmental " +
          "Impact Assessment) Regulations 2020 , the Offshore Petroleum Regulator for Environment and " +
          "Decommissioning, acting on behalf of the Secretary of State for Business, Energy and Industrial " +
          "Strategy, does not consider that the Secretary of State’s agreement to the grant of consent by " +
          "the OGA for the activities described in application reference %s is required."
  ),

  HABITATS_AGREE(
      "Agree",
      "Provide consent conditions if they apply",
      "Consent conditions",
      "Consent conditions under Habitats regulations",
      "Pursuant to the Offshore Petroleum Activities (Conservation of Habitats) Regulations 2001 (as amended), " +
          "the Offshore Petroleum Regulator for Environment and Decommissioning, acting on behalf of the Secretary " +
          "of State for Business, Energy and Industrial Strategy, agrees to the OGA’s grant of consent for the " +
          "activities described in application reference %s and a copy of the " +
          "Secretary of State’s decision is attached."
  ),
  HABITATS_DISAGREE(
      "Do not agree",
      "Why do you not agree to this application?",
      "Reasons for disagreement",
      "Reasons for disagreement under Habitats regulations",
      "Pursuant to the Offshore Petroleum Activities (Conservation of Habitats) Regulations 2001 (as amended), " +
          "the Offshore Petroleum Regulator for Environment and Decommissioning, acting on behalf of the Secretary " +
          "of State for Business, Energy and Industrial Strategy, does not agree to the OGA’s grant of consent for " +
          "the activities described in application reference %s and a copy of the " +
          "Secretary of State’s decision is attached."
  ),
  HABITATS_NOT_RELEVANT(
      "Agreement to consent not required under the Habitats regulations",
      "Why is agreement not required under the Habitats regulations?",
      "Reasons for agreement not required",
      "Reasons for agreement not required under Habitats regulations",
      "Pursuant to the Offshore Petroleum Activities (Conservation of Habitats) Regulations 2001 (as amended), the " +
          "Offshore Petroleum Regulator for Environment and Decommissioning, acting on behalf of the Secretary of " +
          "State for Business, Energy and Industrial Strategy, does not consider that the Secretary of State’s " +
          "agreement to the grant of consent by the OGA for the activities described in application reference " +
          "%s is required."
  );

  private final String labelText;
  private final String textAreaLabelText;
  private final String textAreaLengthValidationMessagePrefix;
  private final String textAreaViewLabelText;
  private final String radioInsetText;

  ConsultationResponseOption(String labelText,
                             String textAreaLabelText,
                             String textAreaLengthValidationMessagePrefix,
                             String textAreaViewLabelText,
                             String radioInsetText) {
    this.labelText = labelText;
    this.textAreaLabelText = textAreaLabelText;
    this.textAreaLengthValidationMessagePrefix = textAreaLengthValidationMessagePrefix;
    this.textAreaViewLabelText = textAreaViewLabelText;
    this.radioInsetText = radioInsetText;
  }

  ConsultationResponseOption(String labelText,
                             String textAreaLabelText,
                             String textAreaLengthValidationMessagePrefix,
                             String textAreaViewLabelText) {
    this.labelText = labelText;
    this.textAreaLabelText = textAreaLabelText;
    this.textAreaLengthValidationMessagePrefix = textAreaLengthValidationMessagePrefix;
    this.textAreaViewLabelText = textAreaViewLabelText;
    this.radioInsetText = "";
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

  public String getRadioInsetText(String appReference) {
    return String.format(radioInsetText, appReference);
  }

  public static List<ConsultationResponseOption> asList() {
    return Arrays.asList(ConsultationResponseOption.values());
  }

}