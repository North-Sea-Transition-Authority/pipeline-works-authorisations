package uk.co.ogauthority.pwa.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroup;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupTestUtil;
import uk.co.ogauthority.pwa.model.enums.aabuilt.AsBuiltNotificationStatus;
import uk.co.ogauthority.pwa.model.notify.emailproperties.asbuilt.AsBuiltNotificationNotPerConsentEmailProps;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;

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

  private static final String OGA_CONSENTS_EMAIL = "consents@ogauthority.co.uk";
  private static final String PIPELINE_NUMBER = "PL8";
  private static final AsBuiltNotificationStatus AS_BUILT_NOTIFICATION_STATUS = AsBuiltNotificationStatus.NOT_PER_CONSENT;

  @Before
  public void setUp() throws Exception {

    asBuiltNotificationEmailService = new AsBuiltNotificationEmailService(notifyService, emailCaseLinkService);

    when(emailCaseLinkService.generateAsBuiltNotificationDashboardLink(any())).thenCallRealMethod();

  }

  @Test
  public void sendConsentReviewReturnedEmail() {
    asBuiltNotificationEmailService.sendAsBuiltNotificationNotPerConsentEmail(OGA_CONSENTS_EMAIL, "recipientName",
        asBuiltNotificationGroup, PIPELINE_NUMBER, AS_BUILT_NOTIFICATION_STATUS);
    verify(notifyService).sendEmail(asBuiltNotificationNotPerConsentEmailPropsArgumentCaptor.capture(), eq(OGA_CONSENTS_EMAIL));

    assertThat(asBuiltNotificationNotPerConsentEmailPropsArgumentCaptor.getValue().getEmailPersonalisation()).containsAllEntriesOf(Map.of(
        "RECIPIENT_FULL_NAME", "recipientName",
        "AS_BUILT_GROUP_REF", asBuiltNotificationGroup.getReference(),
        "PIPELINE_NUMBER", PIPELINE_NUMBER,
        "AS_BUILT_NOTIFICATION_STATUS", AS_BUILT_NOTIFICATION_STATUS.getDisplayName(),
        "AS_BUILT_DASHBOARD_LINK", emailCaseLinkService.generateAsBuiltNotificationDashboardLink(asBuiltNotificationGroup.getId())
    ));
  }
}
