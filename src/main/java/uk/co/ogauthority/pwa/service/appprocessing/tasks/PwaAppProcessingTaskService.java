package uk.co.ogauthority.pwa.service.appprocessing.tasks;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;

@Service
public class PwaAppProcessingTaskService {

  public final ApplicationContext applicationContext;

  @Autowired
  public PwaAppProcessingTaskService(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /**
   * Helper which asks Spring to provide the task service if its available.
   */
  private AppProcessingService getTaskService(PwaAppProcessingTask pwaAppProcessingTask) {

    if (pwaAppProcessingTask.getServiceClass() == null) {
      throw new IllegalStateException(String.format("Processing task doesn't have service class specified: %s",
          pwaAppProcessingTask.toString()));
    }

    return applicationContext.getBean(pwaAppProcessingTask.getServiceClass());

  }

  public boolean canShowTask(PwaAppProcessingTask processingTask, PwaAppProcessingContext processingContext) {
    return getTaskService(processingTask).canShowInTaskList(processingContext);
  }

  Optional<TaskStatus> getTaskStatus(PwaAppProcessingTask processingTask, PwaAppProcessingContext processingContext) {
    return getTaskService(processingTask).getTaskStatus(processingContext);
  }

}
