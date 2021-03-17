package uk.co.ogauthority.pwa.service.appprocessing.processingcharges.jobs;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerKey.triggerKey;

import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppPaymentAttemptCleanupJobGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(PaymentAttemptCleanupBean.class);
  private static final JobKey JOB_KEY = jobKey("HOURLY_PAYMENT_ATTEMPT_CLEANUP_JOB", "APPLICATION_CHARGE_JOBS");
  private static final TriggerKey TRIGGER_KEY = triggerKey("HOURLY_PAYMENT_ATTEMPT_CLEANUP_TRIGGER_KEY", "APPLICATION_CHARGE_TRIGGERS");

  private final Scheduler scheduler;

  @Autowired
  public AppPaymentAttemptCleanupJobGenerator(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  @Transactional
  public void registerJob() throws SchedulerException {
    if (scheduler.getJobDetail(JOB_KEY) != null) {
      LOGGER.info("Hourly payment attempt cleanup job found");
    } else {
      LOGGER.info("Hourly payment attempt cleanup job does not exist. Creating...");
      JobDetail jobDetail = newJob(PaymentAttemptCleanupBean.class)
          .withIdentity(JOB_KEY)
          .requestRecovery()
          .storeDurably()
          .build();

      Trigger trigger = TriggerBuilder
          .newTrigger()
          .withIdentity(TRIGGER_KEY)
          // just start the job as soon as possible if it doesnt exist
          .startAt(DateBuilder.evenSecondDateAfterNow())
          .withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule().withIntervalInHours(1))
          .build();

      scheduler.scheduleJob(jobDetail, trigger);

      LOGGER.info("Hourly payment attempt cleanup job creation complete");
    }
  }
}
