package uk.co.ogauthority.pwa.model.notify.emailproperties.consultations;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class ConsultationMultiResponseReceivedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String consulteeGroupName;
  private final String consultationResponses;
  private final String caseManagementLink;

  public ConsultationMultiResponseReceivedEmailProps(String recipientFullName,
                                                     String applicationReference,
                                                     String consulteeGroupName,
                                                     String consultationResponses,
                                                     String caseManagementLink) {
    super(NotifyTemplate.CONSULTATION_MULTI_RESPONSE_RECEIVED, recipientFullName);
    this.applicationReference = applicationReference;
    this.consulteeGroupName = consulteeGroupName;
    this.consultationResponses = consultationResponses;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("CONSULTEE_GROUP", consulteeGroupName);
    emailPersonalisation.put("CONSULTATION_RESPONSES", consultationResponses);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

}
