package uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow;

import java.util.Map;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ThirdPartyConsentIssuedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String consentReference;
  private final String fieldNames;
  private final String consentIssuedDate;
  private final String caseManagementLink;


  public ThirdPartyConsentIssuedEmailProps(NotifyTemplate template,
                                           String recipientFullName, String applicationReference,
                                           String consentReference, String fieldNames, String consentIssuedDate,
                                           String pwaApplicationLink) {
    super(template, recipientFullName);
    this.applicationReference = applicationReference;
    this.consentReference = consentReference;
    this.fieldNames = fieldNames;
    this.consentIssuedDate = consentIssuedDate;
    this.caseManagementLink = pwaApplicationLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("FIELD_NAMES", fieldNames);
    emailPersonalisation.put("CONSENT_REFERENCE", consentReference);
    emailPersonalisation.put("CONSENT_ISSUED_DATE", consentIssuedDate);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getConsentReference() {
    return consentReference;
  }

  public String getConsentIssuedDate() {
    return consentIssuedDate;
  }

  public String getCaseManagementLink() {
    return caseManagementLink;
  }

  public String getFieldNames() {
    return fieldNames;
  }
}
