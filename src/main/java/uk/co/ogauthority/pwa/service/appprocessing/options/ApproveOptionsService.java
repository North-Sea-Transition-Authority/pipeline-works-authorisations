package uk.co.ogauthority.pwa.service.appprocessing.options;

import static uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission.APPROVE_OPTIONS;
import static uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission.APPROVE_OPTIONS_VIEW;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.repository.appprocessing.options.OptionsApplicationApprovalRepository;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.ConsultationRequestService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;

@Service
public class ApproveOptionsService implements AppProcessingService {

  private final ConsultationRequestService consultationRequestService;

  private final OptionsApprovalPersister optionsApprovalPersister;

  private final OptionsApplicationApprovalRepository optionsApplicationApprovalRepository;

  private final OptionsCaseManagementEmailService optionsCaseManagementEmailService;

  private final ApplicationUpdateRequestService applicationUpdateRequestService;

  @Autowired
  public ApproveOptionsService(ConsultationRequestService consultationRequestService,
                               OptionsApprovalPersister optionsApprovalPersister,
                               OptionsApplicationApprovalRepository optionsApplicationApprovalRepository,
                               OptionsCaseManagementEmailService optionsCaseManagementEmailService,
                               ApplicationUpdateRequestService applicationUpdateRequestService) {
    this.consultationRequestService = consultationRequestService;
    this.optionsApprovalPersister = optionsApprovalPersister;
    this.optionsApplicationApprovalRepository = optionsApplicationApprovalRepository;
    this.optionsCaseManagementEmailService = optionsCaseManagementEmailService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext pwaAppProcessingContext) {
    return hasEditAccessPermissions(pwaAppProcessingContext) || hasViewAccessPermissions(pwaAppProcessingContext);
  }

  private boolean hasEditAccessPermissions(PwaAppProcessingContext pwaAppProcessingContext) {
    return pwaAppProcessingContext.getAppProcessingPermissions().contains(APPROVE_OPTIONS);
  }

  private boolean hasViewAccessPermissions(PwaAppProcessingContext pwaAppProcessingContext) {
    return pwaAppProcessingContext.getAppProcessingPermissions().contains(APPROVE_OPTIONS_VIEW);
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

    var updateInProgress = applicationUpdateRequestService.applicationDetailHasOpenUpdateRequest(
        pwaAppProcessingContext.getApplicationDetail()
    );

    return openCount == 0 && respondedCount > 0 && !updateInProgress;

  }

  @Transactional
  public void approveOptions(PwaApplicationDetail pwaApplicationDetail, Person approverPerson, Instant deadlineDate) {

    var initialApprovalDeadlineHistory = optionsApprovalPersister.createInitialOptionsApproval(
        pwaApplicationDetail.getPwaApplication(),
        approverPerson,
        deadlineDate
    );

    optionsCaseManagementEmailService.sendInitialOptionsApprovedEmail(pwaApplicationDetail);

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
      taskStatus = TaskStatus.NOT_COMPLETED;
    }

    return new TaskListEntry(
        task.getTaskName(),
        null,
        TaskTag.from(taskStatus),
        task.getDisplayOrder());
  }

  private TaskListEntry getEditAccessTaskListEntry(PwaAppProcessingTask task,
                                                   PwaAppProcessingContext processingContext,
                                                   boolean optionsApproved) {

    var taskAccessible = taskAccessible(processingContext);

    TaskStatus taskStatus;

    if (optionsApproved) {
      taskStatus = TaskStatus.COMPLETED;
    } else if (taskAccessible && !optionsApproved) {
      taskStatus = TaskStatus.NOT_COMPLETED;
    } else {
      taskStatus = TaskStatus.CANNOT_START_YET;
    }

    String route;
    if (taskStatus.equals(TaskStatus.NOT_COMPLETED)) {
      route = taskAccessible ? task.getRoute(processingContext) : null;
    } else {
      route = null;
    }

    return new TaskListEntry(
        task.getTaskName(),
        route,
        TaskTag.from(taskStatus),
        task.getDisplayOrder());
  }
}
