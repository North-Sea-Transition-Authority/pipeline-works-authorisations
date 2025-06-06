package uk.co.ogauthority.pwa.validators.appprocessing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;

/**
 * Validation service that checks all tasks in the application are complete.
 */
@Service
public class PwaApplicationValidationService {

  private final TaskListService taskListService;

  @Autowired
  public PwaApplicationValidationService(TaskListService taskListService) {
    this.taskListService = taskListService;
  }

  public boolean isApplicationValid(PwaApplicationDetail pwaApplicationDetail) {
    return taskListService.areAllApplicationTasksComplete(pwaApplicationDetail);
  }

}
