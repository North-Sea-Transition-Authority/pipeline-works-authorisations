package uk.co.ogauthority.pwa.features.email.emailproperties.assignments;

import java.util.Map;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class CaseOfficerAssignedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String caseOfficerName;
  private final String caseManagementLink;

  public CaseOfficerAssignedEmailProps(String recipientFullName,
                                       String applicationReference,
                                       String caseOfficerName,
                                       String caseManagementLink) {
    super(NotifyTemplate.CASE_OFFICER_ASSIGNED, recipientFullName);
    this.applicationReference = applicationReference;
    this.caseOfficerName = caseOfficerName;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("CASE_OFFICER_NAME", caseOfficerName);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getCaseOfficerName() {
    return caseOfficerName;
  }
}
