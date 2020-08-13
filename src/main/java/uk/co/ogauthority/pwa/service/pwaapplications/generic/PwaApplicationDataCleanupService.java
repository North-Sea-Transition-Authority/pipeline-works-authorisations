package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

@Service
public class PwaApplicationDataCleanupService {

  private final TaskListService taskListService;
  private final ApplicationContext applicationContext;

  @Autowired
  public PwaApplicationDataCleanupService(TaskListService taskListService,
                                          ApplicationContext applicationContext) {
    this.taskListService = taskListService;
    this.applicationContext = applicationContext;
  }

  /**
   * For each task that is relevant for the application, clean up hidden and unnecessary data.
   * @param detail of the application we're cleaning data for
   */
  @Transactional
  public void cleanupData(PwaApplicationDetail detail) {

    taskListService.getShownApplicationTasksForDetail(detail)
        .forEach(applicationTask -> applicationContext.getBean(applicationTask.getServiceClass()).cleanupData(detail));

  }

}
