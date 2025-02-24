package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.asbuilt.AsBuiltNotificationDeadlinePassedEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.asbuilt.AsBuiltNotificationDeadlineUpcomingEmailProps;
import uk.co.ogauthority.pwa.features.email.emailproperties.asbuilt.AsBuiltNotificationNotPerConsentEmailProps;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class AsBuiltNotificationEmailServiceTest {

  private AsBuiltNotificationEmailService asBuiltNotificationEmailService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private CaseLinkService caseLinkService;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationNotPerConsentEmailProps> asBuiltNotificationNotPerConsentEmailPropsArgumentCaptor;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationDeadlineUpcomingEmailProps> asBuiltNotificationDeadlineUpcomingEmailPropsArgumentCaptor;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationDeadlinePassedEmailProps> asBuiltNotificationDeadlinePassedEmailPropsArgumentCaptor;

  private final AsBuiltNotificationGroup asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil.createGroupWithConsent_fromNgId(10);

  private final PipelineDetail pipelineDetail = PipelineDetailTestUtil.createPipelineDetail_withDefaultPipelineNumber(10,
      new PipelineId(20), Instant.now());

  private static final String OGA_CONSENTS_EMAIL = "consents@ogauthority.co.uk";
  private static final String PIPELINE_NUMBER = "PL8";
  private static final AsBuiltNotificationStatus AS_BUILT_NOTIFICATION_STATUS = AsBuiltNotificationStatus.NOT_PER_CONSENT;

  private final Person person = PersonTestUtil.createDefaultPerson();

  @BeforeEach
  void setUp() {

    asBuiltNotificationEmailService = new AsBuiltNotificationEmailService(notifyService, caseLinkService, OGA_CONSENTS_EMAIL);



  }

  @Test
  void sendAsBuiltNotificationNotPerConsentEmail() {
    String caseManagementLink = "case management link url";
    when(caseLinkService.generateAsBuiltNotificationSummaryLink(asBuiltNotificationGroup.getMasterPwaIdFromGroupConsent(), pipelineDetail.getPipelineId().asInt()))
        .thenReturn(caseManagementLink);

    asBuiltNotificationEmailService.sendAsBuiltNotificationNotPerConsentEmail(OGA_CONSENTS_EMAIL, "recipientName",
        asBuiltNotificationGroup, pipelineDetail, AS_BUILT_NOTIFICATION_STATUS);
    verify(notifyService).sendEmail(asBuiltNotificationNotPerConsentEmailPropsArgumentCaptor.capture(), eq(OGA_CONSENTS_EMAIL));

    assertThat(asBuiltNotificationNotPerConsentEmailPropsArgumentCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
        "RECIPIENT_FULL_NAME", "recipientName",
        "AS_BUILT_GROUP_REF", asBuiltNotificationGroup.getReference(),
        "PIPELINE_NUMBER", pipelineDetail.getPipelineNumber(),
        "AS_BUILT_NOTIFICATION_STATUS", AS_BUILT_NOTIFICATION_STATUS.getDisplayName(),
        "AS_BUILT_DASHBOARD_LINK", caseManagementLink
    ));
  }

  @Test
  void sendAsBuiltNotificationDeadlineUpcomingEmail() {
    when(caseLinkService.generateAsBuiltNotificationWorkareaLink()).thenCallRealMethod();
    asBuiltNotificationEmailService.sendUpcomingDeadlineEmail(person.getEmailAddress(), person.getFullName(),
        asBuiltNotificationGroup.getReference());
    verify(notifyService).sendEmail(asBuiltNotificationDeadlineUpcomingEmailPropsArgumentCaptor.capture(), eq(person.getEmailAddress()));

    assertThat(asBuiltNotificationDeadlineUpcomingEmailPropsArgumentCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
        "RECIPIENT_FULL_NAME", person.getFullName(),
        "AS_BUILT_GROUP_REFERENCES", asBuiltNotificationGroup.getReference(),
        "AS_BUILT_WORKAREA_LINK", caseLinkService.generateAsBuiltNotificationWorkareaLink()
    ));
  }

  @Test
  void sendAsBuiltNotificationDeadlinePassedEmail() {
    when(caseLinkService.generateAsBuiltNotificationWorkareaLink()).thenCallRealMethod();
    asBuiltNotificationEmailService.sendDeadlinePassedEmail(person.getEmailAddress(), person.getFullName(),
        asBuiltNotificationGroup.getReference());
    verify(notifyService).sendEmail(asBuiltNotificationDeadlinePassedEmailPropsArgumentCaptor.capture(), eq(person.getEmailAddress()));

    assertThat(asBuiltNotificationDeadlinePassedEmailPropsArgumentCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
        "RECIPIENT_FULL_NAME", person.getFullName(),
        "AS_BUILT_GROUP_REFERENCES", asBuiltNotificationGroup.getReference(),
        "OGA_CONSENTS_EMAIL", OGA_CONSENTS_EMAIL,
        "AS_BUILT_WORKAREA_LINK", caseLinkService.generateAsBuiltNotificationWorkareaLink()
    ));
  }

}
