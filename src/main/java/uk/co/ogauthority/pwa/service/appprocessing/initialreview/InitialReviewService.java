package uk.co.ogauthority.pwa.service.appprocessing.initialreview;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.CaseOfficerAssignedEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.applicationupdate.ApplicationUpdateRequestService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/**
 * Service to provide actions available to users at the 'Initial review' stage after submission.
 */
@Service
public class InitialReviewService implements AppProcessingService {

  private final PwaApplicationDetailService applicationDetailService;
  private final CamundaWorkflowService workflowService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final TeamManagementService teamManagementService;
  private final NotifyService notifyService;
  private final PersonService personService;
  private final ApplicationUpdateRequestService applicationUpdateRequestService;

  @Autowired
  public InitialReviewService(PwaApplicationDetailService applicationDetailService,
                              CamundaWorkflowService workflowService,
                              WorkflowAssignmentService workflowAssignmentService,
                              TeamManagementService teamManagementService,
                              NotifyService notifyService,
                              PersonService personService,
                              ApplicationUpdateRequestService applicationUpdateRequestService) {
    this.applicationDetailService = applicationDetailService;
    this.workflowService = workflowService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.teamManagementService = teamManagementService;
    this.notifyService = notifyService;
    this.personService = personService;
    this.applicationUpdateRequestService = applicationUpdateRequestService;
  }

  @Transactional
  public void acceptApplication(PwaApplicationDetail detail,
                                Integer caseOfficerPersonId,
                                WebUserAccount acceptingUser) {

    if (!detail.getStatus().equals(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)) {
      throw new ActionAlreadyPerformedException(
          String.format("Action: acceptApplication for app detail with ID: %s", detail.getId()));
    }

    applicationDetailService.setInitialReviewApproved(detail, acceptingUser);
    workflowService.completeTask(new WorkflowTaskInstance(detail.getPwaApplication(), PwaApplicationWorkflowTask.APPLICATION_REVIEW));

    var caseOfficer = teamManagementService.getPerson(caseOfficerPersonId);

    workflowAssignmentService.assign(
        detail.getPwaApplication(),
        PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW,
        caseOfficer,
        acceptingUser.getLinkedPerson());

    sendCaseOfficerAssignedEmail(detail, caseOfficer.getFullName());

  }

  private void sendCaseOfficerAssignedEmail(PwaApplicationDetail applicationDetail, String caseOfficerName) {

    var submitterPerson = personService.getPersonById(applicationDetail.getSubmittedByPersonId());

    var props = new CaseOfficerAssignedEmailProps(submitterPerson.getFullName(), applicationDetail.getPwaApplicationRef(), caseOfficerName);

    notifyService.sendEmail(props, submitterPerson.getEmailAddress());

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
