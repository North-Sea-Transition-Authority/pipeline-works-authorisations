package uk.co.ogauthority.pwa.service.appprocessing.publicnotice.quartzjobs;

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
public class PublicNoticePublicationUpdateJobGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(PublicNoticePublicationUpdateJobGenerator.class);
  private static final JobKey JOB_KEY = jobKey("DAILY_PUBLICATION_UPDATER_JOB", "PUBLIC_NOTICE_PUBLICATION_JOBS");
  private static final TriggerKey TRIGGER_KEY = triggerKey("DAILY_PUBLICATION_UPDATER_TRIGGER_KEY", "PUBLIC_NOTICE_PUBLICATION_TRIGGERS");

  private final Scheduler scheduler;

  @Autowired
  public PublicNoticePublicationUpdateJobGenerator(Scheduler scheduler) {
    this.scheduler = scheduler;
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void registerJob() throws SchedulerException {
    if (scheduler.getJobDetail(JOB_KEY) != null) {
      LOGGER.info("Daily publication updater job found");
    } else {
      LOGGER.info("Daily publication updater job does not exist. Creating...");
      JobDetail jobDetail = newJob(PublicNoticePublicationUpdaterJob.class)
          .withIdentity(JOB_KEY)
          .requestRecovery()
          .storeDurably()
          .build();

      Trigger trigger = TriggerBuilder
          .newTrigger()
          .withIdentity(TRIGGER_KEY)
          // start job at 1am of the current day
          .startAt(DateBuilder.newDate().atHourOfDay(1).build())
          .withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule().withIntervalInDays(1))
          .build();

      scheduler.scheduleJob(jobDetail, trigger);

      LOGGER.info("Daily publication updater job creation complete");
    }
  }
}
