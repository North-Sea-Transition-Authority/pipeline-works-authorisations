package uk.co.ogauthority.pwa.integrations.govuknotify;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class EmailDeliveryFailedEmailProps extends EmailProperties {

  private final String failedEmailAddress;
  private final String originalSubject;
  private final String originalBody;

  public EmailDeliveryFailedEmailProps(String failedEmailAddress,
                                       String originalSubject,
                                       String originalBody) {
    super(NotifyTemplate.EMAIL_DELIVERY_FAILED);
    this.failedEmailAddress = failedEmailAddress;
    this.originalSubject = originalSubject;
    this.originalBody = originalBody;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("FAILED_EMAIL_ADDRESS", failedEmailAddress);
    emailPersonalisation.put("EMAIL_SUBJECT", originalSubject);
    emailPersonalisation.put("EMAIL_BODY", originalBody);
    return emailPersonalisation;
  }

  public String getFailedEmailAddress() {
    return failedEmailAddress;
  }

  public String getOriginalSubject() {
    return originalSubject;
  }

  public String getOriginalBody() {
    return originalBody;
  }
}
