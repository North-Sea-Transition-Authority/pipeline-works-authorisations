package uk.co.ogauthority.pwa.model.notify.emailproperties;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ApplicationSubmittedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String applicationType;

  public ApplicationSubmittedEmailProps(String recipientFullName,
                                        String applicationReference,
                                        String applicationType) {
    super(NotifyTemplate.APPLICATION_SUBMITTED, recipientFullName);
    this.applicationReference = applicationReference;
    this.applicationType = applicationType;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("APPLICATION_TYPE", applicationType);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getApplicationType() {
    return applicationType;
  }
}
