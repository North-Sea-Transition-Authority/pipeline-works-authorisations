package uk.co.ogauthority.pwa.model.notify.emailproperties;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ConsultationWithdrawnEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String consulteeGroupName;
  private final String withdrawingUserName;

  public ConsultationWithdrawnEmailProps(String recipientFullName,
                                         String applicationReference, String consulteeGroupName,
                                         String withdrawingUserName) {
    super(NotifyTemplate.CONSULTATION_WITHDRAWN, recipientFullName);
    this.applicationReference = applicationReference;
    this.consulteeGroupName = consulteeGroupName;
    this.withdrawingUserName = withdrawingUserName;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("CONSULTEE_GROUP_NAME", consulteeGroupName);
    emailPersonalisation.put("WITHDRAWING_USER_NAME", withdrawingUserName);
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
    ConsultationWithdrawnEmailProps that = (ConsultationWithdrawnEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(consulteeGroupName, that.consulteeGroupName)
        && Objects.equals(withdrawingUserName, that.withdrawingUserName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, consulteeGroupName, withdrawingUserName);
  }
}
