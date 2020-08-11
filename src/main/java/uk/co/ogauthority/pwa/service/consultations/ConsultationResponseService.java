package uk.co.ogauthority.pwa.service.consultations;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationResponseValidator;

@Service
public class ConsultationResponseService {

  private final ConsultationRequestService consultationRequestService;
  private final ConsultationResponseRepository consultationResponseRepository;
  private final ConsultationResponseValidator consultationResponseValidator;
  private final CamundaWorkflowService camundaWorkflowService;
  private final Clock clock;

  @Autowired
  public ConsultationResponseService(
      ConsultationRequestService consultationRequestService,
      ConsultationResponseRepository consultationResponseRepository,
      ConsultationResponseValidator consultationResponseValidator,
      CamundaWorkflowService camundaWorkflowService,
      @Qualifier("utcClock") Clock clock) {
    this.consultationRequestService = consultationRequestService;
    this.consultationResponseRepository = consultationResponseRepository;
    this.consultationResponseValidator = consultationResponseValidator;
    this.camundaWorkflowService = camundaWorkflowService;
    this.clock = clock;
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

  public void saveResponseAndCompleteWorkflow(ConsultationResponseForm form, ConsultationRequest consultationRequest, WebUserAccount user) {
    ConsultationResponse consultationResponse = mapFormToResponse(form, consultationRequest, user);
    consultationResponseRepository.save(consultationResponse);
    camundaWorkflowService.completeTask(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));
    consultationRequest.setStatus(ConsultationRequestStatus.RESPONDED);
    consultationRequestService.saveConsultationRequest(consultationRequest);
  }


  public BindingResult validate(ConsultationResponseForm form, BindingResult bindingResult) {
    consultationResponseValidator.validate(form, bindingResult);
    return bindingResult;
  }








}
