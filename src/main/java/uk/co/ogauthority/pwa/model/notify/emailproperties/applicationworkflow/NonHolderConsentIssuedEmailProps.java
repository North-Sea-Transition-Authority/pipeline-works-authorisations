package uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class NonHolderConsentIssuedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String coverLetterText;

  public NonHolderConsentIssuedEmailProps(String recipientFullName,
                                          String applicationReference,
                                          String coverLetterText) {
    super(NotifyTemplate.NON_HOLDER_CONSENT_ISSUED, recipientFullName);
    this.applicationReference = applicationReference;
    this.coverLetterText = coverLetterText;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("COVER_LETTER_TEXT", coverLetterText);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getCoverLetterText() {
    return coverLetterText;
  }


}
