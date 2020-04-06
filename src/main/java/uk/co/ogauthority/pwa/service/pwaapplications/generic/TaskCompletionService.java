package uk.co.ogauthority.pwa.service.pwaapplications.generic;

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

    var service = (ApplicationFormSectionService) context.getBean(applicationTask.getServiceClass());
    return service.isComplete(detail);

  }

}
