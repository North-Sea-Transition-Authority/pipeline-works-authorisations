package uk.co.ogauthority.pwa.service.consultations;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ConsultationResponseReceivedEmailProps;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationResponseValidator;

/*
 A service to  create response /assign response to consultation request
 */
@Service
public class ConsultationResponseService {

  private final ConsultationRequestService consultationRequestService;
  private final ConsultationResponseRepository consultationResponseRepository;
  private final ConsultationResponseValidator consultationResponseValidator;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;
  private final NotifyService notifyService;
  private final ConsulteeGroupDetailService consulteeGroupDetailService;
  private final WorkflowAssignmentService workflowAssignmentService;
  private final EmailCaseLinkService emailCaseLinkService;

  @Autowired
  public ConsultationResponseService(
      ConsultationRequestService consultationRequestService,
      ConsultationResponseRepository consultationResponseRepository,
      ConsultationResponseValidator consultationResponseValidator,
      CamundaWorkflowService camundaWorkflowService,
      @Qualifier("utcClock") Clock clock,
      NotifyService notifyService,
      ConsulteeGroupDetailService consulteeGroupDetailService,
      WorkflowAssignmentService workflowAssignmentService,
      EmailCaseLinkService emailCaseLinkService) {
    this.consultationRequestService = consultationRequestService;
    this.consultationResponseRepository = consultationResponseRepository;
    this.consultationResponseValidator = consultationResponseValidator;
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
    if (form.getConsultationResponseOption().equals(ConsultationResponseOption.REJECTED)) {
      consultationResponse.setResponseText(form.getRejectedDescription());
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

  }

  public BindingResult validate(ConsultationResponseForm form, BindingResult bindingResult) {
    consultationResponseValidator.validate(form, bindingResult);
    return bindingResult;
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
        response.getResponseType().getDisplayText(),
        emailCaseLinkService.generateCaseManagementLink(application)
    );

    notifyService.sendEmail(emailProps, caseOfficerEmail);

  }

}
