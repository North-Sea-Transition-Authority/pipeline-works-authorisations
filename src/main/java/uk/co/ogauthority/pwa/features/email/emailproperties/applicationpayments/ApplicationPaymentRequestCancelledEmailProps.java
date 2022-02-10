package uk.co.ogauthority.pwa.features.email.emailproperties.applicationpayments;

import java.util.Map;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class ApplicationPaymentRequestCancelledEmailProps extends EmailProperties {

  private final String applicationReference;

  public ApplicationPaymentRequestCancelledEmailProps(String recipientFullName,
                                                      String applicationReference) {
    super(NotifyTemplate.APPLICATION_PAYMENT_REQUEST_CANCELLED, recipientFullName);
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
