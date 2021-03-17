package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.jobs;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.model.entity.appprocessing.processingcharges.PwaAppChargePaymentAttempt;
import uk.co.ogauthority.pwa.pwapay.PaymentRequestStatus;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.users.UserAccountService;

@Component
public class PaymentAttemptCleanupBean extends QuartzJobBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentAttemptCleanupBean.class);
  private static final String LOGGER_JOB_STRING = "Payment attempt cleanup job";

  private final ApplicationChargeRequestService applicationChargeRequestService;
  private final UserAccountService userAccountService;
  private final Clock clock;
  private final int attemptsCreatedMinsBefore;
  private final int attemptsWarningThreshold;

  @Autowired
  public PaymentAttemptCleanupBean(ApplicationChargeRequestService applicationChargeRequestService,
                                   UserAccountService userAccountService,
                                   @Qualifier("utcClock") Clock clock,
                                   @Value("${pwa.app-charges.cleanup-after-minutes}") int attemptsCreatedMinsBefore,
                                   @Value("${pwa.app-charges.cleanup-attempts-warning-threshold}") int warningThreshold) {
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.userAccountService = userAccountService;
    this.clock = clock;
    this.attemptsCreatedMinsBefore = attemptsCreatedMinsBefore;
    this.attemptsWarningThreshold = warningThreshold;

  }

  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {
      LOGGER.info("Executing {} ... [isRecovering = {}]", LOGGER_JOB_STRING, context.isRecovering());

      // govuk pay times out journeys at 90 minutes.
      // we want to capture unfinished attempts that were started more than 90 mins ago and determine how they ended.
      var attemptCreatedBeforeInstant = clock.instant().minus(attemptsCreatedMinsBefore, ChronoUnit.MINUTES);
      var paymentAttemptsToCleanup = applicationChargeRequestService.getActiveAttemptsWhereStatusIsAndStartedBefore(
          PaymentRequestStatus.IN_PROGRESS, attemptCreatedBeforeInstant
      );

      if (paymentAttemptsToCleanup.size() >= attemptsWarningThreshold) {
        LOGGER.warn(
            "Found {} payment attempts to cleanup and this exceeds warning threshold of {}",
            paymentAttemptsToCleanup.size(),
            attemptsWarningThreshold);
      } else {
        LOGGER.info("Found {} payment attempts to cleanup", paymentAttemptsToCleanup.size());
      }

      if (paymentAttemptsToCleanup.isEmpty()) {
        LOGGER.info("Execution complete for {}", LOGGER_JOB_STRING);
        return;
      }

      // some functionality relies of hibernate envers, and that uses the security context to associate changes to users.
      // we need to manually set the default system user as the security context to account for this.
      var systemWua = userAccountService.getSystemWebUserAccount();
      Authentication auth = new UsernamePasswordAuthenticationToken(systemWua, null);
      SecurityContextHolder.getContext().setAuthentication(auth);

      for (PwaAppChargePaymentAttempt paymentAttemptToCleanup : paymentAttemptsToCleanup) {
        LOGGER.debug("Processing attempt id:{}", paymentAttemptToCleanup.getId());
        var processingResult = applicationChargeRequestService.processPaymentAttempt(paymentAttemptToCleanup,
            systemWua);
        LOGGER.debug("Processing attempt id:{} result:{}", paymentAttemptToCleanup.getId(), processingResult);
      }
      LOGGER.info("Job execution complete for {}", LOGGER_JOB_STRING);
    } catch (Exception e) {
      LOGGER.error(" Payment attempt cleanup job execution failed", e);
      throw new JobExecutionException(e);
    }
  }
}
