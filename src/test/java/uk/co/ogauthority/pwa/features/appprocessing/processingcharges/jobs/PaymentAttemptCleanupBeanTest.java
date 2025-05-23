package uk.co.ogauthority.pwa.features.appprocessing.processingcharges.jobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.internal.PwaAppChargePaymentAttemptTestUtil;
import uk.co.ogauthority.pwa.features.pwapay.PaymentRequestStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.UserAccountService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;

@ExtendWith(MockitoExtension.class)
class PaymentAttemptCleanupBeanTest {

  @Mock
  private ApplicationChargeRequestService applicationChargeRequestService;

  @Mock
  private UserAccountService userAccountService;

  @Mock
  private JobExecutionContext jobExecutionContext; // mocking an interface we dont control is generally a bad idea. Not sure how to avoid here.

  private Clock clock = Clock.fixed(
      LocalDateTime.of(2020, 12, 1, 0, 0).toInstant(ZoneOffset.UTC), ZoneId.systemDefault());

  private int attemptsCreatedMinsBefore = 10;
  private int warningThreshold = 20;

  private Person person;
  private WebUserAccount sysWua;

  private PaymentAttemptCleanupBean paymentAttemptCleanupBean;


  @BeforeEach
  void setUp() {
    person = PersonTestUtil.createDefaultPerson();
    sysWua = new WebUserAccount();
    paymentAttemptCleanupBean = new PaymentAttemptCleanupBean(
        applicationChargeRequestService,
        userAccountService,
        clock,
        attemptsCreatedMinsBefore,
        warningThreshold
    );

  }

  @Test
  void executeInternal_whenNoPaymentAttemptsReturned() throws JobExecutionException {

    paymentAttemptCleanupBean.executeInternal(jobExecutionContext);

    var expectedStartedBeforeInstant = clock.instant().minus(attemptsCreatedMinsBefore, ChronoUnit.MINUTES);

    verifyNoInteractions(userAccountService);
    verify(applicationChargeRequestService, times(1))
        .getActiveAttemptsWhereStatusIsAndStartedBefore(PaymentRequestStatus.IN_PROGRESS, expectedStartedBeforeInstant);

    verifyNoMoreInteractions(applicationChargeRequestService);

  }

  @Test
  void executeInternal_whenSinglePaymentAttemptReturned() throws JobExecutionException {

    when(userAccountService.getSystemWebUserAccount()).thenReturn(sysWua);

    var paymentAttempt = PwaAppChargePaymentAttemptTestUtil.createWithPaymentRequest(
        null, PaymentRequestStatus.IN_PROGRESS, person);
    when(applicationChargeRequestService.getActiveAttemptsWhereStatusIsAndStartedBefore(any() ,any()))
        .thenReturn(List.of(paymentAttempt));

    paymentAttemptCleanupBean.executeInternal(jobExecutionContext);

    var expectedStartedBeforeInstant = clock.instant().minus(attemptsCreatedMinsBefore, ChronoUnit.MINUTES);

    verify(userAccountService, times(1)).getSystemWebUserAccount();

    verify(applicationChargeRequestService, times(1))
        .getActiveAttemptsWhereStatusIsAndStartedBefore(PaymentRequestStatus.IN_PROGRESS, expectedStartedBeforeInstant);

    verify(applicationChargeRequestService, times(1)).processPaymentAttempt(paymentAttempt, sysWua);

    verifyNoMoreInteractions(applicationChargeRequestService, userAccountService);

  }
}