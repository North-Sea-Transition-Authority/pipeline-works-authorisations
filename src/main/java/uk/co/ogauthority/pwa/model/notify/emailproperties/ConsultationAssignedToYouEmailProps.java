package uk.co.ogauthority.pwa.model.notify.emailproperties;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ConsultationAssignedToYouEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String assigningUserFullName;
  private final String consultationDueDateDisplay;

  public ConsultationAssignedToYouEmailProps(String recipientFullName,
                                             String applicationReference,
                                             String assigningUserFullName,
                                             String consultationDueDateDisplay) {
    super(NotifyTemplate.CONSULTATION_ASSIGNED_TO_YOU, recipientFullName);
    this.applicationReference = applicationReference;
    this.assigningUserFullName = assigningUserFullName;
    this.consultationDueDateDisplay = consultationDueDateDisplay;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("ASSIGNER_FULL_NAME", assigningUserFullName);
    emailPersonalisation.put("DUE_DATE", consultationDueDateDisplay);
    return emailPersonalisation;
  }

}
