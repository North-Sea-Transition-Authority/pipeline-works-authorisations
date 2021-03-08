package uk.co.ogauthority.pwa.model.notify.emailproperties.optionsapplications;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class ApplicationOptionsApprovalDeadlineChangedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String deadlineDate;
  private final String caseManagementLink;

  public ApplicationOptionsApprovalDeadlineChangedEmailProps(String recipientFullName,
                                                             String applicationReference,
                                                             String deadlineDate,
                                                             String caseManagementLink) {
    super(NotifyTemplate.APPLICATION_OPTIONS_APPROVAL_DEADLINE_CHANGE, recipientFullName);
    this.applicationReference = applicationReference;
    this.deadlineDate = deadlineDate;

    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("DEADLINE_DATE", deadlineDate);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

}
