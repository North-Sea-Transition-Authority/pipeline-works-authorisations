package uk.co.ogauthority.pwa.service.appprocessing.initialreview;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appcharges.ApplicationChargeRequestSpecification;
import uk.co.ogauthority.pwa.service.appprocessing.processingcharges.appfees.ApplicationFeeService;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.consultations.AssignCaseOfficerService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

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

  @Autowired
  public InitialReviewService(PwaApplicationDetailService applicationDetailService,
                              CamundaWorkflowService workflowService,
                              ApplicationUpdateRequestService applicationUpdateRequestService,
                              ApplicationChargeRequestService applicationChargeRequestService,
                              ApplicationFeeService applicationFeeService,
                              AssignCaseOfficerService assignCaseOfficerService) {
    this.applicationDetailService = applicationDetailService;
    this.workflowService = workflowService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
    this.applicationChargeRequestService = applicationChargeRequestService;
    this.applicationFeeService = applicationFeeService;
    this.assignCaseOfficerService = assignCaseOfficerService;
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
        .setChargeSummary(appFeeReport.getFeeSummary())
        .setTotalPennies(appFeeReport.getTotalPennies())
        .setChargeWaivedReason(paymentWaivedReason)
        .setOnPaymentCompleteCaseOfficerPersonId(caseOfficerPersonId);

    appFeeReport.getApplicationFeeItems().forEach(
        applicationFeeItem -> appChargeSpec.addChargeItem(applicationFeeItem.getDescription(), applicationFeeItem.getPennyAmount())
    );

    applicationChargeRequestService.createPwaAppChargeRequest(
        acceptingUser.getLinkedPerson(),
        appChargeSpec
    );

    applicationDetailService.setInitialReviewApproved(detail, acceptingUser, initialReviewPaymentDecision);
    workflowService.setWorkflowProperty(detail.getPwaApplication(), initialReviewPaymentDecision.getPwaApplicationInitialReviewResult());
    workflowService.completeTask(new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.APPLICATION_REVIEW));

    // If payment waived, assume workflow has migrated to the correct state and try to assign immediately.
    if (initialReviewPaymentDecision.equals(InitialReviewPaymentDecision.PAYMENT_WAIVED)) {
      assignCaseOfficerService.assignCaseOfficer(caseOfficerPersonId, detail, acceptingUser);
    }

  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW)
        || processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    boolean initialReviewCompleted = applicationDetailService
        .getAllSubmittedApplicationDetailsForApplication(processingContext.getPwaApplication())
        .stream()
        .anyMatch(d -> d.getInitialReviewApprovedTimestamp() != null);

    var taskStatus = initialReviewCompleted ? TaskStatus.COMPLETED : TaskStatus.NOT_COMPLETED;

    boolean openUpdateRequest = applicationUpdateRequestService.applicationHasOpenUpdateRequest(processingContext.getApplicationDetail());

    return new TaskListEntry(
        task.getTaskName(),
        !openUpdateRequest ? task.getRoute(processingContext) : null,
        TaskTag.from(taskStatus),
        task.getDisplayOrder());

  }

}
