package uk.co.ogauthority.pwa.model.notify.emailproperties.asbuilt;

import java.util.Map;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;

public class AsBuiltNotificationNotPerConsentEmailProps extends EmailProperties {

  private final String asBuiltGroupReference;
  private final String pipelineNumber;
  private final AsBuiltNotificationStatus asBuiltNotificationStatus;
  private final String asBuiltDashboardLink;

  public AsBuiltNotificationNotPerConsentEmailProps(String recipientFullName, String asBuiltGroupReference, String pipelineNumber,
                                                    AsBuiltNotificationStatus asBuiltNotificationStatus,
                                                    String asBuiltDashboardLink) {
    super(NotifyTemplate.AS_BUILT_NOTIFICATION_NOT_PER_CONSENT, recipientFullName);
    this.asBuiltGroupReference = asBuiltGroupReference;
    this.pipelineNumber = pipelineNumber;
    this.asBuiltNotificationStatus = asBuiltNotificationStatus;
    this.asBuiltDashboardLink = asBuiltDashboardLink;
  }

  @Override
  public Map<String, String> getEmailPersonalisation() {
    Map<String, String> emailPersonalisation = super.getEmailPersonalisation();
    emailPersonalisation.put("AS_BUILT_GROUP_REF", asBuiltGroupReference);
    emailPersonalisation.put("PIPELINE_NUMBER", pipelineNumber);
    emailPersonalisation.put("AS_BUILT_NOTIFICATION_STATUS", asBuiltNotificationStatus.getDisplayName());
    emailPersonalisation.put("AS_BUILT_DASHBOARD_LINK", asBuiltDashboardLink);
    return emailPersonalisation;
  }

}