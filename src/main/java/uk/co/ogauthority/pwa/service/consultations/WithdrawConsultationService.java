package uk.co.ogauthority.pwa.service.consultations;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.features.email.EmailRecipientWithName;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.teammanagement.OldTeamManagementService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;

/**
 * A service to withdraw consultation requests from application.
 */
@Service
public class WithdrawConsultationService {

  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final ConsultationRequestService consultationRequestService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final OldTeamManagementService teamManagementService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final Clock clock;
  private final EmailService emailService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public WithdrawConsultationService(
      ConsulteeGroupDetailService consulteeGroupDetailService,
      ConsultationRequestService consultationRequestService,
      CamundaWorkflowService camundaWorkflowService,
      OldTeamManagementService teamManagementService,
      WorkflowAssignmentService workflowAssignmentService,
      @Qualifier("utcClock") Clock clock,
      EmailService emailService, TeamQueryService teamQueryService) {
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.consultationRequestService = consultationRequestService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.teamManagementService = teamManagementService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.clock = clock;
    this.emailService = emailService;
    this.teamQueryService = teamQueryService;
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

    List<EmailRecipientWithName> emailRecipientNames =
        getEmailRecipientNames(consultationRequest, originalRequestStatus, responderPerson);
    var consulteeGroupName =
        consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consultationRequest.getConsulteeGroup()).getName();

    emailRecipientNames
        .forEach(recipient -> {
          var emailProps = buildWithdrawnEmailProps(consultationRequest, consulteeGroupName,
              recipient.fullName(), user.getLinkedPerson().getFullName());
          emailService.sendEmail(emailProps, recipient, consultationRequest.getPwaApplication().getAppReference());
        });

    workflowAssignmentService.clearAssignments(consultationRequest);
  }


  private List<EmailRecipientWithName> getEmailRecipientNames(
      ConsultationRequest consultationRequest, ConsultationRequestStatus originalRequestStatus, Person responderPerson) {

    if (originalRequestStatus == ConsultationRequestStatus.AWAITING_RESPONSE) {
      return List.of(EmailRecipientWithName.from(responderPerson));
    }

    var consulteeGroupId = consultationRequest.getConsulteeGroup().getId();
    var teamScopeReference = TeamScopeReference.from(consulteeGroupId, TeamType.CONSULTEE);

    var membersOfConsulteeGroup = teamQueryService.getMembersOfScopedTeam(TeamType.CONSULTEE, teamScopeReference);

    return membersOfConsulteeGroup.stream()
        .filter(teamMemberView -> teamMemberView.roles().contains(Role.RECIPIENT))
        .map(EmailRecipientWithName::from)
        .toList();
  }

  private ConsultationWithdrawnEmailProps buildWithdrawnEmailProps(ConsultationRequest consultationRequest,
                                                                   String consulteeGroupName,
                                                                   String recipientName,
                                                                   String withdrawnByUserName) {
    return new ConsultationWithdrawnEmailProps(
        recipientName,
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupName,
        withdrawnByUserName
    );
  }

  public boolean canWithDrawConsultationRequest(ConsultationRequest consultationRequest) {
    return !ConsultationRequestService.getEndedStatuses().contains(consultationRequest.getStatus());
  }
}
