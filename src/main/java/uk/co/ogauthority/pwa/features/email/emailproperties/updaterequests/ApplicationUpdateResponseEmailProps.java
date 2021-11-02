package uk.co.ogauthority.pwa.features.email.emailproperties.updaterequests;

import java.util.Map;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ApplicationUpdateResponseEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String caseManagementLink;

  public ApplicationUpdateResponseEmailProps(String recipientFullName,
                                             String applicationReference,
                                             String caseManagementLink) {
    super(NotifyTemplate.APPLICATION_UPDATE_RESPONDED, recipientFullName);
    this.applicationReference = applicationReference;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

}
