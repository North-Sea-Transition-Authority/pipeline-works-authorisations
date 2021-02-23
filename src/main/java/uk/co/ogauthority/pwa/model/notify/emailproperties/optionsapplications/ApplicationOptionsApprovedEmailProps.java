package uk.co.ogauthority.pwa.model.notify.emailproperties.optionsapplications;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class ApplicationOptionsApprovedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String holders;
  private final String deadlineDate;
  private final String caseManagementLink;

  public ApplicationOptionsApprovedEmailProps(String recipientFullName,
                                              String applicationReference,
                                              String holder,
                                              String deadlineDate,
                                              String caseManagementLink) {
    super(NotifyTemplate.APPLICATION_OPTIONS_APPROVED, recipientFullName);
    this.applicationReference = applicationReference;
    this.holders = holder;
    this.deadlineDate = deadlineDate;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("HOLDER", holders);
    emailPersonalisation.put("DEADLINE_DATE", deadlineDate);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

}
