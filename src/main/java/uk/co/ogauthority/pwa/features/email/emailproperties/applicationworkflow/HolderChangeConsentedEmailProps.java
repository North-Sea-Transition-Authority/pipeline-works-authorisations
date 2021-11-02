package uk.co.ogauthority.pwa.features.email.emailproperties.applicationworkflow;

import java.util.Map;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;

public class HolderChangeConsentedEmailProps extends EmailProperties {

  private final String applicationType;
  private final String applicationReference;
  private final String pwaReference;
  private final String oldHolderName;
  private final String newHolderName;

  public HolderChangeConsentedEmailProps(String recipientFullName,
                                         String applicationType,
                                         String applicationReference,
                                         String pwaReference,
                                         String oldHolderNameCsv,
                                         String newHolderNameCsv) {
    super(NotifyTemplate.HOLDER_CHANGE_CONSENTED, recipientFullName);
    this.applicationType = applicationType;
    this.applicationReference = applicationReference;
    this.pwaReference = pwaReference;
    this.oldHolderName = oldHolderNameCsv;
    this.newHolderName = newHolderNameCsv;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("APPLICATION_TYPE", applicationType);
    emailPersonalisation.put("APPLICATION_REFERENCE", applicationReference);
    emailPersonalisation.put("PWA_REFERENCE", pwaReference);
    emailPersonalisation.put("OLD_HOLDER_NAME", oldHolderName);
    emailPersonalisation.put("NEW_HOLDER_NAME", newHolderName);
    return emailPersonalisation;
  }

  public String getApplicationType() {
    return applicationType;
  }

  public String getApplicationReference() {
    return applicationReference;
  }

  public String getPwaReference() {
    return pwaReference;
  }

  public String getOldHolderName() {
    return oldHolderName;
  }

  public String getNewHolderName() {
    return newHolderName;
  }

}
