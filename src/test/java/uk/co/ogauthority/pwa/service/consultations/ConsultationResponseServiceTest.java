package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.notify.emailproperties.consultations.ConsultationResponseReceivedEmailProps;
import uk.co.ogauthority.pwa.model.tasklist.TaskTag;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationResponseValidator;


@RunWith(MockitoJUnitRunner.class)
public class ConsultationResponseServiceTest {

  private ConsultationResponseService consultationResponseService;

  @Mock
  private ConsultationResponseRepository consultationResponseRepository;

  @Mock
  private ConsultationResponseValidator validator;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private EmailCaseLinkService emailCaseLinkService;

  @Captor
  private ArgumentCaptor<ConsultationResponse> responseCaptor;

  @Captor
  private ArgumentCaptor<ConsultationResponseReceivedEmailProps> responseEmailPropsCaptor;

  private Clock clock;

  private Person caseOfficerPerson;
  private ConsulteeGroupDetail groupDetail;
  private PwaApplication application;

  @Before
  public void setUp() {

    clock = Clock.fixed(Instant.parse(Instant.now().toString()), ZoneId.of("UTC"));

    caseOfficerPerson = new Person(1, "fore", "sur", "a@b.com", "012345");

    groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("name", "abb");
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(groupDetail.getConsulteeGroup())).thenReturn(groupDetail);

    when(workflowAssignmentService.getAssignee(any())).thenReturn(Optional.of(caseOfficerPerson));

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    application = detail.getPwaApplication();

    when(emailCaseLinkService.generateCaseManagementLink(application)).thenReturn("http://case-link");

    consultationResponseService = new ConsultationResponseService(consultationRequestService, consultationResponseRepository,
        validator, camundaWorkflowService, clock, notifyService, consulteeGroupDetailService, workflowAssignmentService, emailCaseLinkService);
  }


  @Test
  public void saveResponseAndCompleteWorkflow_confirmed_confirmedDescriptionNotProvided_caseOfficerAssigned() {

    ConsultationRequest consultationRequest = buildConsultationRequest();

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));
    verify(consultationResponseRepository, times(1)).save(responseCaptor.capture());
    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);
    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.RESPONDED);

    var response = responseCaptor.getValue();
    assertThat(response.getConsultationRequest()).isEqualTo(consultationRequest);
    assertThat(response.getResponseType()).isEqualTo(ConsultationResponseOption.CONFIRMED);
    assertThat(response.getResponseText()).isNull();
    assertThat(response.getResponseTimestamp()).isEqualTo(Instant.now(clock));
    assertThat(response.getRespondingPersonId()).isEqualTo(1);

    verify(notifyService, times(1)).sendEmail(responseEmailPropsCaptor.capture(), eq(caseOfficerPerson.getEmailAddress()));

    var props = responseEmailPropsCaptor.getValue();

    assertThat(props.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("APPLICATION_REFERENCE", application.getAppReference()),
            tuple("CONSULTEE_GROUP", groupDetail.getName()),
            tuple("CONSULTATION_RESPONSE", response.getResponseType().getDisplayText()),
            tuple("CASE_MANAGEMENT_LINK", "http://case-link"),
            tuple("RECIPIENT_FULL_NAME", caseOfficerPerson.getFullName())
        );

  }

  @Test
  public void saveResponseAndCompleteWorkflow_confirmed_confirmedDescriptionProvided() {

    ConsultationRequest consultationRequest = buildConsultationRequest();

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.CONFIRMED);
    form.setConfirmedDescription("some text");
    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));
    verify(consultationResponseRepository, times(1)).save(responseCaptor.capture());
    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);
    verify(workflowAssignmentService, times(1)).clearAssignments(consultationRequest);

    var response = responseCaptor.getValue();

    assertThat(response.getResponseText()).isEqualTo("some text");

  }

  @Test
  public void saveResponseAndCompleteWorkflow_rejected_caseOfficerAssigned() {

    ConsultationRequest consultationRequest = buildConsultationRequest();

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.REJECTED);
    form.setRejectedDescription("my reason");
    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));
    verify(consultationResponseRepository, times(1)).save(responseCaptor.capture());
    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);
    verify(workflowAssignmentService, times(1)).clearAssignments(consultationRequest);
    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.RESPONDED);

    var response = responseCaptor.getValue();
    assertThat(response.getConsultationRequest()).isEqualTo(consultationRequest);
    assertThat(response.getResponseType()).isEqualTo(ConsultationResponseOption.REJECTED);
    assertThat(response.getResponseText()).isEqualTo("my reason");
    assertThat(response.getResponseTimestamp()).isEqualTo(Instant.now(clock));
    assertThat(response.getRespondingPersonId()).isEqualTo(1);

    verify(notifyService, times(1)).sendEmail(responseEmailPropsCaptor.capture(), eq(caseOfficerPerson.getEmailAddress()));

    var props = responseEmailPropsCaptor.getValue();

    assertThat(props.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("APPLICATION_REFERENCE", application.getAppReference()),
            tuple("CONSULTEE_GROUP", groupDetail.getName()),
            tuple("CONSULTATION_RESPONSE", response.getResponseType().getDisplayText()),
            tuple("CASE_MANAGEMENT_LINK", "http://case-link"),
            tuple("RECIPIENT_FULL_NAME", caseOfficerPerson.getFullName())
        );

  }

  @Test(expected = WorkflowAssignmentException.class)
  public void saveResponseAndCompleteWorkflow_noCaseOfficer() {

    ConsultationRequest consultationRequest = buildConsultationRequest();

    var form = new ConsultationResponseForm();
    form.setConsultationResponseOption(ConsultationResponseOption.REJECTED);
    form.setRejectedDescription("my reason");
    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    when(workflowAssignmentService.getAssignee(any())).thenReturn(Optional.empty());

    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

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

    when(camundaWorkflowService.getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE)))
        .thenReturn(Optional.of(user.getLinkedPerson().getId()));

    boolean isMemberOfRequestGroup = consultationResponseService.isUserAssignedResponderForConsultation(user, consultationRequest);

    assertTrue(isMemberOfRequestGroup);

  }

  @Test
  public void isUserAssignedResponderForConsultation_invalid() {

    var consultationRequest = new ConsultationRequest();
    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    when(camundaWorkflowService.getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE)))
        .thenReturn(Optional.of(new PersonId(5)));

    boolean isMemberOfRequestGroup = consultationResponseService.isUserAssignedResponderForConsultation(user, consultationRequest);

    assertFalse(isMemberOfRequestGroup);

  }

  @Test
  public void canShowInTaskList_notRespondedYet() {

    var request = new ConsultationRequest();
    when(consultationResponseRepository.findByConsultationRequest(request)).thenReturn(Optional.empty());

    var processingContext = new PwaAppProcessingContext(
        null,
        null,
        Set.of(PwaAppProcessingPermission.CONSULTATION_RESPONDER),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request));

    boolean canShow = consultationResponseService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_alreadyResponded() {

    var request = new ConsultationRequest();
    var response = new ConsultationResponse();
    response.setConsultationRequest(request);
    when(consultationResponseRepository.findByConsultationRequest(request)).thenReturn(Optional.of(response));

    var processingContext = new PwaAppProcessingContext(
        null,
        null,
        Set.of(PwaAppProcessingPermission.CONSULTATION_RESPONDER),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request));

    boolean canShow = consultationResponseService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_caseOfficer() {

    var processingContext = new PwaAppProcessingContext(
        null,
        null,
        Set.of(PwaAppProcessingPermission.CASE_OFFICER_REVIEW),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("group", new ConsultationRequest()));

    boolean canShow = consultationResponseService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void canShowInTaskList_industry() {

    var processingContext = new PwaAppProcessingContext(
        null,
        null,
        Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("group", new ConsultationRequest()));

    boolean canShow = consultationResponseService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void getTaskListEntry() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    var processingContext = new PwaAppProcessingContext(
        detail,
        null,
        Set.of(),
        null,
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("group", new ConsultationRequest()));

    var taskListEntry = consultationResponseService.getTaskListEntry(PwaAppProcessingTask.CONSULTATION_RESPONSE, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.CONSULTATION_RESPONSE.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.CONSULTATION_RESPONSE.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.NOT_STARTED));
    assertThat(taskListEntry.getDisplayOrder()).isEqualTo(PwaAppProcessingTask.CONSULTATION_RESPONSE.getDisplayOrder());

  }

  @Test
  public void getLatestResponseForRequests_whenMultipleRequestsChecked_andLatestResponseFound() {
    var request1 = buildConsultationRequest();
    var request2 = buildConsultationRequest();
    request1.setId(40);
    request2.setId(41);
    var response = new ConsultationResponse();
    when(consultationResponseRepository.getFirstByConsultationRequestInOrderByResponseTimestampDesc(eq(List.of(request1, request2))))
        .thenReturn(response);
    assertThat(consultationResponseService.getLatestResponseForRequests(List.of(request1, request2))).isEqualTo(response);
  }

  @Test
  public void isThereAtLeastOneApprovalFromAnyGroup_approvalsPresent() {
    var request1 = buildConsultationRequest();
    request1.setId(40);
    request1.setStatus(ConsultationRequestStatus.RESPONDED);
    var request2 = buildConsultationRequest();
    request2.setStatus(ConsultationRequestStatus.RESPONDED);
    request2.setId(41);
    var response = new ConsultationResponse();
    response.setResponseType(ConsultationResponseOption.CONFIRMED);
    when(consultationResponseRepository.getFirstByConsultationRequestInOrderByResponseTimestampDesc(eq(List.of(request1, request2))))
        .thenReturn(response);
    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of(request1, request2));
    assertThat(consultationResponseService.isThereAtLeastOneApprovalFromAnyGroup(application)).isTrue();
  }

  private ConsultationRequest buildConsultationRequest() {
    var request = new ConsultationRequest();
    request.setConsulteeGroup(groupDetail.getConsulteeGroup());
    request.setPwaApplication(application);
    request.setConsulteeGroup(groupDetail.getConsulteeGroup());
    return request;
  }
}

