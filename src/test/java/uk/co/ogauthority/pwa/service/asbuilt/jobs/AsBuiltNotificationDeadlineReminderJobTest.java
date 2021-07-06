package uk.co.ogauthority.pwa.service.asbuilt.jobs;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;

@RunWith(MockitoJUnitRunner.class)
public class AsBuiltNotificationDeadlineReminderJobTest {

  @Mock
  private AsBuiltInteractorService asBuiltInteractorService;

  @Mock
  private JobExecutionContext jobExecutionContext;

  private AsBuiltNotificationDeadlineReminderJob asBuiltNotificationDeadlineReminderJob;

  @Before
  public void setup() {
    asBuiltNotificationDeadlineReminderJob = new AsBuiltNotificationDeadlineReminderJob(asBuiltInteractorService);
  }

  @Test
  public void executeInternal_callsInteractorService() throws JobExecutionException {
    asBuiltNotificationDeadlineReminderJob.executeInternal(jobExecutionContext);
    verify(asBuiltInteractorService).notifyHoldersOfAsBuiltGroupDeadlines();
  }

}
