package uk.co.ogauthority.pwa.service.appprocessing.initialreview;

import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.CaseOfficerAssignedEmailProps;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.users.UserAccountService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/**
 * Service to provide actions available to users at the 'Initial review' stage after submission.
 */
@Service
public class InitialReviewService {

  private final PwaApplicationDetailService applicationDetailService;
  private final CamundaWorkflowService workflowService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final TeamManagementService teamManagementService;
  private final NotifyService notifyService;
  private final UserAccountService userAccountService;

  @Autowired
  public InitialReviewService(PwaApplicationDetailService applicationDetailService,
                              CamundaWorkflowService workflowService,
                              WorkflowAssignmentService workflowAssignmentService,
                              TeamManagementService teamManagementService,
                              NotifyService notifyService,
                              UserAccountService userAccountService) {
    this.applicationDetailService = applicationDetailService;
    this.workflowService = workflowService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.teamManagementService = teamManagementService;
    this.notifyService = notifyService;
    this.userAccountService = userAccountService;
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

    var submitterPerson = userAccountService.getWebUserAccount(applicationDetail.getSubmittedByWuaId())
        .getLinkedPerson();

    var props = new CaseOfficerAssignedEmailProps(submitterPerson.getFullName(), applicationDetail.getPwaApplicationRef(), caseOfficerName);

    notifyService.sendEmail(props, submitterPerson.getEmailAddress());

  }

}
