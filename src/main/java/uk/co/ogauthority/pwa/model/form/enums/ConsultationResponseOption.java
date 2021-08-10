package uk.co.ogauthority.pwa.model.form.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum ConsultationResponseOption {

  CONFIRMED(
      "Confirm contentedness",
      "Provide consent conditions if they apply",
      "Consent conditions",
      "Consent conditions",
      null, false),
  REJECTED(
      "Rejected",
      "Why are you rejecting this application?",
      "Reason for rejecting the application",
      "Rejection reason",
      null, false),

  PROVIDE_ADVICE(
      "Provide advice",
      "Advice text",
      "Advice text",
      "Advice text",
      "Provide advice - On the basis of the information received HSE is able to provide the following advice " +
          "regarding the work proposed in the application. HSE’s advice is listed below.\n\n", true),
  NO_ADVICE(
      "No advice",
      "Provide comments if they apply",
      "Comments",
      "Comments",
      "No advice - On the basis of the information received HSE does not have any advice " +
          "regarding the work proposed in the application.", false),

  EIA_AGREE(
      "Agree",
      "Provide consent conditions if they apply",
      "Consent conditions",
      "EIA consent conditions",
      "Pursuant to the Offshore Oil and Gas Exploration, Production, Unloading and Storage " +
          "(Environmental Impact Assessment) Regulations 2020 , the Offshore Petroleum Regulator for " +
          "Environment and Decommissioning, acting on behalf of the Secretary of State for Business, " +
          "Energy and Industrial Strategy, agrees to the OGA’s grant of consent for the activities " +
          "described in application reference %s and a copy of the " +
          "Secretary of State’s decision is attached.",
      null, false),
  EIA_DISAGREE(
      "Do not agree",
      "Why do you not agree to this application?",
      "Reasons for disagreement",
      "Why does not agree under EIA regulations",
      "Pursuant to the Offshore Oil and Gas Exploration, Production, Unloading and Storage (Environmental " +
          "Impact Assessment) Regulations 2020 , the Offshore Petroleum Regulator for Environment and " +
          "Decommissioning, acting on behalf of the Secretary of State for Business, Energy and Industrial " +
          "Strategy, does not agree to the OGA’s grant of consent for the activities described in application " +
          "reference %s and a copy of the Secretary of State’s decision is attached.",
      null, false),
  EIA_NOT_RELEVANT(
      "Agreement to consent not required under the EIA regulations",
      "Why is agreement not required under the EIA regulations?",
      "Reasons for agreement not required",
      "Why not required under EIA regulations",
      "Pursuant to the Offshore Oil and Gas Exploration, Production, Unloading and Storage (Environmental " +
          "Impact Assessment) Regulations 2020 , the Offshore Petroleum Regulator for Environment and " +
          "Decommissioning, acting on behalf of the Secretary of State for Business, Energy and Industrial " +
          "Strategy, does not consider that the Secretary of State’s agreement to the grant of consent by " +
          "the OGA for the activities described in application reference %s is required.",
      null, false),

  HABITATS_AGREE(
      "Agree",
      "Provide consent conditions if they apply",
      "Consent conditions",
      "Habitats consent conditions",
      "Pursuant to the Offshore Petroleum Activities (Conservation of Habitats) Regulations 2001 (as amended), " +
          "the Offshore Petroleum Regulator for Environment and Decommissioning, acting on behalf of the Secretary " +
          "of State for Business, Energy and Industrial Strategy, agrees to the OGA’s grant of consent for the " +
          "activities described in application reference %s and a copy of the " +
          "Secretary of State’s decision is attached.",
      null, false),
  HABITATS_DISAGREE(
      "Do not agree",
      "Why do you not agree to this application?",
      "Reasons for disagreement",
      "Why does not agree under Habitats regulations",
      "Pursuant to the Offshore Petroleum Activities (Conservation of Habitats) Regulations 2001 (as amended), " +
          "the Offshore Petroleum Regulator for Environment and Decommissioning, acting on behalf of the Secretary " +
          "of State for Business, Energy and Industrial Strategy, does not agree to the OGA’s grant of consent for " +
          "the activities described in application reference %s and a copy of the " +
          "Secretary of State’s decision is attached.",
      null, false),
  HABITATS_NOT_RELEVANT(
      "Agreement to consent not required under the Habitats regulations",
      "Why is agreement not required under the Habitats regulations?",
      "Reasons for agreement not required",
      "Why not required under Habitats regulations",
      "Pursuant to the Offshore Petroleum Activities (Conservation of Habitats) Regulations 2001 (as amended), the " +
          "Offshore Petroleum Regulator for Environment and Decommissioning, acting on behalf of the Secretary of " +
          "State for Business, Energy and Industrial Strategy, does not consider that the Secretary of State’s " +
          "agreement to the grant of consent by the OGA for the activities described in application reference " +
          "%s is required.",
      null, false);

  private final String labelText;
  private final String textAreaLabelText;
  private final String textAreaLengthValidationMessagePrefix;
  private final String textAreaViewLabelText;
  private final String radioInsetText;
  private final String emailText;
  private final boolean includeResponseTextInEmail;

  ConsultationResponseOption(String labelText,
                             String textAreaLabelText,
                             String textAreaLengthValidationMessagePrefix,
                             String textAreaViewLabelText,
                             String radioInsetText,
                             String emailText,
                             boolean includeResponseTextInEmail) {
    this.labelText = labelText;
    this.textAreaLabelText = textAreaLabelText;
    this.textAreaLengthValidationMessagePrefix = textAreaLengthValidationMessagePrefix;
    this.textAreaViewLabelText = textAreaViewLabelText;
    this.radioInsetText = radioInsetText;
    this.emailText = emailText;
    this.includeResponseTextInEmail = includeResponseTextInEmail;
  }

  ConsultationResponseOption(String labelText,
                             String textAreaLabelText,
                             String textAreaLengthValidationMessagePrefix,
                             String textAreaViewLabelText,
                             String emailText,
                             boolean includeResponseTextInEmail) {
    this.labelText = labelText;
    this.textAreaLabelText = textAreaLabelText;
    this.textAreaLengthValidationMessagePrefix = textAreaLengthValidationMessagePrefix;
    this.textAreaViewLabelText = textAreaViewLabelText;
    this.radioInsetText = "";
    this.emailText = emailText;
    this.includeResponseTextInEmail = includeResponseTextInEmail;
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

  public Optional<String> getEmailText() {
    return Optional.ofNullable(emailText);
  }

  public boolean includeResponseTextInEmail() {
    return includeResponseTextInEmail;
  }

  public static List<ConsultationResponseOption> asList() {
    return Arrays.asList(ConsultationResponseOption.values());
  }

}