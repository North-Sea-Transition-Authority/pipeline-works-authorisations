package uk.co.ogauthority.pwa.usercontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.starter.usercontext.VersionedUserContext;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargeRequest;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccountStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccountTestUtil;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeDate;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltGroupDeadlineService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;

@ExtendWith(MockitoExtension.class)
class PwaEnergyPortalUserServicesContextProviderTest {

  @Mock
  private PublicNoticeService publicNoticeService;

  @Mock
  private AsBuiltGroupDeadlineService asBuiltGroupDeadlineService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private ApplicationChargeRequestService applicationChargeRequestService;

  @Mock
  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Mock
  private UserAccountService userAccountService;

  private WebUserAccount webUserAccount;

  private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
  private static final Instant NOW = CLOCK.instant();
  private static final LocalDate TODAY = LocalDate.now(CLOCK);
  private static final long WUA_ID = 123L;
  private static final int WUA_ID_INT = Math.toIntExact(WUA_ID);
  private static final Person PERSON = PersonTestUtil.createDefaultPerson();

  private PwaEnergyPortalUserServicesContextProvider provider;

  @BeforeEach
  void setUp() {
    provider = new PwaEnergyPortalUserServicesContextProvider(
        publicNoticeService,
        asBuiltGroupDeadlineService,
        consultationRequestService,
        applicationChargeRequestService,
        applicationUpdateRequestService,
        userAccountService,
        CLOCK
    );

    webUserAccount = WebUserAccountTestUtil.createWebUserAccountMatchingPerson(
        WUA_ID_INT,
        PERSON,
        WebUserAccountStatus.ACTIVE
    );

    when(userAccountService.getWebUserAccount(WUA_ID_INT)).thenReturn(webUserAccount);
    when(publicNoticeService.getActiveNoticesForCaseOfficer(any())).thenReturn(Collections.emptyList());
    when(asBuiltGroupDeadlineService.getActiveAsBuiltNotificationsForUser(webUserAccount)).thenReturn(
        Collections.emptyList());
    when(consultationRequestService.getActiveConsultationsForUser(WUA_ID)).thenReturn(Collections.emptyList());
    when(applicationChargeRequestService.getOpenChargeRequestsForApplicant(any())).thenReturn(
        Collections.emptyList());
    when(applicationUpdateRequestService.getOpenUpdateRequestsForPreparer(any())).thenReturn(
        Collections.emptyList());
  }

  @Test
  void getUserContext_whenNoActiveItems_returnsEmptyContext() {
    var expectedContext = VersionedUserContext.newBuilder().v1().build();
    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenOneOverduePublicNotice_returnsHighContext() {
    var notice = new PublicNoticeDate();
    notice.setPublicationEndTimestamp(NOW.minusSeconds(1));
    when(publicNoticeService.getActiveNoticesForCaseOfficer(any())).thenReturn(List.of(notice));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(1, "public notice overdue")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenMultipleOverduePublicNotices_returnsHighContext() {
    var notice1 = new PublicNoticeDate();
    notice1.setPublicationEndTimestamp(NOW.minusSeconds(1));
    var notice2 = new PublicNoticeDate();
    notice2.setPublicationEndTimestamp(NOW.minusSeconds(100));
    when(publicNoticeService.getActiveNoticesForCaseOfficer(any())).thenReturn(List.of(notice1, notice2));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(2, "public notices overdue")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenOneUpcomingPublicNotice_returnsLowContext() {
    var notice = new PublicNoticeDate();
    notice.setPublicationEndTimestamp(NOW.plusSeconds(100));
    when(publicNoticeService.getActiveNoticesForCaseOfficer(any())).thenReturn(List.of(notice));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .low(1, "public notice upcoming")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenMixedPublicNotices_returnsBothContexts() {
    var overdueNotice = new PublicNoticeDate();
    overdueNotice.setPublicationEndTimestamp(NOW.minusSeconds(1));
    var upcomingNotice = new PublicNoticeDate();
    upcomingNotice.setPublicationEndTimestamp(NOW.plusSeconds(100));
    when(publicNoticeService.getActiveNoticesForCaseOfficer(any())).thenReturn(List.of(overdueNotice, upcomingNotice));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(1, "public notice overdue")
        .low(1, "public notice upcoming")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenOneOpenPaymentRequest_returnsHighContext() {
    when(applicationChargeRequestService.getOpenChargeRequestsForApplicant(any()))
        .thenReturn(List.of(new PwaAppChargeRequest()));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(1, "payment request awaiting action")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenMultipleOpenPaymentRequests_returnsHighContext() {
    when(applicationChargeRequestService.getOpenChargeRequestsForApplicant(any()))
        .thenReturn(List.of(new PwaAppChargeRequest(), new PwaAppChargeRequest()));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(2, "payment requests awaiting action")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenOneOverdueUpdateRequest_returnsHighContext() {
    var updateRequest = new ApplicationUpdateRequest();
    updateRequest.setDeadlineTimestamp(NOW.minusSeconds(1));
    when(applicationUpdateRequestService.getOpenUpdateRequestsForPreparer(any()))
        .thenReturn(List.of(updateRequest));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(1, "update request overdue")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenMultipleOverdueUpdateRequests_returnsHighContext() {
    var updateRequest1 = new ApplicationUpdateRequest();
    updateRequest1.setDeadlineTimestamp(NOW.minusSeconds(1));
    var updateRequest2 = new ApplicationUpdateRequest();
    updateRequest2.setDeadlineTimestamp(NOW.minusSeconds(100));
    when(applicationUpdateRequestService.getOpenUpdateRequestsForPreparer(any()))
        .thenReturn(List.of(updateRequest1, updateRequest2));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(2, "update requests overdue")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenOneUpcomingUpdateRequest_returnsLowContext() {
    var updateRequest = new ApplicationUpdateRequest();
    updateRequest.setDeadlineTimestamp(NOW.plusSeconds(100));
    when(applicationUpdateRequestService.getOpenUpdateRequestsForPreparer(any()))
        .thenReturn(List.of(updateRequest));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .low(1, "update request upcoming")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenMixedUpdateRequests_returnsBothContexts() {
    var overdueRequest = new ApplicationUpdateRequest();
    overdueRequest.setDeadlineTimestamp(NOW.minusSeconds(1));
    var upcomingRequest = new ApplicationUpdateRequest();
    upcomingRequest.setDeadlineTimestamp(NOW.plusSeconds(100));
    when(applicationUpdateRequestService.getOpenUpdateRequestsForPreparer(any()))
        .thenReturn(List.of(overdueRequest, upcomingRequest));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(1, "update request overdue")
        .low(1, "update request upcoming")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenOneOverdueAsBuiltNotification_returnsHighContext() {
    var detail = new AsBuiltNotificationGroupDetail();
    detail.setDeadlineDate(TODAY.minusDays(1));
    when(asBuiltGroupDeadlineService.getActiveAsBuiltNotificationsForUser(webUserAccount))
        .thenReturn(List.of(detail));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(1, "as-built notification overdue")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenOneUpcomingAsBuiltNotification_returnsLowContext() {
    var detail = new AsBuiltNotificationGroupDetail();
    detail.setDeadlineDate(TODAY.plusDays(1));
    when(asBuiltGroupDeadlineService.getActiveAsBuiltNotificationsForUser(webUserAccount))
        .thenReturn(List.of(detail));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .low(1, "as-built notification due soon")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenOneOverdueConsultation_returnsHighContext() {
    var consultation = new ConsultationRequest();
    consultation.setDeadlineDate(NOW.minusSeconds(1));
    when(consultationRequestService.getActiveConsultationsForUser(WUA_ID))
        .thenReturn(List.of(consultation));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(1, "consultation overdue")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenOneUpcomingConsultation_returnsLowContext() {
    var consultation = new ConsultationRequest();
    consultation.setDeadlineDate(NOW.plusSeconds(100));
    when(consultationRequestService.getActiveConsultationsForUser(WUA_ID))
        .thenReturn(List.of(consultation));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .low(1, "consultation upcoming")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }

  @Test
  void getUserContext_whenMixedConsultations_returnsBothContexts() {
    var overdueConsultation = new ConsultationRequest();
    overdueConsultation.setDeadlineDate(NOW.minusSeconds(1));
    var upcomingConsultation = new ConsultationRequest();
    upcomingConsultation.setDeadlineDate(NOW.plusSeconds(100));
    when(consultationRequestService.getActiveConsultationsForUser(WUA_ID))
        .thenReturn(List.of(overdueConsultation, upcomingConsultation));

    var expectedContext = VersionedUserContext.newBuilder().v1()
        .high(1, "consultation overdue")
        .low(1, "consultation upcoming")
        .build();

    assertThat(provider.getUserContext(WUA_ID)).isEqualTo(expectedContext);
  }
}