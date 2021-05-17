package uk.co.ogauthority.pwa.model.notify.emailproperties.applicationworkflow;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class ApplicationSubmittedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String applicationType;
  private final String caseManagementLink;

  public ApplicationSubmittedEmailProps(String recipientFullName,
                                        String applicationReference,
                                        String applicationType,
                                        String caseManagementLink) {
    super(NotifyTemplate.APPLICATION_SUBMITTED, recipientFullName);
    this.applicationReference = applicationReference;
    this.applicationType = applicationType;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("APPLICATION_TYPE", applicationType);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getApplicationType() {
    return applicationType;
  }
}
