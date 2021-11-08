package uk.co.ogauthority.pwa.features.email.emailproperties.assignments;

import java.util.Map;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ApplicationAssignedToYouEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String assigningUserFullName;
  private final String caseManagementLink;

  public ApplicationAssignedToYouEmailProps(String recipientFullName,
                                            String applicationReference,
                                            String assigningUserFullName, String caseManagementLink) {
    super(NotifyTemplate.APPLICATION_ASSIGNED_TO_YOU, recipientFullName);
    this.applicationReference = applicationReference;
    this.assigningUserFullName = assigningUserFullName;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("RECIPIENT_FULL_NAME", getRecipientFullName());
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    emailPersonalisation.put("ASSIGNING_PERSON_FULL_NAME", assigningUserFullName);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getAssigningUserFullName() {
    return assigningUserFullName;
  }

  public String getCaseManagementLink() {
    return caseManagementLink;
  }

}
