package uk.co.ogauthority.pwa.service.appprocessing.options;

import static uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission.APPROVE_OPTIONS;
import static uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW;
import static uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission.SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.model.enums.tasklist.TaskState;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApplicationApprovalRepository;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationResponseService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;

@Service
public class ApproveOptionsTaskService implements AppProcessingService {

  private final ConsultationRequestService consultationRequestService;

  private final OptionsApplicationApprovalRepository optionsApplicationApprovalRepository;

  private final ApplicationUpdateRequestService applicationUpdateRequestService;

  private final ConsultationResponseService consultationResponseService;

  @Autowired
  public ApproveOptionsTaskService(ConsultationRequestService consultationRequestService,
                                   OptionsApplicationApprovalRepository optionsApplicationApprovalRepository,
                                   ApplicationUpdateRequestService applicationUpdateRequestService,
                                   ConsultationResponseService consultationResponseService) {
    this.consultationRequestService = consultationRequestService;
    this.optionsApplicationApprovalRepository = optionsApplicationApprovalRepository;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.consultationResponseService = consultationResponseService;
  }


  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext pwaAppProcessingContext) {
    return hasEditAccessPermissions(pwaAppProcessingContext) || hasViewAccessPermissions(pwaAppProcessingContext);
  }

  private boolean hasEditAccessPermissions(PwaAppProcessingContext pwaAppProcessingContext) {
    return pwaAppProcessingContext.getAppProcessingPermissions().contains(APPROVE_OPTIONS);
  }

  private boolean hasViewAccessPermissions(PwaAppProcessingContext pwaAppProcessingContext) {
    return pwaAppProcessingContext.getAppProcessingPermissions().contains(APPROVE_OPTIONS_VIEW)
        || (pwaAppProcessingContext.getApplicationType().equals(PwaApplicationType.OPTIONS_VARIATION)
        && pwaAppProcessingContext.getAppProcessingPermissions().contains(SHOW_ALL_TASKS_AS_PWA_MANAGER_ONLY));
  }

  public boolean taskAccessible(PwaAppProcessingContext pwaAppProcessingContext) {
    var hasEditAccessPermission = hasEditAccessPermissions(pwaAppProcessingContext);

    // dont need to query consultations if we know we dont have basic permission
    if (!hasEditAccessPermission) {
      return false;
    }

    var appStatusCountView = consultationRequestService.getApplicationConsultationStatusView(
        pwaAppProcessingContext.getPwaApplication()
    );

    var openCount = appStatusCountView.sumFilteredStatusCounts(ConsultationRequestStatus::isRequestOpen);
    var respondedCount = appStatusCountView.getCountOfRequestsWithStatus(ConsultationRequestStatus.RESPONDED);

    var updateInProgress = applicationUpdateRequestService.applicationHasOpenUpdateRequest(
        pwaAppProcessingContext.getApplicationDetail()
    );

    var consultationResponsesValid = consultationResponseService.areConsultationResponsesValidForOptionsApproval(
        pwaAppProcessingContext.getPwaApplication());

    return openCount == 0 && respondedCount > 0 && !updateInProgress && consultationResponsesValid;

  }


  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    var optionsApproved = optionsApplicationApprovalRepository.findByPwaApplication(
        processingContext.getPwaApplication())
        .isPresent();

    if (!hasEditAccessPermissions(processingContext) && hasViewAccessPermissions(processingContext)) {
      return getViewAccessTaskListEntry(task, optionsApproved);
    }

    return getEditAccessTaskListEntry(task, processingContext, optionsApproved);

  }


  private TaskListEntry getViewAccessTaskListEntry(PwaAppProcessingTask task,
                                                   boolean optionsApproved) {

    TaskStatus taskStatus;
    if (optionsApproved) {
      taskStatus = TaskStatus.COMPLETED;
    } else {
      taskStatus = TaskStatus.NOT_STARTED;
    }

    return new TaskListEntry(
        task.getTaskName(),
        null,
        TaskTag.from(taskStatus),
        TaskState.LOCK,
        task.getDisplayOrder());
  }

  private TaskListEntry getEditAccessTaskListEntry(PwaAppProcessingTask task,
                                                   PwaAppProcessingContext processingContext,
                                                   boolean optionsApproved) {

    var taskAccessible = taskAccessible(processingContext);

    TaskStatus taskStatus;

    if (optionsApproved) {
      taskStatus = TaskStatus.COMPLETED;
    } else if (taskAccessible) {
      taskStatus = TaskStatus.NOT_STARTED;
    } else {
      taskStatus = TaskStatus.CANNOT_START_YET;
    }

    boolean atLeastOneSatisfactoryVersion = processingContext.getApplicationInvolvement().hasAtLeastOneSatisfactoryVersion();

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        atLeastOneSatisfactoryVersion ? TaskTag.from(taskStatus) : TaskTag.from(TaskStatus.CANNOT_START_YET),
        atLeastOneSatisfactoryVersion && taskStatus.equals(TaskStatus.NOT_STARTED) && taskAccessible ? TaskState.EDIT : TaskState.LOCK,
        task.getDisplayOrder());
  }

}
