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
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequestTestUtil;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseData;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponseFileLink;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.AppFile;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.model.notify.emailproperties.consultations.ConsultationMultiResponseReceivedEmailProps;
import uk.co.ogauthority.pwa.model.notify.emailproperties.consultations.ConsultationResponseReceivedEmailProps;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseFileLinkRepository;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationResponseRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.service.enums.appprocessing.TaskStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.notify.EmailCaseLinkService;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;
import uk.co.ogauthority.pwa.testutils.PwaAppProcessingContextDtoTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ConsultationResponseServiceTest {

  private ConsultationResponseService consultationResponseService;

  @Mock
  private ConsultationResponseRepository consultationResponseRepository;

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

  @Mock
  private ConsultationResponseDataService consultationResponseDataService;

  @Mock
  private ConsultationResponseFileLinkRepository consultationResponseFileLinkRepository;

  @Mock
  private AppFileService appFileService;

  @Captor
  private ArgumentCaptor<ConsultationResponse> responseCaptor;

  @Captor
  private ArgumentCaptor<ConsultationResponseReceivedEmailProps> singleResponseEmailPropsCaptor;

  @Captor
  private ArgumentCaptor<ConsultationMultiResponseReceivedEmailProps> multiResponseEmailPropsCaptor;

  @Captor
  private ArgumentCaptor<List<ConsultationResponseFileLink>> consultationResponseFileLinkArgumentCaptor;

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

    consultationResponseService = new ConsultationResponseService(
        consultationRequestService,
        consultationResponseRepository,
        camundaWorkflowService,
        clock,
        notifyService,
        consulteeGroupDetailService,
        workflowAssignmentService,
        emailCaseLinkService,
        consultationResponseDataService,
        consultationResponseFileLinkRepository,
        appFileService);

  }

  @Test
  public void saveResponseAndCompleteWorkflow_confirmed_confirmedDescriptionNotProvided_caseOfficerAssigned_singleResponseEmailSent() {

    ConsultationRequest consultationRequest = buildConsultationRequest();

    var dataForm = buildDataForm(ConsultationResponseOptionGroup.CONTENT);

    var form = new ConsultationResponseForm();
    form.setResponseDataForms(Map.of(ConsultationResponseOptionGroup.CONTENT, dataForm));

    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    var response = new ConsultationResponse();
    when(consultationResponseRepository.save(any())).thenReturn(response);

    var data = new ConsultationResponseData();
    data.setResponseType(ConsultationResponseOption.CONFIRMED);
    when(consultationResponseDataService.createAndSaveResponseData(any(), any())).thenReturn(List.of(data));

    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));
    verify(consultationResponseRepository, times(1)).save(responseCaptor.capture());
    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);
    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.RESPONDED);

    var responseValue = responseCaptor.getValue();
    assertThat(responseValue.getConsultationRequest()).isEqualTo(consultationRequest);
    assertThat(responseValue.getResponseTimestamp()).isEqualTo(Instant.now(clock));
    assertThat(responseValue.getRespondingPersonId()).isEqualTo(1);

    verify(consultationResponseDataService, times(1)).createAndSaveResponseData(response, form);

    verify(notifyService, times(1)).sendEmail(singleResponseEmailPropsCaptor.capture(), eq(caseOfficerPerson.getEmailAddress()));

    var props = singleResponseEmailPropsCaptor.getValue();

    assertThat(props.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("APPLICATION_REFERENCE", application.getAppReference()),
            tuple("CONSULTEE_GROUP", groupDetail.getName()),
            tuple("CONSULTATION_RESPONSE", data.getResponseType().getLabelText()),
            tuple("CASE_MANAGEMENT_LINK", "http://case-link"),
            tuple("RECIPIENT_FULL_NAME", caseOfficerPerson.getFullName())
        );

  }

  @Test
  public void saveResponseAndCompleteWorkflow_multiResponse_caseOfficerAssigned_multiResponseEmailSent() {

    ConsultationRequest consultationRequest = buildConsultationRequest();

    var eiaDataForm = buildDataForm(ConsultationResponseOptionGroup.EIA_REGS);
    var habitatsDataForm = buildDataForm(ConsultationResponseOptionGroup.HABITATS_REGS);

    var form = new ConsultationResponseForm();
    form.setResponseDataForms(Map.of(
        ConsultationResponseOptionGroup.EIA_REGS, eiaDataForm,
        ConsultationResponseOptionGroup.HABITATS_REGS, habitatsDataForm
    ));

    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    var response = new ConsultationResponse();
    when(consultationResponseRepository.save(any())).thenReturn(response);

    var eiaData = new ConsultationResponseData();
    eiaData.setResponseGroup(ConsultationResponseOptionGroup.EIA_REGS);
    eiaData.setResponseType(ConsultationResponseOption.EIA_AGREE);
    var habitatsData = new ConsultationResponseData();
    habitatsData.setResponseGroup(ConsultationResponseOptionGroup.HABITATS_REGS);
    habitatsData.setResponseType(ConsultationResponseOption.HABITATS_AGREE);
    when(consultationResponseDataService.createAndSaveResponseData(any(), any())).thenReturn(List.of(eiaData, habitatsData));

    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(camundaWorkflowService, times(1)).completeTask(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE));
    verify(consultationResponseRepository, times(1)).save(responseCaptor.capture());
    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);
    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.RESPONDED);

    var responseValue = responseCaptor.getValue();
    assertThat(responseValue.getConsultationRequest()).isEqualTo(consultationRequest);
    assertThat(responseValue.getResponseTimestamp()).isEqualTo(Instant.now(clock));
    assertThat(responseValue.getRespondingPersonId()).isEqualTo(1);

    verify(consultationResponseDataService, times(1)).createAndSaveResponseData(response, form);

    verify(notifyService, times(1)).sendEmail(multiResponseEmailPropsCaptor.capture(), eq(caseOfficerPerson.getEmailAddress()));

    var props = multiResponseEmailPropsCaptor.getValue();

    assertThat(props.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("APPLICATION_REFERENCE", application.getAppReference()),
            tuple("CONSULTEE_GROUP", groupDetail.getName()),
            tuple("CASE_MANAGEMENT_LINK", "http://case-link"),
            tuple("RECIPIENT_FULL_NAME", caseOfficerPerson.getFullName())
        );

    assertThat(props.getEmailPersonalisation().get("CONSULTATION_RESPONSES"))
        .contains(eiaData.getResponseGroup().getResponseLabel())
        .contains(eiaData.getResponseType().getRadioInsetText(application.getAppReference()))
        .contains(habitatsData.getResponseGroup().getResponseLabel())
        .contains(habitatsData.getResponseType().getRadioInsetText(application.getAppReference()));

  }

  @Test
  public void saveResponseAndCompleteWorkflow_confirmed_fileLinksCreated() {

    ConsultationRequest consultationRequest = buildConsultationRequest();

    var dataForm = buildDataForm(ConsultationResponseOptionGroup.CONTENT);

    var form = new ConsultationResponseForm();
    form.setResponseDataForms(Map.of(ConsultationResponseOptionGroup.CONTENT, dataForm));
    form.setUploadedFileWithDescriptionForms(List.of(
        new UploadFileWithDescriptionForm("id", "desc", Instant.now())
    ));

    var response = new ConsultationResponse();
    when(consultationResponseRepository.save(any())).thenReturn(response);

    var appFile = new AppFile(consultationRequest.getPwaApplication(), "id", AppFilePurpose.CONSULTATION_RESPONSE,
        ApplicationFileLinkStatus.FULL);
    when(appFileService.getFilesByIdIn(eq(appFile.getPwaApplication()), eq(AppFilePurpose.CONSULTATION_RESPONSE), any())).thenReturn(List.of(
        appFile));

    var user = new WebUserAccount(1, new Person(1, null, null, null, null));


    var data = new ConsultationResponseData();
    data.setResponseType(ConsultationResponseOption.CONFIRMED);
    when(consultationResponseDataService.createAndSaveResponseData(any(), any())).thenReturn(List.of(data));

    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(consultationResponseFileLinkRepository, times(1)).saveAll(consultationResponseFileLinkArgumentCaptor.capture());

    assertThat(consultationResponseFileLinkArgumentCaptor.getValue())
        .extracting(ConsultationResponseFileLink::getConsultationResponse, ConsultationResponseFileLink::getAppFile)
        .containsExactlyInAnyOrder(
            tuple(response, appFile)
        );

    verify(appFileService, times(1)).updateFiles(form, consultationRequest.getPwaApplication(), AppFilePurpose.CONSULTATION_RESPONSE, FileUpdateMode.KEEP_UNLINKED_FILES, user);
    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.RESPONDED);

  }

  private ConsultationResponseDataForm buildDataForm(ConsultationResponseOptionGroup responseGroup) {
    var dataForm = new ConsultationResponseDataForm();
    dataForm.setConsultationResponseOption(responseGroup.getResponseOptionNumber(1).orElseThrow());
    return dataForm;
  }

  @Test(expected = WorkflowAssignmentException.class)
  public void saveResponseAndCompleteWorkflow_noCaseOfficer() {

    ConsultationRequest consultationRequest = buildConsultationRequest();

    var dataForm = buildDataForm(ConsultationResponseOptionGroup.CONTENT);

    var form = new ConsultationResponseForm();
    form.setResponseDataForms(Map.of(ConsultationResponseOptionGroup.CONTENT, dataForm));

    var user = new WebUserAccount(1, new Person(1, null, null, null, null));

    when(workflowAssignmentService.getAssignee(any())).thenReturn(Optional.empty());

    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

  }

  private void setupEmailTest(ConsultationResponseData data,
                              ConsultationResponseOptionGroup consultationResponseOptionGroup,
                              ConsultationResponseOption consultationResponseOption) {

    data.setResponseType(consultationResponseOption);
    data.setResponseGroup(consultationResponseOptionGroup);
    when(consultationResponseDataService.createAndSaveResponseData(any(), any())).thenReturn(List.of(data));
  }

  @Test
  public void saveResponseAndCompleteWorkflow_responseOptionHasEmailText_includeResponseTextInEmail_emailHasCorrectText() {

    var consultationRequest = buildConsultationRequest();
    var form = new ConsultationResponseForm();
    var data = new ConsultationResponseData();
    data.setResponseText("My response text");
    setupEmailTest(data, ConsultationResponseOptionGroup.ADVICE, ConsultationResponseOption.PROVIDE_ADVICE);

    var user = new WebUserAccount(1, new Person(1, null, null, null, null));
    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(notifyService, times(1)).sendEmail(
        singleResponseEmailPropsCaptor.capture(), eq(caseOfficerPerson.getEmailAddress()));
    var props = singleResponseEmailPropsCaptor.getValue();

    assertThat(ConsultationResponseOption.PROVIDE_ADVICE.getEmailText()).isPresent();
    assertThat(props.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("CONSULTATION_RESPONSE", ConsultationResponseOption.PROVIDE_ADVICE.getEmailText().get() + data.getResponseText())
        );
  }

  @Test
  public void saveResponseAndCompleteWorkflow_responseOptionHasEmailText_dontIncludeResponseTextInEmail_emailHasCorrectText() {

    var consultationRequest = buildConsultationRequest();
    var form = new ConsultationResponseForm();
    var data = new ConsultationResponseData();
    setupEmailTest(data, ConsultationResponseOptionGroup.ADVICE, ConsultationResponseOption.NO_ADVICE);

    var user = new WebUserAccount(1, new Person(1, null, null, null, null));
    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(notifyService, times(1)).sendEmail(
        singleResponseEmailPropsCaptor.capture(), eq(caseOfficerPerson.getEmailAddress()));
    var props = singleResponseEmailPropsCaptor.getValue();

    assertThat(ConsultationResponseOption.NO_ADVICE.getEmailText()).isPresent();
    assertThat(props.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("CONSULTATION_RESPONSE", ConsultationResponseOption.NO_ADVICE.getEmailText().get())
        );
  }

  @Test
  public void saveResponseAndCompleteWorkflow_responseOptionDoesNotHaveEmailText_defaultTextUsed_emailHasCorrectText() {

    var consultationRequest = buildConsultationRequest();
    var form = new ConsultationResponseForm();
    var data = new ConsultationResponseData();
    setupEmailTest(data, ConsultationResponseOptionGroup.CONTENT, ConsultationResponseOption.CONFIRMED);

    var user = new WebUserAccount(1, new Person(1, null, null, null, null));
    consultationResponseService.saveResponseAndCompleteWorkflow(form, consultationRequest, user);

    verify(notifyService, times(1)).sendEmail(
        singleResponseEmailPropsCaptor.capture(), eq(caseOfficerPerson.getEmailAddress()));

    var props = singleResponseEmailPropsCaptor.getValue();

    assertThat(props.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("CONSULTATION_RESPONSE", ConsultationResponseOption.CONFIRMED.getLabelText()));
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
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request), Set.of());

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
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("name", request), Set.of());

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
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("group", new ConsultationRequest()),
        Set.of());

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
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("group", new ConsultationRequest()),
        Set.of());

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
        PwaAppProcessingContextDtoTestUtils.appInvolvementWithConsultationRequest("group", new ConsultationRequest()),
        Set.of());

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
  public void areConsultationResponsesValidForOptionsApproval_approvalsPresent_noEmtDisagreeResponse_valid() {
    var request1 = buildConsultationRequest();
    request1.setId(40);
    request1.setStatus(ConsultationRequestStatus.RESPONDED);
    var request2 = buildConsultationRequest();
    request2.setStatus(ConsultationRequestStatus.RESPONDED);
    request2.setId(41);
    var response = new ConsultationResponse();

    when(consultationResponseRepository.getFirstByConsultationRequestInOrderByResponseTimestampDesc(eq(List.of(request1, request2))))
        .thenReturn(response);

    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of(request1, request2));

    var data = new ConsultationResponseData(response);
    data.setResponseGroup(ConsultationResponseOptionGroup.CONTENT);
    data.setResponseType(ConsultationResponseOption.CONFIRMED);
    data.setResponseText("text");

    when(consultationResponseDataService.findAllByConsultationResponseIn(List.of(response)))
        .thenReturn(List.of(data));

    assertThat(consultationResponseService.areConsultationResponsesValidForOptionsApproval(application)).isTrue();

  }

  @Test
  public void areConsultationResponsesValidForOptionsApproval_approvalsPresent_includesEmtDisagreeResponse_invalid() {

    var hseRequest = ConsultationRequestTestUtil.createWithRespondedRequest(
        application, ConsulteeGroupTestingUtils.createConsulteeGroup("hse", "hse").getConsulteeGroup());
    var emtRequest = ConsultationRequestTestUtil.createWithRespondedRequest(
        application, ConsulteeGroupTestingUtils.createConsulteeGroup("emt", "emt").getConsulteeGroup());
    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of(hseRequest, emtRequest));


    var hseResponse = new ConsultationResponse();
    when(consultationResponseRepository.getFirstByConsultationRequestInOrderByResponseTimestampDesc(eq(List.of(hseRequest))))
        .thenReturn(hseResponse);

    var emtResponse = new ConsultationResponse();
    when(consultationResponseRepository.getFirstByConsultationRequestInOrderByResponseTimestampDesc(eq(List.of(emtRequest))))
        .thenReturn(emtResponse);


    var hseResponseData = new ConsultationResponseData(hseResponse);
    hseResponseData.setResponseGroup(ConsultationResponseOptionGroup.ADVICE);
    hseResponseData.setResponseType(ConsultationResponseOption.PROVIDE_ADVICE);

    var emtResponseData = new ConsultationResponseData(emtResponse);
    emtResponseData.setResponseGroup(ConsultationResponseOptionGroup.EIA_REGS);
    emtResponseData.setResponseType(ConsultationResponseOption.EIA_DISAGREE);
    when(consultationResponseDataService.findAllByConsultationResponseIn(List.of(hseResponse, emtResponse)))
        .thenReturn(List.of(hseResponseData, emtResponseData));

    assertThat(consultationResponseService.areConsultationResponsesValidForOptionsApproval(application)).isFalse();

  }

  @Test
  public void areConsultationResponsesValidForOptionsApproval_approvalPresentAndNonApprovalPresent_valid() {

    var hseRequest = ConsultationRequestTestUtil.createWithRespondedRequest(
        application, ConsulteeGroupTestingUtils.createConsulteeGroup("hse", "hse").getConsulteeGroup());
    var oduRequest = ConsultationRequestTestUtil.createWithRespondedRequest(
        application, ConsulteeGroupTestingUtils.createConsulteeGroup("odu", "odu").getConsulteeGroup());
    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of(hseRequest, oduRequest));


    var hseResponse = new ConsultationResponse();
    when(consultationResponseRepository.getFirstByConsultationRequestInOrderByResponseTimestampDesc(eq(List.of(hseRequest))))
        .thenReturn(hseResponse);

    var oduResponse = new ConsultationResponse();
    when(consultationResponseRepository.getFirstByConsultationRequestInOrderByResponseTimestampDesc(eq(List.of(oduRequest))))
        .thenReturn(oduResponse);


    var hseResponseData = new ConsultationResponseData(hseResponse);
    hseResponseData.setResponseGroup(ConsultationResponseOptionGroup.ADVICE);
    hseResponseData.setResponseType(ConsultationResponseOption.PROVIDE_ADVICE);

    var oduResponseData = new ConsultationResponseData(oduResponse);
    oduResponseData.setResponseGroup(ConsultationResponseOptionGroup.ADVICE);
    oduResponseData.setResponseType(ConsultationResponseOption.REJECTED);
    when(consultationResponseDataService.findAllByConsultationResponseIn(List.of(hseResponse, oduResponse)))
        .thenReturn(List.of(hseResponseData, oduResponseData));

    assertThat(consultationResponseService.areConsultationResponsesValidForOptionsApproval(application)).isTrue();

  }


  @Test
  public void areConsultationResponsesValidForOptionsApproval_noApprovalsPresent_invalid() {

    var oduRequest = ConsultationRequestTestUtil.createWithRespondedRequest(application, groupDetail.getConsulteeGroup());
    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of(oduRequest));

    var oduResponse = new ConsultationResponse();
    when(consultationResponseRepository.getFirstByConsultationRequestInOrderByResponseTimestampDesc(eq(List.of(oduRequest))))
        .thenReturn(oduResponse);

    var oduResponseData = new ConsultationResponseData(oduResponse);
    oduResponseData.setResponseGroup(ConsultationResponseOptionGroup.CONTENT);
    oduResponseData.setResponseType(ConsultationResponseOption.REJECTED);
    when(consultationResponseDataService.findAllByConsultationResponseIn(List.of(oduResponse)))
        .thenReturn(List.of(oduResponseData));

    assertThat(consultationResponseService.areConsultationResponsesValidForOptionsApproval(application)).isFalse();

  }

  @Test
  public void areConsultationResponsesValidForOptionsApproval_noResponses_invalid() {
    var request1 = buildConsultationRequest();
    request1.setId(40);
    request1.setStatus(ConsultationRequestStatus.RESPONDED);

    when(consultationRequestService.getAllRequestsByApplication(application)).thenReturn(List.of(request1));

    assertThat(consultationResponseService.areConsultationResponsesValidForOptionsApproval(application)).isFalse();

  }

  @Test
  public void getConsultationResponseFileLink_successfullyRetrieved() {
    var appFile = new AppFile(application, "FILE_ID", AppFilePurpose.CONSULTATION_RESPONSE, ApplicationFileLinkStatus.FULL);
    var fileLink = new ConsultationResponseFileLink(null, appFile);
    when(consultationResponseFileLinkRepository.findByAppFile_PwaApplicationAndAppFile(application, appFile)).thenReturn(
        Optional.of(fileLink));
    assertThat(consultationResponseService.getConsultationResponseFileLink(appFile)).isEqualTo(Optional.of(fileLink));
  }

  @Test
  public void deleteConsultationResponseFileLink_calledRepositoryMethod() {
    var fileLink = new ConsultationResponseFileLink(null, null);
    consultationResponseFileLinkRepository.delete(fileLink);
    verify(consultationResponseFileLinkRepository).delete(fileLink);
  }

  private ConsultationRequest buildConsultationRequest() {
    var request = new ConsultationRequest();
    request.setConsulteeGroup(groupDetail.getConsulteeGroup());
    request.setPwaApplication(application);
    request.setConsulteeGroup(groupDetail.getConsulteeGroup());
    return request;
  }
}

