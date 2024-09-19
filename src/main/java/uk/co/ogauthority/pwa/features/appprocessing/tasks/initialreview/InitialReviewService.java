package uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview;

import static uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.InitialReviewPaymentDecision.PAYMENT_WAIVED;

import jakarta.transaction.Transactional;
import java.util.EnumSet;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appcharges.ApplicationChargeRequestSpecification;
import uk.co.ogauthority.pwa.features.appprocessing.processingcharges.appfees.ApplicationFeeService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;

/**
 * Service to provide actions available to users at the 'Initial review' stage after submission.
 */
@Service
public class InitialReviewService implements AppProcessingService {

  private final PwaApplicationDetailService applicationDetailService;
  private final CamundaWorkflowService workflowService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;
  private final ApplicationChargeRequestService applicationChargeRequestService;
  private final ApplicationFeeService applicationFeeService;
  private final AssignCaseOfficerService assignCaseOfficerService;
  private final PadInitialReviewService padInitialReviewService;

  @Autowired
  public InitialReviewService(PwaApplicationDetailService applicationDetailService,
                              CamundaWorkflowService workflowService,
                              ApplicationUpdateRequestService applicationUpdateRequestService,
                              ApplicationChargeRequestService applicationChargeRequestService,
                              ApplicationFeeService applicationFeeService,
                              AssignCaseOfficerService assignCaseOfficerService,
                              PadInitialReviewService padInitialReviewService) {
    this.applicationDetailService = applicationDetailService;
    this.workflowService = workflowService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.applicationFeeService = applicationFeeService;
    this.assignCaseOfficerService = assignCaseOfficerService;
    this.padInitialReviewService = padInitialReviewService;
  }

  @Transactional
  public void acceptApplication(PwaApplicationDetail detail,
                                PersonId caseOfficerPersonId,
                                InitialReviewPaymentDecision initialReviewPaymentDecision,
                                String paymentWaivedReason,
                                AuthenticatedUserAccount acceptingUser) {

    if (!detail.getStatus().equals(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)) {
      throw new ActionAlreadyPerformedException(
          String.format("Action: acceptApplication for app detail with ID: %s", detail.getId()));
    }

    var appFeeReport = applicationFeeService.getApplicationFeeReport(detail);

    var appChargeSpec = new ApplicationChargeRequestSpecification(
        detail.getPwaApplication(),
        initialReviewPaymentDecision.getPwaAppChargeRequestStatus()
    )
        .setChargeSummary(appFeeReport.getSummary())
        .setTotalPennies(appFeeReport.getTotalPennies())
        .setChargeWaivedReason(paymentWaivedReason)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPersonId);

    appFeeReport.getPaymentItems().forEach(
        applicationFeeItem -> appChargeSpec.addChargeItem(applicationFeeItem.getDescription(), applicationFeeItem.getPennyAmount())
    );

    applicationChargeRequestService.createPwaAppChargeRequest(
        acceptingUser.getLinkedPerson(),
        appChargeSpec
    );

    padInitialReviewService.addApprovedInitialReview(detail, acceptingUser);
    applicationDetailService.setInitialReviewApproved(detail, acceptingUser, initialReviewPaymentDecision);
    workflowService.setWorkflowProperty(detail.getPwaApplication(), initialReviewPaymentDecision.getPwaApplicationInitialReviewResult());
    workflowService.completeTask(new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.APPLICATION_REVIEW));

    // If payment waived, assume workflow has migrated to the correct state and try to assign immediately.
    if (initialReviewPaymentDecision.equals(PAYMENT_WAIVED)) {
      assignCaseOfficerService.assignCaseOfficer(detail, caseOfficerPersonId, acceptingUser);
    }

  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    var showPermissions = EnumSet.of(
        PwaAppProcessingPermission.CASE_MANAGEMENT_OGA,
        PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY
    );
    return !SetUtils.intersection(showPermissions, processingContext.getAppProcessingPermissions()).isEmpty();
  }

  private boolean taskAccessible(PwaAppProcessingContext processingContext, TaskStatus taskStatus) {

    boolean openUpdateRequest = applicationUpdateRequestService.applicationHasOpenUpdateRequest(processingContext.getApplicationDetail());

    return !openUpdateRequest && (
        !TaskStatus.COMPLETED.equals(taskStatus)
        && processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW)
        && processingContext.getApplicationDetail().getStatus().equals(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)
      );
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    boolean initialReviewCompleted = padInitialReviewService.isInitialReviewComplete(processingContext.getPwaApplication());

    var taskStatus = initialReviewCompleted ? TaskStatus.COMPLETED : TaskStatus.NOT_STARTED;

    var taskState = taskAccessible(processingContext, taskStatus)
        ? TaskState.EDIT
        : TaskState.LOCK;

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        TaskTag.from(taskStatus),
        taskState,
        task.getDisplayOrder());

  }

}
