package uk.co.ogauthority.pwa.service.pwaapplications.generic;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;

@Service
public class TaskCompletionService {

  private final ApplicationContext context;

  @Autowired
  public TaskCompletionService(ApplicationContext context) {
    this.context = context;
  }

  /**
   * Return whether or not an application task is considered to be complete for a specific application.
   */
  public boolean isTaskComplete(PwaApplicationDetail detail, ApplicationTask applicationTask) {

    if (applicationTask.getServiceClass() == null) {
      throw new IllegalStateException(String.format("Application task doesn't have service class specified: %s",
          applicationTask.name()));
    }

    var service = context.getBean(applicationTask.getServiceClass());
    return service.isComplete(detail);

  }

  /**
   * Return whether or the task info entries for a task.
   * TODO PWA-95 replace this service with one that just constructs TaskListEntry objects on demand
   */
  public List<TaskInfo> getTaskInfoList(PwaApplicationDetail detail, ApplicationTask applicationTask) {

    if (applicationTask.getServiceClass() == null) {
      throw new IllegalStateException(String.format("Application task doesn't have service class specified: %s",
          applicationTask.name()));
    }

    var service = context.getBean(applicationTask.getServiceClass());
    return service.getTaskInfoList(detail);

  }

}
