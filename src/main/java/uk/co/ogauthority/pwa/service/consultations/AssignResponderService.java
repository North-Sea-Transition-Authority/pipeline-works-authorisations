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
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.consultations.ConsultationAssignedToYouEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
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
  private final EmailCaseLinkService emailCaseLinkService;

  @Autowired
  public AssignResponderService(WorkflowAssignmentService workflowAssignmentService,
                                AssignResponderValidator assignResponderValidator,
                                ConsulteeGroupTeamService consulteeGroupTeamService,
                                TeamManagementService teamManagementService,
                                CamundaWorkflowService camundaWorkflowService,
                                ConsultationRequestService consultationRequestService,
                                NotifyService notifyService,
                                EmailCaseLinkService emailCaseLinkService) {
    this.workflowAssignmentService = workflowAssignmentService;
    this.assignResponderValidator = assignResponderValidator;
    this.consulteeGroupTeamService = consulteeGroupTeamService;
    this.teamManagementService = teamManagementService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.consultationRequestService = consultationRequestService;
    this.notifyService = notifyService;
    this.emailCaseLinkService = emailCaseLinkService;
  }

  public List<Person> getAllRespondersForRequest(ConsultationRequest consultationRequest) {
    return workflowAssignmentService.getAssignmentCandidates(consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE).stream()
        .sorted(Comparator.comparing(Person::getSurname))
        .collect(Collectors.toList());
  }

  public void assignResponder(AssignResponderForm form,
                              ConsultationRequest consultationRequest,
                              WebUserAccount assigningUser) {

    var responderPerson = teamManagementService.getPerson(form.getResponderPersonId());
    var assigningPerson = assigningUser.getLinkedPerson();

    var workflowTask = camundaWorkflowService.getAllActiveWorkflowTasks(consultationRequest).stream()
        .findFirst()
        .orElseThrow(() -> new WorkflowException(
            String.format("Couldn't find active task for consultation request with id %s", consultationRequest.getId())));

    // move workflow on if assigning for first time
    var taskType = PwaApplicationConsultationWorkflowTask.getByTaskKey(workflowTask.getTaskKey());
    if (taskType == PwaApplicationConsultationWorkflowTask.ALLOCATION) {

      camundaWorkflowService.completeTask(workflowTask);

      consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);
      consultationRequestService.saveConsultationRequest(consultationRequest);

    }

    workflowAssignmentService.assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        responderPerson,
        assigningPerson);

    // if user didn't assign to themselves, email the assigned responder
    if (!Objects.equals(responderPerson, assigningPerson)) {
      var caseManagementLink = emailCaseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication());
      var emailProps = buildAssignedEmailProps(responderPerson, consultationRequest, assigningPerson, caseManagementLink);
      notifyService.sendEmail(emailProps, responderPerson.getEmailAddress());

    }

  }

  private ConsultationAssignedToYouEmailProps buildAssignedEmailProps(Person assignee,
                                                                      ConsultationRequest consultationRequest,
                                                                      Person assigningPerson,
                                                                      String caseManagementLink) {

    String appRef = consultationRequest.getPwaApplication().getAppReference();
    String dueDateDisplay = DateUtils.formatDate(consultationRequest.getDeadlineDate());

    return new ConsultationAssignedToYouEmailProps(
        assignee.getFullName(),
        appRef,
        assigningPerson.getFullName(),
        dueDateDisplay,
        caseManagementLink);

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
    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.ASSIGN_RESPONDER)
        && processingContext.getActiveConsultationRequest().isPresent();
  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    var request = processingContext.getActiveConsultationRequestOrThrow().getConsultationRequest();

    var assignedPerson = camundaWorkflowService
        .getAllActiveWorkflowTasks(request)
        .stream()
        .findFirst()
        .flatMap(workflowAssignmentService::getAssignee)
        .orElse(null);

    var taskTag = assignedPerson != null
        ? new TaskTag(assignedPerson.getFullName(), "govuk-tag--purple")
        : TaskTag.from(TaskStatus.NOT_COMPLETED);

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        taskTag,
        task.getDisplayOrder());

  }
}
