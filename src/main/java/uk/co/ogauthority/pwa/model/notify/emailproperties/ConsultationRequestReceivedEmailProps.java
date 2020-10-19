package uk.co.ogauthority.pwa.model.notify.emailproperties;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ConsultationRequestReceivedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String consulteeGroupName;
  private final String caseManagementLink;

  public ConsultationRequestReceivedEmailProps(String recipientFullName,
                                               String applicationReference, String consulteeGroupName,
                                               String caseManagementLink) {
    super(NotifyTemplate.CONSULTATION_REQUEST_RECEIVED, recipientFullName);
    this.applicationReference = applicationReference;
    this.consulteeGroupName = consulteeGroupName;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("CONSULTEE_GROUP_NAME", consulteeGroupName);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsultationRequestReceivedEmailProps that = (ConsultationRequestReceivedEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(consulteeGroupName, that.consulteeGroupName)
        && Objects.equals(caseManagementLink, that.caseManagementLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, consulteeGroupName, caseManagementLink);
  }
}
