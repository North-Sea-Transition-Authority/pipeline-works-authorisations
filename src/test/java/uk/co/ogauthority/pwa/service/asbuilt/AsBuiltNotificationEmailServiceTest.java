package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.notify.emailproperties.asbuilt.AsBuiltNotificationNotPerConsentEmailProps;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaconsents.testutil.PipelineDetailTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltNotificationEmailServiceTest {

  private AsBuiltNotificationEmailService asBuiltNotificationEmailService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Captor
  private ArgumentCaptor<AsBuiltNotificationNotPerConsentEmailProps> asBuiltNotificationNotPerConsentEmailPropsArgumentCaptor;

  private final AsBuiltNotificationGroup asBuiltNotificationGroup = AsBuiltNotificationGroupTestUtil.createGroupWithConsent_withNgId(10);

  private final PipelineDetail pipelineDetail = PipelineDetailTestUtil.createPipelineDetail_withDefaultPipelineNumber(10,
      new PipelineId(20), Instant.now());

  private static final String OGA_CONSENTS_EMAIL = "consents@ogauthority.co.uk";
  private static final AsBuiltNotificationStatus AS_BUILT_NOTIFICATION_STATUS = AsBuiltNotificationStatus.NOT_PER_CONSENT;

  @Before
  public void setUp() throws Exception {

    asBuiltNotificationEmailService = new AsBuiltNotificationEmailService(notifyService, emailCaseLinkService);

    when(emailCaseLinkService.generateAsBuiltNotificationSummaryLink(any(), any())).thenCallRealMethod();

  }

  @Test
  public void sendAsBuiltNotificationNotPerConsentEmail() {
    asBuiltNotificationEmailService.sendAsBuiltNotificationNotPerConsentEmail(OGA_CONSENTS_EMAIL, "recipientName",
        asBuiltNotificationGroup, pipelineDetail, AS_BUILT_NOTIFICATION_STATUS);
    verify(notifyService).sendEmail(asBuiltNotificationNotPerConsentEmailPropsArgumentCaptor.capture(), eq(OGA_CONSENTS_EMAIL));

    assertThat(asBuiltNotificationNotPerConsentEmailPropsArgumentCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
        "RECIPIENT_FULL_NAME", "recipientName",
        "AS_BUILT_GROUP_REF", asBuiltNotificationGroup.getReference(),
        "PIPELINE_NUMBER", pipelineDetail.getPipelineNumber(),
        "AS_BUILT_NOTIFICATION_STATUS", AS_BUILT_NOTIFICATION_STATUS.getDisplayName(),
        "AS_BUILT_DASHBOARD_LINK", emailCaseLinkService.generateAsBuiltNotificationSummaryLink(
            pipelineDetail.getPipeline().getMasterPwa().getId(), pipelineDetail.getPipelineId().asInt())
    ));
  }
}
