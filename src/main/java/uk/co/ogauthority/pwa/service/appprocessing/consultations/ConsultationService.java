package uk.co.ogauthority.pwa.service.appprocessing.consultations;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

@Service
public class ConsultationService implements AppProcessingService {

  private final ConsultationRequestService consultationRequestService;
  private static final List<ConsultationRequestStatus> COMPLETED_REQUEST_STATUSES = List
      .of(ConsultationRequestStatus.RESPONDED, ConsultationRequestStatus.WITHDRAWN);

  @Autowired
  public ConsultationService(ConsultationRequestService consultationRequestService) {
    this.consultationRequestService = consultationRequestService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    var requests = consultationRequestService.getAllRequestsByApplication(processingContext.getPwaApplication());

    TaskStatus taskStatus;

    if (requests.isEmpty()) {
      taskStatus = TaskStatus.NOT_STARTED;
    } else {

      boolean allRespondedOrWithdrawn = requests.stream()
          .allMatch(r -> COMPLETED_REQUEST_STATUSES.contains(r.getStatus()));

      taskStatus = allRespondedOrWithdrawn ? TaskStatus.COMPLETED : TaskStatus.IN_PROGRESS;

    }

    boolean atLeastOneSatisfactoryVersion = processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion();

    return new TaskListEntry(
        task.getTaskName(),
        atLeastOneSatisfactoryVersion ? task.getRoute(processingContext) : null,
        atLeastOneSatisfactoryVersion ? TaskTag.from(taskStatus) : TaskTag.from(TaskStatus.CANNOT_START_YET),
        task.getDisplayOrder());

  }
}
