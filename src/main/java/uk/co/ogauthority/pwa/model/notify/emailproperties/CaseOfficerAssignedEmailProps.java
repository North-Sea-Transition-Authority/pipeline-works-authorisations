package uk.co.ogauthority.pwa.model.notify.emailproperties;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class CaseOfficerAssignedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String caseOfficerName;

  public CaseOfficerAssignedEmailProps(String recipientFullName,
                                       String applicationReference,
                                       String caseOfficerName) {
    super(NotifyTemplate.CASE_OFFICER_ASSIGNED, recipientFullName);
    this.applicationReference = applicationReference;
    this.caseOfficerName = caseOfficerName;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("CASE_OFFICER_NAME", caseOfficerName);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getCaseOfficerName() {
    return caseOfficerName;
  }
}
