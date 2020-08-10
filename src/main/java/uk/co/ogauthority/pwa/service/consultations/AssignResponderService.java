package uk.co.ogauthority.pwa.service.consultations;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidationHints;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidator;

@Service
public class AssignResponderService {

  private final WorkflowAssignmentService workflowAssignmentService;
  private final AssignResponderValidator assignResponderValidator;
  private final ConsulteeGroupTeamService consulteeGroupTeamService;
  private final TeamManagementService teamManagementService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final ConsultationRequestService consultationRequestService;

  @Autowired
  public AssignResponderService(
      WorkflowAssignmentService workflowAssignmentService,
      AssignResponderValidator assignResponderValidator,
      ConsulteeGroupTeamService consulteeGroupTeamService,
      TeamManagementService teamManagementService,
      CamundaWorkflowService camundaWorkflowService,
      ConsultationRequestService consultationRequestService) {
    this.workflowAssignmentService = workflowAssignmentService;
    this.assignResponderValidator = assignResponderValidator;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.teamManagementService = teamManagementService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.consultationRequestService = consultationRequestService;
  }


  public List<Person> getAllRespondersForRequest(ConsultationRequest consultationRequest) {
    return workflowAssignmentService.getAssignmentCandidates(consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE).stream()
        .sorted(Comparator.comparing(Person::getSurname))
        .collect(Collectors.toList());
  }

  public void assignUserAndCompleteWorkflow(AssignResponderForm form, ConsultationRequest consultationRequest, WebUserAccount user) {

    camundaWorkflowService.completeTask(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION));

    var responderPerson = teamManagementService.getPerson(form.getResponderPersonId());

    workflowAssignmentService.assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        responderPerson,
        user.getLinkedPerson());

    consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);
    consultationRequestService.saveConsultationRequest(consultationRequest);

  }

  public boolean isUserMemberOfRequestGroup(WebUserAccount user, ConsultationRequest consultationRequest) {
    for (var member: consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())) {
      if ((member.getRoles().contains(ConsulteeGroupMemberRole.RECIPIENT)
          || member.getRoles().contains(ConsulteeGroupMemberRole.RESPONDER))
          &&  member.getConsulteeGroup().equals(consultationRequest.getConsulteeGroup())) {
        return true;
      }
    }
    return false;
  }

  public BindingResult validate(AssignResponderForm form, BindingResult bindingResult, ConsultationRequest consultationRequest) {
    assignResponderValidator.validate(form, bindingResult,
        new AssignResponderValidationHints(this, consultationRequest));
    return bindingResult;
  }

}
