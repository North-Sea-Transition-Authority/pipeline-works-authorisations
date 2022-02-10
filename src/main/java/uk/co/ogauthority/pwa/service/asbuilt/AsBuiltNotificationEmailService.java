package uk.co.ogauthority.pwa.service.asbuilt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.asbuilt.AsBuiltNotificationDeadlinePassedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.asbuilt.AsBuiltNotificationDeadlineUpcomingEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.asbuilt.AsBuiltNotificationNotPerConsentEmailProps;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;

@Service
class AsBuiltNotificationEmailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsBuiltNotificationEmailService.class);

  private final NotifyService notifyService;
  private final CaseLinkService caseLinkService;
  private final String ogaConsentsMailboxEmail;

  @Autowired
  public AsBuiltNotificationEmailService(NotifyService notifyService,
                                         CaseLinkService caseLinkService,
                                         @Value("${oga.consents.email}") String ogaConsentsMailboxEmail) {
    this.notifyService = notifyService;
    this.caseLinkService = caseLinkService;
    this.ogaConsentsMailboxEmail = ogaConsentsMailboxEmail;
  }

  void sendAsBuiltNotificationNotPerConsentEmail(String recipientEmail,
                                                 String recipientName,
                                                 AsBuiltNotificationGroup asBuiltNotificationGroup,
                                                 PipelineDetail pipelineDetail,
                                                 AsBuiltNotificationStatus asBuiltNotificationStatus) {
    var emailProps = new AsBuiltNotificationNotPerConsentEmailProps(
        recipientName,
        asBuiltNotificationGroup.getReference(),
        pipelineDetail.getPipelineNumber(),
        asBuiltNotificationStatus,
        caseLinkService.generateAsBuiltNotificationSummaryLink(
            asBuiltNotificationGroup.getMasterPwaIdFromGroupConsent(),
            pipelineDetail.getPipelineId().asInt())
        );

    LOGGER.debug("Sending as-built notification not per consent email to {}", recipientEmail);
    notifyService.sendEmail(emailProps, recipientEmail);
  }

  void sendUpcomingDeadlineEmail(String recipientEmail,
                                 String recipientName,
                                 String asBuiltNotificationGroupReferences) {
    var emailProps = new AsBuiltNotificationDeadlineUpcomingEmailProps(
        recipientName,
        asBuiltNotificationGroupReferences,
        caseLinkService.generateAsBuiltNotificationWorkareaLink()
    );

    LOGGER.debug("Sending upcoming as-built deadline notification email to {}", recipientEmail);
    notifyService.sendEmail(emailProps, recipientEmail);
  }

  void sendDeadlinePassedEmail(String recipientEmail,
                               String recipientName,
                               String asBuiltNotificationGroupReferences) {
    var emailProps = new AsBuiltNotificationDeadlinePassedEmailProps(
        recipientName,
        asBuiltNotificationGroupReferences,
        ogaConsentsMailboxEmail,
        caseLinkService.generateAsBuiltNotificationWorkareaLink()
    );

    LOGGER.debug("Sending past as-built deadline notification email to {}", recipientEmail);
    notifyService.sendEmail(emailProps, recipientEmail);
  }

}
