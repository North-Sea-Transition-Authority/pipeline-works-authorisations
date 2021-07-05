package uk.co.ogauthority.pwa.service.asbuilt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.notify.emailproperties.asbuilt.AsBuiltNotificationNotPerConsentEmailProps;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;

@Service
public class AsBuiltNotificationEmailService {

  private final NotifyService notifyService;
  private final EmailCaseLinkService emailCaseLinkService;

  @Autowired
  public AsBuiltNotificationEmailService(NotifyService notifyService,
                                         EmailCaseLinkService emailCaseLinkService) {
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
  }

  public void sendAsBuiltNotificationNotPerConsentEmail(String recipientEmail,
                                                        String recipientName,
                                                        AsBuiltNotificationGroup asBuiltNotificationGroup,
                                                        PipelineDetail pipelineDetail,
                                                        AsBuiltNotificationStatus asBuiltNotificationStatus) {
    var emailProps = new AsBuiltNotificationNotPerConsentEmailProps(
        recipientName,
        asBuiltNotificationGroup.getReference(),
        pipelineDetail.getPipelineNumber(),
        asBuiltNotificationStatus,
        emailCaseLinkService.generateAsBuiltNotificationSummaryLink(
            asBuiltNotificationGroup.getMasterPwaIdFromGroupConsent(),
            pipelineDetail.getPipelineId().asInt())
        );

    notifyService.sendEmail(emailProps, recipientEmail);
  }

}
