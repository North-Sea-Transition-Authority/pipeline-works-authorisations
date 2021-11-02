package uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow;

import java.util.Map;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ConsentReviewEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String submittingUserName;
  private final String caseManagementLink;

  public ConsentReviewEmailProps(String recipientFullName,
                                 String applicationReference,
                                 String submittingUserName,
                                 String caseManagementLink) {
    super(NotifyTemplate.CONSENT_REVIEW, recipientFullName);
    this.applicationReference = applicationReference;
    this.submittingUserName = submittingUserName;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("SUBMITTING_USER_NAME", submittingUserName);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getSubmittingUserName() {
    return submittingUserName;
  }

  public String getCaseManagementLink() {
    return caseManagementLink;
  }

}
