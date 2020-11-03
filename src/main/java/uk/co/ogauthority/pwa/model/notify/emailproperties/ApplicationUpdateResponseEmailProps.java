package uk.co.ogauthority.pwa.model.notify.emailproperties;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ApplicationUpdateResponseEmailProps extends EmailProperties {

  private final String applicationReference;

  public ApplicationUpdateResponseEmailProps(String recipientFullName,
                                             String applicationReference) {
    super(NotifyTemplate.APPLICATION_UPDATE_RESPONDED, recipientFullName);
    this.applicationReference = applicationReference;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

}
