package uk.co.ogauthority.pwa.usercontext;


import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.co.fivium.energyportal.starter.usercontext.EnergyPortalUserServicesContextProvider;
import uk.co.fivium.energyportal.starter.usercontext.UserContextV1;
import uk.co.fivium.energyportal.starter.usercontext.VersionedUserContext;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.service.appprocessing.publicnotice.PublicNoticeService;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltGroupDeadlineService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;

@Component
public class PwaEnergyPortalUserServicesContextProvider implements EnergyPortalUserServicesContextProvider {

  private final PublicNoticeService publicNoticeService;
  private final AsBuiltGroupDeadlineService asBuiltGroupDeadlineService;
  private final ConsultationRequestService consultationRequestService;
  private final ApplicationChargeRequestService applicationChargeRequestService;
  private final ApplicationUpdateRequestService  applicationUpdateRequestService;
  private final UserAccountService userAccountService;

  private final Clock clock;

  @Autowired
  public PwaEnergyPortalUserServicesContextProvider(PublicNoticeService publicNoticeService,
                                                    AsBuiltGroupDeadlineService asBuiltGroupDeadlineService,
                                                    ConsultationRequestService consultationRequestService,
                                                    ApplicationChargeRequestService applicationChargeRequestService,
                                                    ApplicationUpdateRequestService applicationUpdateRequestService,
                                                    UserAccountService userAccountService,
                                                    @Qualifier("utcClock") Clock clock) {
    this.publicNoticeService = publicNoticeService;
    this.asBuiltGroupDeadlineService = asBuiltGroupDeadlineService;
    this.consultationRequestService = consultationRequestService;
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.userAccountService = userAccountService;
    this.clock = clock;
  }

  @Override
  public VersionedUserContext getUserContext(long wuaId) {
    var contextBuilder = VersionedUserContext.newBuilder().v1();

    var webUserAccount = userAccountService.getWebUserAccount(Math.toIntExact(wuaId));
    var targetPersonId = webUserAccount.getLinkedPerson().getId();
    var nowInstant = clock.instant();
    var todayDate = nowInstant.atZone(clock.getZone()).toLocalDate();

    addPublicNoticesContext(contextBuilder, targetPersonId, nowInstant);
    addAsBuiltNotificationsContext(contextBuilder, webUserAccount, todayDate);
    addPaymentRequestContext(contextBuilder, webUserAccount);
    addConsultationsContext(contextBuilder, wuaId, nowInstant);
    addUpdateRequestContext(contextBuilder, webUserAccount, nowInstant);

    return contextBuilder.build();
  }

  private void addPublicNoticesContext(UserContextV1.Builder contextBuilder, PersonId personId, Instant nowInstant) {
    var activeNotices = publicNoticeService.getActiveNoticesForCaseOfficer(personId)
        .stream()
        .filter(n -> n.getPublicationEndTimestamp() != null)
        .toList();

    if (activeNotices.isEmpty()) {
      return;
    }

    var overdueCount = Math.toIntExact(activeNotices.stream()
        .filter(n -> n.getPublicationEndTimestamp().isBefore(nowInstant))
        .count());
    var upcomingCount = activeNotices.size() - overdueCount;

    addOverdueContext(contextBuilder, overdueCount, "public notice");
    addUpcomingContext(contextBuilder, upcomingCount, "public notice", "upcoming");
  }

  private void addAsBuiltNotificationsContext(UserContextV1.Builder contextBuilder, WebUserAccount webUserAccount, LocalDate todayDate) {
    var activeAsBuilts = asBuiltGroupDeadlineService.getActiveAsBuiltNotificationsForUser(webUserAccount);

    if (activeAsBuilts.isEmpty()) {
      return;
    }

    var overdueCount = Math.toIntExact(activeAsBuilts.stream()
        .filter(a -> a.getDeadlineDate() != null && a.getDeadlineDate().isBefore(todayDate))
        .count());
    var upcomingCount = activeAsBuilts.size() - overdueCount;

    addOverdueContext(contextBuilder, overdueCount, "as-built notification");
    addUpcomingContext(contextBuilder, upcomingCount, "as-built notification", "due soon");
  }

  private void addConsultationsContext(UserContextV1.Builder contextBuilder, long wuaId, Instant nowInstant) {
    var activeConsultations = consultationRequestService.getActiveConsultationsForUser(wuaId);

    if (activeConsultations.isEmpty()) {
      return;
    }

    var overdueCount = Math.toIntExact(activeConsultations.stream()
        .filter(c -> c.getDeadlineDate() != null && c.getDeadlineDate().isBefore(nowInstant))
        .count());
    var upcomingCount = activeConsultations.size() - overdueCount;

    addOverdueContext(contextBuilder, overdueCount, "consultation");
    addUpcomingContext(contextBuilder, upcomingCount, "consultation", "upcoming");
  }

  private void addPaymentRequestContext(UserContextV1.Builder contextBuilder, WebUserAccount webUserAccount) {
    var openPaymentRequests = applicationChargeRequestService
        .getOpenChargeRequestsForApplicant(webUserAccount);

    if (openPaymentRequests.isEmpty()) {
      return;
    }
    contextBuilder.high(
        openPaymentRequests.size(),
        "%s awaiting action".formatted(pluralise(openPaymentRequests.size(), "payment request")));
  }

  private void addUpdateRequestContext(UserContextV1.Builder contextBuilder, WebUserAccount webUserAccount, Instant nowInstant) {
    var openUpdateRequests = applicationUpdateRequestService
        .getOpenUpdateRequestsForPreparer(webUserAccount.getLinkedPerson());

    if (openUpdateRequests.isEmpty()) {
      return;
    }

    var overdueCount = Math.toIntExact(openUpdateRequests.stream()
        .filter(r -> r.getDeadlineTimestamp() != null && r.getDeadlineTimestamp().isBefore(nowInstant))
        .count());
    var upcomingCount = openUpdateRequests.size() - overdueCount;

    addOverdueContext(contextBuilder, overdueCount, "update request");
    addUpcomingContext(contextBuilder, upcomingCount, "update request", "upcoming");
  }

  private static void addOverdueContext(UserContextV1.Builder contextBuilder, int overdueCount, String singular) {
    if (overdueCount > 0) {
      contextBuilder.high(overdueCount, "%s overdue".formatted(pluralise(overdueCount, singular)));
    }
  }

  private static void addUpcomingContext(UserContextV1.Builder contextBuilder, int upcomingCount, String singular, String suffix) {
    if (upcomingCount > 0) {
      contextBuilder.low(upcomingCount, "%s %s".formatted(pluralise(upcomingCount, singular), suffix));
    }
  }

  private static String pluralise(int count, String singular) {
    return count == 1 ? singular : singular + "s";
  }
}