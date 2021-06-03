package uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class HolderSubmitterConsentIssuedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String coverLetterText;
  private final String caseManagementLink;

  public HolderSubmitterConsentIssuedEmailProps(String recipientFullName,
                                                String applicationReference,
                                                String coverLetterText,
                                                String caseManagementLink) {
    super(NotifyTemplate.HOLDER_SUBMITTER_CONSENT_ISSUED, recipientFullName);
    this.applicationReference = applicationReference;
    this.coverLetterText = coverLetterText;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("COVER_LETTER_TEXT", coverLetterText);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getCoverLetterText() {
    return coverLetterText;
  }

  public String getCaseManagementLink() {
    return caseManagementLink;
  }

}
