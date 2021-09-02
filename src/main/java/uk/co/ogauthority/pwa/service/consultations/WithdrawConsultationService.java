package uk.co.ogauthority.pwa.service.consultations;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.notify.emailproperties.consultations.ConsultationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/**
 * A service to withdraw consultation requests from application.
 */
@Service
public class WithdrawConsultationService {

  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final ConsultationRequestService consultationRequestService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final TeamManagementService teamManagementService;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final NotifyService notifyService;
  private final Clock clock;

  @Autowired
  public WithdrawConsultationService(
      ConsulteeGroupDetailService consulteeGroupDetailService,
      ConsultationRequestService consultationRequestService,
      CamundaWorkflowService camundaWorkflowService,
      TeamManagementService teamManagementService,
      ConsulteeGroupTeamService consulteeGroupTeamService,
      WorkflowAssignmentService workflowAssignmentService,
      NotifyService notifyService,
      @Qualifier("utcClock") Clock clock) {
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.consultationRequestService = consultationRequestService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.teamManagementService = teamManagementService;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.notifyService = notifyService;
    this.clock = clock;
  }



  @Transactional
  public void withdrawAllOpenConsultationRequests(PwaApplication pwaApplication,
                                                  AuthenticatedUserAccount withdrawingUser) {
    var consultationRequests = consultationRequestService.getAllOpenRequestsByApplication(pwaApplication);
    for (var consultationRequest : consultationRequests) {
      withdrawConsultationRequest(consultationRequest, withdrawingUser);
    }
  }

  @Transactional
  public void withdrawConsultationRequest(ConsultationRequest consultationRequest, AuthenticatedUserAccount user) {
    var userWorkflowTask = consultationRequest.getStatus() == ConsultationRequestStatus.ALLOCATION
        ? PwaApplicationConsultationWorkflowTask.ALLOCATION : PwaApplicationConsultationWorkflowTask.RESPONSE;
    var workflowTaskInstance = new WorkflowTaskInstance(consultationRequest, userWorkflowTask);

    var responderPersonOpt = camundaWorkflowService.getAssignedPersonId(workflowTaskInstance);
    Person responderPerson = null;
    if (responderPersonOpt.isPresent()) {
      responderPerson = teamManagementService.getPerson(responderPersonOpt.get().asInt());
    }

    camundaWorkflowService.deleteProcessAndTask(workflowTaskInstance);

    var originalRequestStatus = consultationRequest.getStatus();
    consultationRequest.setStatus(ConsultationRequestStatus.WITHDRAWN);
    consultationRequest.setEndedByPersonId(user.getLinkedPerson().getId().asInt());
    consultationRequest.setEndTimestamp(Instant.now(clock));
    consultationRequestService.saveConsultationRequest(consultationRequest);

    List<Person> emailRecipients = getEmailRecipients(consultationRequest, originalRequestStatus, responderPerson);
    var consulteeGroupName = consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(
        consultationRequest.getConsulteeGroup()).getName();
    emailRecipients.forEach(recipient -> {
      var emailProps = buildWithdrawnEmailProps(recipient, consultationRequest, consulteeGroupName, user.getLinkedPerson());
      notifyService.sendEmail(emailProps, recipient.getEmailAddress());
    });

    workflowAssignmentService.clearAssignments(consultationRequest);
  }


  private List<Person> getEmailRecipients(
      ConsultationRequest consultationRequest, ConsultationRequestStatus originalRequestStatus, Person responderPerson) {

    List<Person> emailRecipients =  new ArrayList<>();
    if (originalRequestStatus == ConsultationRequestStatus.AWAITING_RESPONSE) {
      emailRecipients.add(responderPerson);

    } else {
      consulteeGroupTeamService.getTeamMembersForGroup(consultationRequest.getConsulteeGroup()).forEach(
          teamMember -> {
            if (teamMember.getRoles().contains(ConsulteeGroupMemberRole.RECIPIENT)) {
              emailRecipients.add(teamMember.getPerson());
            }
          });
    }
    return emailRecipients;
  }

  private ConsultationWithdrawnEmailProps buildWithdrawnEmailProps(Person recipient,
                                                                   ConsultationRequest consultationRequest,
                                                                   String consulteeGroupName,
                                                                   Person withdrawnByUser) {
    return new ConsultationWithdrawnEmailProps(
        recipient.getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupName,
        withdrawnByUser.getFullName());
  }


  public boolean canWithDrawConsultationRequest(ConsultationRequest consultationRequest) {
    return !ConsultationRequestService.getEndedStatuses().contains(consultationRequest.getStatus());
  }


}
