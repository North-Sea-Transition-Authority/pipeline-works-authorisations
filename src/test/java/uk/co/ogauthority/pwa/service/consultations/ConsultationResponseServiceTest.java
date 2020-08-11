package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
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


@RunWith(MockitoJUnitRunner.class)
public class ConsultationResponseServiceTest {

  private ConsultationResponseService consultationResponseService;

  @Mock
  ConsultationResponseRepository consultationResponseRepository;

  @Mock
  private ConsultationResponseValidator validator;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  private Clock clock;

  @Before
  public void setUp() {
    clock = Clock.fixed(Instant.parse(Instant.now().toString()), ZoneId.of("UTC"));
    consultationResponseService = new ConsultationResponseService(consultationRequestService, consultationResponseRepository,
        validator, camundaWorkflowService, clock);
  }


  @Test
  public void saveResponseAndCompleteWorkflow() {

    ConsultationRequest consultationRequest = new ConsultationRequest();
    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    ConsultationResponse expectedConsultationResponse = new ConsultationResponse();
    expectedConsultationResponse.setConsultationRequest(consultationRequest);
    expectedConsultationResponse.setResponseType(ConsultationResponseOption.CONFIRMED);
    expectedConsultationResponse.setResponseTimestamp(Instant.now(clock));
    expectedConsultationResponse.setRespondingPersonId(1);

    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(camundaWorkflowService, times(1)).completeTask(eq(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE)));
    verify(consultationResponseRepository, times(1)).save(expectedConsultationResponse);
    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);
    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.RESPONDED);
  }

  @Test
  public void saveResponseAndCompleteWorkflow_rejected() {

    ConsultationRequest consultationRequest = new ConsultationRequest();
    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.REJECTED);
    form.setRejectedDescription("my reason");
    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    ConsultationResponse expectedConsultationResponse = new ConsultationResponse();
    expectedConsultationResponse.setConsultationRequest(consultationRequest);
    expectedConsultationResponse.setResponseType(ConsultationResponseOption.REJECTED);
    expectedConsultationResponse.setResponseText("my reason");
    expectedConsultationResponse.setResponseTimestamp(Instant.now(clock));
    expectedConsultationResponse.setRespondingPersonId(1);

    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(camundaWorkflowService, times(1)).completeTask(eq(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE)));
    verify(consultationResponseRepository, times(1)).save(expectedConsultationResponse);
    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);
    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.RESPONDED);
  }


  @Test
  public void validate() {
    var form = new ConsultationResponseForm();
    consultationResponseService.validate(form, new BeanPropertyBindingResult(form, "form"));
    verify(validator, times(1)).validate(any(ConsultationResponseForm.class), any(BeanPropertyBindingResult.class));
  }


  @Test
  public void isUserAssignedResponderForConsultation_valid() {

    var consultationRequest = new ConsultationRequest();
    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    when(camundaWorkflowService.getAssignedPersonId(eq(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE))))
        .thenReturn(Optional.of(user.getLinkedPerson().getId()));

    boolean isMemberOfRequestGroup = consultationResponseService.isUserAssignedResponderForConsultation(user, consultationRequest);

    assertTrue(isMemberOfRequestGroup);

  }

  @Test
  public void isUserAssignedResponderForConsultation_invalid() {

    var consultationRequest = new ConsultationRequest();
    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    when(camundaWorkflowService.getAssignedPersonId(eq(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE))))
        .thenReturn(Optional.of(new PersonId(5)));

    boolean isMemberOfRequestGroup = consultationResponseService.isUserAssignedResponderForConsultation(user, consultationRequest);

    assertFalse(isMemberOfRequestGroup);

  }


}

