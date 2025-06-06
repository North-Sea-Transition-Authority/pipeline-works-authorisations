package uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow;

import java.util.Map;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class CaseOfficerConsentIssuedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String issuingPersonName;

  public CaseOfficerConsentIssuedEmailProps(String recipientFullName,
                                            String applicationReference,
                                            String issuingPersonName) {
    super(NotifyTemplate.CONSENT_ISSUED, recipientFullName);
    this.applicationReference = applicationReference;
    this.issuingPersonName = issuingPersonName;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("ISSUING_PERSON_NAME", issuingPersonName);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getIssuingPersonName() {
    return issuingPersonName;
  }

}
