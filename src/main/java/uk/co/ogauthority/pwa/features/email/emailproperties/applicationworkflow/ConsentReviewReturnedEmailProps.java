package uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow;

import java.util.Map;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ConsentReviewReturnedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String returningPersonName;
  private final String returnReason;
  private final String caseManagementLink;

  public ConsentReviewReturnedEmailProps(String recipientFullName,
                                         String applicationReference,
                                         String returningPersonName,
                                         String returnReason,
                                         String caseManagementLink) {
    super(NotifyTemplate.CONSENT_REVIEW_RETURNED, recipientFullName);
    this.applicationReference = applicationReference;
    this.returningPersonName = returningPersonName;
    this.returnReason = returnReason;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("RETURNING_PERSON_NAME", returningPersonName);
    emailPersonalisation.put("RETURN_REASON", returnReason);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getReturningPersonName() {
    return returningPersonName;
  }

  public String getReturnReason() {
    return returnReason;
  }

  public String getCaseManagementLink() {
    return caseManagementLink;
  }

}
