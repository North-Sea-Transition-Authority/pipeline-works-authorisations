package uk.co.ogauthority.pwa.service.consultations;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ConsultationAssignedToYouEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidationHints;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidator;

@Service
public class AssignResponderService implements AppProcessingService {

  private final WorkflowAssignmentService workflowAssignmentService;
  private final AssignResponderValidator assignResponderValidator;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final TeamManagementService teamManagementService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final ConsultationRequestService consultationRequestService;
  private final NotifyService notifyService;

  @Autowired
  public AssignResponderService(
      WorkflowAssignmentService workflowAssignmentService,
      AssignResponderValidator assignResponderValidator,
      ConsulteeGroupTeamService consulteeGroupTeamService,
      TeamManagementService teamManagementService,
      CamundaWorkflowService camundaWorkflowService,
      ConsultationRequestService consultationRequestService,
      NotifyService notifyService) {
    this.workflowAssignmentService = workflowAssignmentService;
    this.assignResponderValidator = assignResponderValidator;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.teamManagementService = teamManagementService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.consultationRequestService = consultationRequestService;
    this.notifyService = notifyService;
  }


  public List<Person> getAllRespondersForRequest(ConsultationRequest consultationRequest) {
    return workflowAssignmentService.getAssignmentCandidates(consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE).stream()
        .sorted(Comparator.comparing(Person::getSurname))
        .collect(Collectors.toList());
  }

  public void assignUserAndCompleteWorkflow(AssignResponderForm form,
                                            ConsultationRequest consultationRequest,
                                            WebUserAccount assigningUser) {

    camundaWorkflowService.completeTask(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION));

    var responderPerson = teamManagementService.getPerson(form.getResponderPersonId());
    var assigningPerson = assigningUser.getLinkedPerson();

    workflowAssignmentService.assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        responderPerson,
        assigningPerson);

    consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);
    consultationRequestService.saveConsultationRequest(consultationRequest);

    // if user didn't assign to themselves, email the assigned responder
    if (!Objects.equals(responderPerson, assigningPerson)) {

      var emailProps = buildAssignedEmailProps(responderPerson, consultationRequest, assigningPerson);
      notifyService.sendEmail(emailProps, responderPerson.getEmailAddress());

    }

  }

  public void reassignUser(AssignResponderForm form,
                           ConsultationRequest consultationRequest,
                           WebUserAccount assigningUser) {

    var responderPerson = teamManagementService.getPerson(form.getResponderPersonId());
    var assigningPerson = assigningUser.getLinkedPerson();

    workflowAssignmentService.assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        responderPerson,
        assigningPerson);

    // if user didn't assign to themselves, email the assigned responder
    if (!Objects.equals(responderPerson, assigningPerson)) {
      var emailProps = buildAssignedEmailProps(responderPerson, consultationRequest, assigningPerson);
      notifyService.sendEmail(emailProps, responderPerson.getEmailAddress());
    }
  }


  private ConsultationAssignedToYouEmailProps buildAssignedEmailProps(Person assignee,
                                                                      ConsultationRequest consultationRequest,
                                                                      Person assigningPerson) {

    String appRef = consultationRequest.getPwaApplication().getAppReference();
    String dueDateDisplay = DateUtils.formatDateTime(consultationRequest.getDeadlineDate());

    return new ConsultationAssignedToYouEmailProps(
        assignee.getFullName(),
        appRef,
        assigningPerson.getFullName(),
        dueDateDisplay
    );

  }


  public boolean isUserMemberOfRequestGroup(WebUserAccount user, ConsultationRequest consultationRequest) {
    return consulteeGroupTeamService.getTeamMemberByPerson(user.getLinkedPerson())
        .filter(member -> member.getConsulteeGroup().equals(consultationRequest.getConsulteeGroup()))
        .map(member -> member.getRoles().contains(ConsulteeGroupMemberRole.RECIPIENT)
            || member.getRoles().contains(ConsulteeGroupMemberRole.RESPONDER))
        .orElse(false);
  }

  public BindingResult validate(AssignResponderForm form, BindingResult bindingResult, ConsultationRequest consultationRequest) {
    assignResponderValidator.validate(form, bindingResult,
        new AssignResponderValidationHints(this, consultationRequest));
    return bindingResult;
  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.ASSIGN_RESPONDER);
  }

}
