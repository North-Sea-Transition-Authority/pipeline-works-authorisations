package uk.co.ogauthority.pwa.features.application.submission;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class PwaApplicationDataCleanupService {

  private final TaskListService taskListService;
  private final ApplicationContext applicationContext;
  private final PadFileService padFileService;

  @Autowired
  public PwaApplicationDataCleanupService(TaskListService taskListService,
                                          ApplicationContext applicationContext,
                                          PadFileService padFileService) {
    this.taskListService = taskListService;
    this.applicationContext = applicationContext;
    this.padFileService = padFileService;
  }

  /**
   * For each task that is relevant for the application, clean up hidden and unnecessary data.
   * @param detail of the application we're cleaning data for
   */
  @Transactional
  public void cleanupData(PwaApplicationDetail detail, WebUserAccount userAccount) {

    taskListService.getShownApplicationTasksForDetail(detail)
        .forEach(applicationTask -> applicationContext.getBean(applicationTask.getServiceClass()).cleanupData(detail));
    padFileService.deleteTemporaryFilesForDetail(detail, userAccount);
  }

}
