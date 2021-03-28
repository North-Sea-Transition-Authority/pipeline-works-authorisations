package uk.co.ogauthority.pwa.model.notify.emailproperties.applicationpayments;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class ApplicationPaymentRequestIssuedEmailProps extends EmailProperties {

  private final String applicationReference;
  private final String caseManagementLink;

  public ApplicationPaymentRequestIssuedEmailProps(String recipientFullName,
                                                   String applicationReference,
                                                   String caseManagementLink) {
    super(NotifyTemplate.APPLICATION_PAYMENT_REQUEST_ISSUED, recipientFullName);
    this.applicationReference = applicationReference;
    this.caseManagementLink = caseManagementLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("CASE_MANAGEMENT_LINK", caseManagementLink);
    return emailPersonalisation;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getCaseManagementLink() {
    return caseManagementLink;
  }
}
