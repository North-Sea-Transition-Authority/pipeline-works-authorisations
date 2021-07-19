package uk.co.ogauthority.pwa.service.consultations;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsultationRequestDto;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.notify.emailproperties.consultations.ConsultationResponseReceivedEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskListEntry;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.tasks.AppProcessingService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.application.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

/*
 A service to  create response /assign response to consultation request
 */
@Service
public class ConsultationResponseService implements AppProcessingService {

  private final ConsultationRequestService consultationRequestService;
  private final ConsultationResponseRepository consultationResponseRepository;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;
  private final NotifyService notifyService;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final EmailCaseLinkService emailCaseLinkService;

  @Autowired
  public ConsultationResponseService(ConsultationRequestService consultationRequestService,
                                     ConsultationResponseRepository consultationResponseRepository,
                                     CamundaWorkflowService camundaWorkflowService,
                                     @Qualifier("utcClock") Clock clock,
                                     NotifyService notifyService,
                                     ConsulteeGroupDetailService consulteeGroupDetailService,
                                     WorkflowAssignmentService workflowAssignmentService,
                                     EmailCaseLinkService emailCaseLinkService) {
    this.consultationRequestService = consultationRequestService;
    this.consultationResponseRepository = consultationResponseRepository;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
    this.notifyService = notifyService;
    this.consulteeGroupDetailService = consulteeGroupDetailService;
    this.workflowAssignmentService = workflowAssignmentService;
    this.emailCaseLinkService = emailCaseLinkService;
  }

  public List<ConsultationResponse> getResponsesByConsultationRequests(List<ConsultationRequest> consultationRequests) {
    return consultationResponseRepository.getAllByConsultationRequestIn(consultationRequests);
  }

  public Optional<ConsultationResponse> getResponseByConsultationRequest(ConsultationRequest consultationRequest) {
    return consultationResponseRepository.findByConsultationRequest(consultationRequest);
  }

  public boolean isUserAssignedResponderForConsultation(WebUserAccount user, ConsultationRequest consultationRequest) {

    Optional<PersonId> assignedResponderPersonId = camundaWorkflowService
        .getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));

    return assignedResponderPersonId
        .map(personId -> Objects.equals(personId, user.getLinkedPerson().getId()))
        .orElse(false);

  }

  private ConsultationResponse mapFormToResponse(ConsultationResponseForm form,
                                                 ConsultationRequest consultationRequest,
                                                 WebUserAccount user) {

    ConsultationResponse consultationResponse = new ConsultationResponse();
    consultationResponse.setConsultationRequest(consultationRequest);
    consultationResponse.setResponseType(form.getConsultationResponseOption());

    var response1 = form.getConsultationResponseOptionGroup().getResponseOptionNumber(1);
    var response2 = form.getConsultationResponseOptionGroup().getResponseOptionNumber(2);

    if (form.getConsultationResponseOption().equals(response1)) {
      consultationResponse.setResponseText(form.getOption1Description());
    }

    if (form.getConsultationResponseOption().equals(response2)) {
      consultationResponse.setResponseText(form.getOption2Description());
    }

    consultationResponse.setResponseTimestamp(Instant.now(clock));
    consultationResponse.setRespondingPersonId(user.getLinkedPerson().getId().asInt());

    return consultationResponse;

  }

  @Transactional
  public void saveResponseAndCompleteWorkflow(ConsultationResponseForm form, ConsultationRequest consultationRequest, WebUserAccount user) {

    ConsultationResponse consultationResponse = mapFormToResponse(form, consultationRequest, user);
    consultationResponseRepository.save(consultationResponse);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));
    consultationRequest.setStatus(ConsultationRequestStatus.RESPONDED);
    consultationRequestService.saveConsultationRequest(consultationRequest);

    sendResponseNotificationToCaseOfficer(consultationRequest, consultationResponse);

    workflowAssignmentService.clearAssignments(consultationRequest);

  }

  private void sendResponseNotificationToCaseOfficer(ConsultationRequest consultationRequest, ConsultationResponse response) {

    var application = consultationRequest.getPwaApplication();

    var caseOfficerPerson = workflowAssignmentService
        .getAssignee(new WorkflowTaskInstance(application, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW))
        .orElseThrow(() -> new WorkflowAssignmentException(
            String.format("Expected to find a case officer for application with ID: %s", application.getId())));

    String caseOfficerName = caseOfficerPerson.getFullName();
    String caseOfficerEmail = caseOfficerPerson.getEmailAddress();

    var tipGroupDetail = consulteeGroupDetailService
        .getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consultationRequest.getConsulteeGroup());

    String consulteeGroupName = tipGroupDetail.getName();

    var emailProps = new ConsultationResponseReceivedEmailProps(
        caseOfficerName,
        application.getAppReference(),
        consulteeGroupName,
        response.getResponseType().getLabelText(),
        emailCaseLinkService.generateCaseManagementLink(application)
    );

    notifyService.sendEmail(emailProps, caseOfficerEmail);

  }

  @Override
  public boolean canShowInTaskList(PwaAppProcessingContext processingContext) {

    boolean responded = processingContext.getActiveConsultationRequest()
        .map(ConsultationRequestDto::getConsultationRequest)
        .flatMap(this::getResponseByConsultationRequest)
        .isPresent();

    return processingContext.getAppProcessingPermissions().contains(PwaAppProcessingPermission.CONSULTATION_RESPONDER)
        && !responded;

  }

  @Override
  public TaskListEntry getTaskListEntry(PwaAppProcessingTask task, PwaAppProcessingContext processingContext) {

    // given show in task list rules, assumes not responded
    return new TaskListEntry(
        PwaAppProcessingTask.CONSULTATION_RESPONSE.getTaskName(),
        PwaAppProcessingTask.CONSULTATION_RESPONSE.getRoute(processingContext),
        TaskTag.from(TaskStatus.NOT_STARTED),
        PwaAppProcessingTask.CONSULTATION_RESPONSE.getDisplayOrder()
    );

  }

  public ConsultationResponse getLatestResponseForRequests(List<ConsultationRequest> requests) {
    return consultationResponseRepository.getFirstByConsultationRequestInOrderByResponseTimestampDesc(requests);
  }

  public boolean isThereAtLeastOneApprovalFromAnyGroup(PwaApplication pwaApplication) {
    var groupRequestMap = consultationRequestService.getAllRequestsByApplication(pwaApplication).stream()
        .filter(consultationRequest -> consultationRequest.getStatus() == ConsultationRequestStatus.RESPONDED)
        .collect(Collectors.groupingBy(ConsultationRequest::getConsulteeGroup));

    var latestResponsesByGroup = groupRequestMap.keySet().stream()
        .map(consulteeGroup -> getLatestResponseForRequests(groupRequestMap.get(consulteeGroup)))
        .collect(Collectors.toList());

    return latestResponsesByGroup.stream()
        .anyMatch(consultationResponse ->
            Set.of(ConsultationResponseOption.CONFIRMED, ConsultationResponseOption.PROVIDE_ADVICE, ConsultationResponseOption.NO_ADVICE)
                .contains(consultationResponse.getResponseType()));

  }

}
