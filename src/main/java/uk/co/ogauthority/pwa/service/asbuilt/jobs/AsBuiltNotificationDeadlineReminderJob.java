package uk.co.ogauthority.pwa.service.asbuilt.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;

@Component
public class AsBuiltNotificationDeadlineReminderJob extends QuartzJobBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(AsBuiltNotificationDeadlineReminderJob.class);
  private final AsBuiltInteractorService asBuiltInteractorService;

  @Autowired
  public AsBuiltNotificationDeadlineReminderJob(AsBuiltInteractorService asBuiltInteractorService) {
    this.asBuiltInteractorService = asBuiltInteractorService;
  }

  @Override
  protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    LOGGER.info("Executing as-built notification deadline reminder job...");
    asBuiltInteractorService.notifyHoldersOfAsBuiltGroupDeadlines();
    LOGGER.info("As-built notification deadline reminder job complete.");
  }
}