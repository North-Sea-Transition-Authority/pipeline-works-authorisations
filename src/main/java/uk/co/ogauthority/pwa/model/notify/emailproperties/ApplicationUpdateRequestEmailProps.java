package uk.co.ogauthority.pwa.model.notify.emailproperties;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ApplicationUpdateRequestEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String requesterName;

  public ApplicationUpdateRequestEmailProps(String recipientFullName,
                                            String applicationReference,
                                            String requesterName) {
    super(NotifyTemplate.APPLICATION_UPDATE_REQUESTED, recipientFullName);
    this.applicationReference = applicationReference;
    this.requesterName = requesterName;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("REQUESTER_NAME", requesterName);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getRequesterName() {
    return requesterName;
  }

}
