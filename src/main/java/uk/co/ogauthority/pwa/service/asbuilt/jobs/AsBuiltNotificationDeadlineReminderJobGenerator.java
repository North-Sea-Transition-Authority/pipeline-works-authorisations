package uk.co.ogauthority.pwa.service.asbuilt.jobs;

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

@Service
public class AsBuiltNotificationDeadlineReminderJobGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsBuiltNotificationDeadlineReminderJobGenerator.class);
  private static final JobKey JOB_KEY = jobKey("DAILY_AS_BUILT_DEADLINE_REMINDER_JOB", "AS_BUILT_DEADLINE_REMINDER_JOBS");
  private static final TriggerKey TRIGGER_KEY = triggerKey("DAILY_AS_BUILT_DEADLINE_REMINDER_TRIGGER_KEY",
      "AS_BUILT_DEADLINE_REMINDER_TRIGGERS");

  private final Scheduler scheduler;

  @Autowired
  public AsBuiltNotificationDeadlineReminderJobGenerator(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void registerJob() throws SchedulerException {
    if (scheduler.getJobDetail(JOB_KEY) != null) {
      LOGGER.info("Daily as-built deadline reminder job found");
    } else {
      LOGGER.info("Daily as-built deadline reminder job does not exist. Creating...");
      JobDetail jobDetail = newJob(AsBuiltNotificationDeadlineReminderJob.class)
          .withIdentity(JOB_KEY)
          .requestRecovery()
          .storeDurably()
          .build();

      Trigger trigger = TriggerBuilder
          .newTrigger()
          .withIdentity(TRIGGER_KEY)
          // start job at 5am of the current day
          .startAt(DateBuilder.todayAt(5,0,0))
          .withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule().withIntervalInDays(1))
          .build();

      scheduler.scheduleJob(jobDetail, trigger);

      LOGGER.info("Daily as-built deadline reminder job creation complete");
    }
  }

}
