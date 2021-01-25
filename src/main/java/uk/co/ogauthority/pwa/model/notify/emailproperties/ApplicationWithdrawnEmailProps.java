package uk.co.ogauthority.pwa.model.notify.emailproperties;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ApplicationWithdrawnEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String withdrawingUserName;

  public ApplicationWithdrawnEmailProps(String recipientFullName,
                                        String applicationReference,
                                        String withdrawingUserName) {
    super(NotifyTemplate.APPLICATION_WITHDRAWN, recipientFullName);
    this.applicationReference = applicationReference;
    this.withdrawingUserName = withdrawingUserName;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
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
    ApplicationWithdrawnEmailProps that = (ApplicationWithdrawnEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(withdrawingUserName, that.withdrawingUserName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, withdrawingUserName);
  }
}
