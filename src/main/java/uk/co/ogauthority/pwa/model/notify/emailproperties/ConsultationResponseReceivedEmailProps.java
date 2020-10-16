package uk.co.ogauthority.pwa.model.notify.emailproperties;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ConsultationResponseReceivedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String consulteeGroupName;
  private final String consultationResponse;
  private final String caseManagementLink;

  public ConsultationResponseReceivedEmailProps(String recipientFullName,
                                                String applicationReference,
                                                String consulteeGroupName,
                                                String consultationResponse,
                                                String caseManagementLink) {
    super(NotifyTemplate.CONSULTATION_RESPONSE_RECEIVED, recipientFullName);
    this.applicationReference = applicationReference;
    this.consulteeGroupName = consulteeGroupName;
    this.consultationResponse = consultationResponse;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("CONSULTEE_GROUP", consulteeGroupName);
    emailPersonalisation.put("CONSULTATION_RESPONSE", consultationResponse);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

}
