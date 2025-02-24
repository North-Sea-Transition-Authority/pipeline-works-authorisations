package uk.co.ogauthority.pwa.service.asbuilt.jobs;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import uk.co.ogauthority.pwa.service.asbuilt.AsBuiltInteractorService;

@ExtendWith(MockitoExtension.class)
class AsBuiltNotificationDeadlineReminderJobTest {

  @Mock
  private AsBuiltInteractorService asBuiltInteractorService;

  @Mock
  private JobExecutionContext jobExecutionContext;

  private AsBuiltNotificationDeadlineReminderJob asBuiltNotificationDeadlineReminderJob;

  @BeforeEach
  void setup() {
    asBuiltNotificationDeadlineReminderJob = new AsBuiltNotificationDeadlineReminderJob(asBuiltInteractorService);
  }

  @Test
  void executeInternal_callsInteractorService() throws JobExecutionException {
    asBuiltNotificationDeadlineReminderJob.executeInternal(jobExecutionContext);
    verify(asBuiltInteractorService).notifyHoldersOfAsBuiltGroupDeadlines();
  }

}
