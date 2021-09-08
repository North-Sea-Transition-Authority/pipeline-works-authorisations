package uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class ConsentIssuedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String consentReference;
  private final String coverLetterText;
  private final String caseOfficerEmail;
  private final String caseManagementLink;

  public ConsentIssuedEmailProps(NotifyTemplate notifyTemplate,
                                 String recipientFullName,
                                 String applicationReference,
                                 String consentReference,
                                 String coverLetterText,
                                 String caseOfficerEmail,
                                 String caseManagementLink) {
    super(notifyTemplate, recipientFullName);
    this.applicationReference = applicationReference;
    this.consentReference = consentReference;
    this.coverLetterText = coverLetterText;
    this.caseOfficerEmail = caseOfficerEmail;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("CONSENT_REFERENCE", consentReference);
    emailPersonalisation.put("COVER_LETTER_TEXT", coverLetterText);
    emailPersonalisation.put("CASE_OFFICER_EMAIL", caseOfficerEmail);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getConsentReference() {
    return consentReference;
  }

  public String getCoverLetterText() {
    return coverLetterText;
  }

  public String getCaseOfficerEmail() {
    return caseOfficerEmail;
  }

  public String getCaseManagementLink() {
    return caseManagementLink;
  }

}
