package uk.co.ogauthority.pwa.service.consultations;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.exception.WorkflowException;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.AppProcessingService;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationAssignedToYouEmailProps;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.teammanagement.OldTeamManagementService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamScopeReference;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidationHints;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidator;

@Service
public class AssignResponderService implements AppProcessingService {

  private final WorkflowAssignmentService workflowAssignmentService;
  private final AssignResponderValidator assignResponderValidator;
  private final OldTeamManagementService teamManagementService;
  private final CamundaWorkflowService camundaWorkflowService;
  private final ConsultationRequestService consultationRequestService;
  private final CaseLinkService caseLinkService;
  private final EmailService emailService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public AssignResponderService(WorkflowAssignmentService workflowAssignmentService,
                                AssignResponderValidator assignResponderValidator,
                                OldTeamManagementService teamManagementService,
                                CamundaWorkflowService camundaWorkflowService,
                                ConsultationRequestService consultationRequestService,
                                CaseLinkService caseLinkService,
                                EmailService emailService, TeamQueryService teamQueryService) {
    this.workflowAssignmentService = workflowAssignmentService;
    this.assignResponderValidator = assignResponderValidator;
    this.teamManagementService = teamManagementService;
    this.camundaWorkflowService = camundaWorkflowService;
    this.consultationRequestService = consultationRequestService;
    this.caseLinkService = caseLinkService;
    this.emailService = emailService;
    this.teamQueryService = teamQueryService;
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
      var caseManagementLink = caseLinkService.generateCaseManagementLink(consultationRequest.getPwaApplication());
      var emailProps = buildAssignedEmailProps(responderPerson, consultationRequest, assigningPerson, caseManagementLink);
      emailService.sendEmail(emailProps, responderPerson, consultationRequest.getPwaApplication().getAppReference());

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
    var consulteeGroupId = consultationRequest.getConsulteeGroup().getId();
    var teamType = TeamType.CONSULTEE;
    var teamScopeReference = TeamScopeReference.from(consulteeGroupId, teamType);
    return teamQueryService.userHasAtLeastOneScopedRole(
        (long) user.getWuaId(),
        teamType,
        teamScopeReference,
        Set.of(Role.RECIPIENT, Role.RESPONDER)
    );
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
        : TaskTag.from(TaskStatus.NOT_STARTED);

    return new TaskListEntry(
        task.getTaskName(),
        task.getRoute(processingContext),
        taskTag,
        task.getDisplayOrder());

  }
}
