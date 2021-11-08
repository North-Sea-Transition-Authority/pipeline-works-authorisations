package uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow;

import java.util.Map;
import java.util.Objects;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ApplicationWithdrawnEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String withdrawingUserName;
  private final String caseManagementLink;

  public ApplicationWithdrawnEmailProps(String recipientFullName,
                                        String applicationReference,
                                        String withdrawingUserName,
                                        String caseManagementLink) {
    super(NotifyTemplate.APPLICATION_WITHDRAWN, recipientFullName);
    this.applicationReference = applicationReference;
    this.withdrawingUserName = withdrawingUserName;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("WITHDRAWING_USER_NAME", withdrawingUserName);
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
    ApplicationWithdrawnEmailProps that = (ApplicationWithdrawnEmailProps) o;
    return Objects.equals(applicationReference, that.applicationReference)
        && Objects.equals(withdrawingUserName, that.withdrawingUserName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationReference, withdrawingUserName);
  }
}
