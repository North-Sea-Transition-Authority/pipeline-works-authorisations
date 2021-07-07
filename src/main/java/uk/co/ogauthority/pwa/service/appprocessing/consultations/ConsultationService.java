package uk.co.ogauthority.pwa.service.appprocessing.consultations;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

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
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY);
  }


  /** Task state is either editable or viewable depending on app state and app permissions for oga users.
   * Or it will be always locked for industry users
   */
  public TaskState getTaskState(PwaAppProcessingContext processingContext) {

    boolean atLeastOneSatisfactoryVersion = processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion();
    var permissions = processingContext.getAppProcessingPermissions();
    var appStatusesForViewing = Set.of(
        PwaApplicationStatus.CASE_OFFICER_REVIEW, PwaApplicationStatus.CONSENT_REVIEW, PwaApplicationStatus.COMPLETE);

    var taskState = permissions.contains(PwaAppProcessingPermission.VIEW_ALL_CONSULTATIONS)
        && appStatusesForViewing.contains(processingContext.getApplicationDetail().getStatus())
        && atLeastOneSatisfactoryVersion ? TaskState.VIEW : TaskState.LOCK;

    if (atLeastOneSatisfactoryVersion && permissions.contains(PwaAppProcessingPermission.EDIT_CONSULTATIONS)
        && processingContext.getApplicationDetail().getStatus().equals(PwaApplicationStatus.CASE_OFFICER_REVIEW)) {
      taskState = TaskState.EDIT;
    }

    return taskState;
  }

  public boolean consultationsTaskRequired(PwaApplication pwaApplication) {
    return Set.of(PwaApplicationType.INITIAL,
        PwaApplicationType.CAT_1_VARIATION,
        PwaApplicationType.DECOMMISSIONING,
        PwaApplicationType.OPTIONS_VARIATION,
        PwaApplicationType.DEPOSIT_CONSENT).contains(pwaApplication.getApplicationType());
  }


  public TaskStatus getTaskStatus(PwaApplication pwaApplication) {

    var requests = consultationRequestService.getAllRequestsByApplication(pwaApplication);
    if (requests.isEmpty()) {
      return TaskStatus.NOT_STARTED;
    }

    boolean allRespondedOrWithdrawn = requests.stream()
        .allMatch(r -> COMPLETED_REQUEST_STATUSES.contains(r.getStatus()));

    return allRespondedOrWithdrawn ? TaskStatus.COMPLETED : TaskStatus.IN_PROGRESS;
  }


  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    var taskStatus = getTaskStatus(processingContext.getPwaApplication());
    boolean atLeastOneSatisfactoryVersion = processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion();

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        atLeastOneSatisfactoryVersion ? TaskTag.from(taskStatus) : TaskTag.from(TaskStatus.CANNOT_START_YET),
        getTaskState(processingContext),
        task.getDisplayOrder());
  }

}
