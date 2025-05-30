package uk.co.ogauthority.pwa.features.appprocessing.tasks.applicationupdate;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.features.appprocessing.tasklist.PwaAppProcessingTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.appworkflowmappings.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskState;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskStatus;
import uk.co.ogauthority.pwa.features.generalcase.tasklist.TaskTag;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowMessageEvent;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailProperties;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.service.appprocessing.options.ApproveOptionsService;
import uk.co.ogauthority.pwa.service.appprocessing.options.OptionsApprovalStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationUpdateRequestServiceTest {
  private static final String TELEPHONE = "123456789";

  private static final int PREPARER_1_ID = 100;
  private static final int PREPARER_2_ID = 200;

  private static final String PREPARER_FORENAME = "PREP";
  private static final String PREPARER_1_SURNAME = "1";
  private static final String PREPARER_2_SURNAME = "2";
  private static final String PREPARER_1_FULL_NAME = "PREP 1";
  private static final String PREPARER_2_FULL_NAME = "PREP 2";

  private static final String PREPARER_1_EMAIL = "PREP_1@email.com";
  private static final String PREPARER_2_EMAIL = "PREP_2@email.com";

  private static final String REASON = "REASON";
  private static final String CASE_MANAGEMENT_LINK = "case management link url";

  private static final PersonId REQUESTER_PERSON_ID = new PersonId(10);
  private static final PersonId RESPONDER_PERSON_ID = new PersonId(20);

  @Mock
  private ApplicationUpdateRequestRepository applicationUpdateRequestRepository;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private PersonService personService;

  @Mock
  private ApproveOptionsService approveOptionsService;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private EmailService emailService;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Captor
  private ArgumentCaptor<ApplicationUpdateRequest> appUpdateArgCapture;

  @Captor
  private ArgumentCaptor<EmailRecipient> recipientArgumentCaptor;

  @Captor
  private ArgumentCaptor<EmailProperties> emailPropertiesArgumentCaptor;

  @Captor
  private ArgumentCaptor<WorkflowMessageEvent> messageEventArgumentCaptor;

  private Person responderPerson;
  private Person requesterPerson;
  private WebUserAccount user;

  private Person preparer1;
  private Person preparer2;

  private PwaApplicationDetail pwaApplicationDetail;

  private ApplicationUpdateRequest defaultUpdateRequest;

  @BeforeEach
  void setUp() throws Exception {
    responderPerson = new Person(RESPONDER_PERSON_ID.asInt(), "test", "person", "email", TELEPHONE);
    requesterPerson = new Person(REQUESTER_PERSON_ID.asInt(), "test1", "person1", "email1", TELEPHONE);
    user = new WebUserAccount(99, responderPerson);
    preparer1 = new Person(PREPARER_1_ID, PREPARER_FORENAME, PREPARER_1_SURNAME, PREPARER_1_EMAIL, TELEPHONE);
    preparer2 = new Person(PREPARER_2_ID, PREPARER_FORENAME, PREPARER_2_SURNAME, PREPARER_2_EMAIL, TELEPHONE);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    applicationUpdateRequestService = new ApplicationUpdateRequestService(
        applicationUpdateRequestRepository,
        clock,
        pwaContactService,
        pwaApplicationDetailVersioningService,
        workflowAssignmentService,
        personService,
        approveOptionsService,
        caseLinkService,
        emailService);

    defaultUpdateRequest = new ApplicationUpdateRequest();
    defaultUpdateRequest.setRequestedByPersonId(REQUESTER_PERSON_ID);
    defaultUpdateRequest.setStatus(ApplicationUpdateRequestStatus.OPEN);

    when(personService.getPersonById(REQUESTER_PERSON_ID)).thenReturn(requesterPerson);
    when(caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication())).thenReturn(CASE_MANAGEMENT_LINK);
  }

  @Test
  void createApplicationUpdateRequest_savedRequestHasExpectedAttributes() {

    var deadlineDate = clock.instant().plus(5, ChronoUnit.DAYS);
    applicationUpdateRequestService.createApplicationUpdateRequest(pwaApplicationDetail, responderPerson, REASON, deadlineDate);

    verify(applicationUpdateRequestRepository, times(1)).save(appUpdateArgCapture.capture());

    var updateRequest = appUpdateArgCapture.getValue();

    assertThat(updateRequest.getRequestReason()).isEqualTo(REASON);
    assertThat(updateRequest.getDeadlineTimestamp()).isEqualTo(deadlineDate);
    assertThat(updateRequest.getRequestedByPersonId()).isEqualTo(RESPONDER_PERSON_ID);
    assertThat(updateRequest.getRequestedTimestamp()).isEqualTo(clock.instant());
    assertThat(updateRequest.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(updateRequest.getStatus()).isEqualTo(ApplicationUpdateRequestStatus.OPEN);

  }

  @Test
  void applicationHasOpenUpdateRequest_whenOpenUpdateRequest() {
    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN
    ))
        .thenReturn(Optional.of(defaultUpdateRequest));
    assertThat(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail)).isTrue();

  }

  @Test
  void applicationHasOpenUpdateRequest_whenNoOpenUpdateRequest() {
    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN
    ))
        .thenReturn(Optional.empty());
    assertThat(applicationUpdateRequestService.applicationHasOpenUpdateRequest(pwaApplicationDetail)).isFalse();

  }

  @Test
  void sendApplicationUpdateRequestedEmail_whenMultiplePreparers() {

    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER))
        .thenReturn(List.of(preparer1, preparer2));

    applicationUpdateRequestService.sendApplicationUpdateRequestedEmail(pwaApplicationDetail, responderPerson);

    verify(emailService, times(2)).sendEmail(emailPropertiesArgumentCaptor.capture(), recipientArgumentCaptor.capture(), eq(pwaApplicationDetail.getPwaApplicationRef()));

    var email1Properties = emailPropertiesArgumentCaptor.getAllValues().get(0);
    var recipient1 = recipientArgumentCaptor.getAllValues().get(0);

    var email2Properties = emailPropertiesArgumentCaptor.getAllValues().get(1);
    var recipient2 = recipientArgumentCaptor.getAllValues().get(1);

    assertThat(emailPropertiesArgumentCaptor.getAllValues()).allSatisfy(emailProperties ->
        assertThat(emailProperties.getTemplate().equals(NotifyTemplate.APPLICATION_UPDATE_REQUESTED))
    );

    assertThat(recipient1).isEqualTo(preparer1);
    assertEmailPropertiesAsExpected(email1Properties, PREPARER_1_FULL_NAME);

    assertThat(recipient2).isEqualTo(preparer2);
    assertEmailPropertiesAsExpected(email2Properties, PREPARER_2_FULL_NAME);
  }

  @Test
  void sendApplicationUpdateRequestedEmail_wheZeroPreparers() {

    applicationUpdateRequestService.sendApplicationUpdateRequestedEmail(pwaApplicationDetail, responderPerson);

    verify(emailService, times(0)).sendEmail(any(), any(), any());
  }


  @Test
  void submitApplicationUpdateRequest_serviceInteractions() {
    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER))
        .thenReturn(List.of(preparer1));

    // dont bother creating new tip app detail. just use existing one.
    when(pwaApplicationDetailVersioningService.createNewApplicationVersion(pwaApplicationDetail, user))
        .thenReturn(pwaApplicationDetail);

    var form = new ApplicationUpdateRequestForm();
    form.setRequestReason(REASON);
    form.setDeadlineTimestampStr(DateUtils.formatToDatePickerString(LocalDate.now()));

    applicationUpdateRequestService.submitApplicationUpdateRequest(pwaApplicationDetail, user, form);
    verify(applicationUpdateRequestRepository, times(1)).save(any());
    verify(emailService, times(1)).sendEmail(any(), eq(preparer1), eq(pwaApplicationDetail.getPwaApplicationRef()));
    verify(pwaApplicationDetailVersioningService, times(1)).createNewApplicationVersion(pwaApplicationDetail, user);
    verify(workflowAssignmentService, times(1))
        .triggerWorkflowMessageAndAssertTaskExists(
            messageEventArgumentCaptor.capture(),
            eq(PwaApplicationWorkflowTask.UPDATE_APPLICATION)
        );

    assertThat(messageEventArgumentCaptor.getValue().getEventName()).isEqualTo(PwaApplicationWorkflowMessageEvents.UPDATE_APPLICATION_REQUEST.getMessageEventName());
    assertThat(messageEventArgumentCaptor.getValue().getWorkflowSubject()).isEqualTo(pwaApplicationDetail.getPwaApplication());

  }

  private void assertEmailPropertiesAsExpected(EmailProperties emailProperties,
                                               String recipientFullName) {

    assertThat(emailProperties.getEmailPersonalisation()).contains(
        entry("REQUESTER_NAME", responderPerson.getFullName()),
        entry("APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef()),
        entry("CASE_MANAGEMENT_LINK", CASE_MANAGEMENT_LINK)
    );
    assertThat(emailProperties.getRecipientFullName()).isEqualTo(recipientFullName);

  }

  @Test
  void canShowInTaskList_appNotEnded_hasPermission_true() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    var processingContext = new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        Set.of(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE),
        null,
        null,
        Set.of());

    boolean canShow = applicationUpdateRequestService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  void canShowInTaskList_appNotEnded_noPermission_false() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    var processingContext = new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        Set.of(),
        null,
        null,
        Set.of());

    boolean canShow = applicationUpdateRequestService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void canShowInTaskList_appEnded_false() {

    pwaApplicationDetail.setStatus(PwaApplicationStatus.COMPLETE);

    var processingContext = new PwaAppProcessingContext(
        pwaApplicationDetail,
        null,
        Set.of(),
        null,
        null,
        Set.of());

    boolean canShow = applicationUpdateRequestService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  void respondToApplicationOpenUpdateRequest_happyPath() {

    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN
    )).thenReturn(Optional.of(defaultUpdateRequest));

    applicationUpdateRequestService.respondToApplicationOpenUpdateRequest(pwaApplicationDetail, responderPerson, "RESPONSE");

    verify(applicationUpdateRequestRepository, times(1)).save(appUpdateArgCapture.capture());


    assertThat(appUpdateArgCapture.getValue()).satisfies(applicationUpdateRequest -> {
      assertThat(applicationUpdateRequest.getResponseTimestamp()).isEqualTo(clock.instant());
      assertThat(applicationUpdateRequest.getResponseOtherChanges()).isEqualTo("RESPONSE");
      assertThat(applicationUpdateRequest.getResponseByPersonId()).isEqualTo(RESPONDER_PERSON_ID);
      assertThat(applicationUpdateRequest.getResponsePwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
      assertThat(applicationUpdateRequest.getStatus()).isEqualTo(ApplicationUpdateRequestStatus.RESPONDED);
    });

    verify(personService, times(1)).getPersonById(REQUESTER_PERSON_ID);

    verify(emailService, times(1)).sendEmail(emailPropertiesArgumentCaptor.capture(), eq(requesterPerson),
        eq(pwaApplicationDetail.getPwaApplicationRef()));

    assertThat(emailPropertiesArgumentCaptor.getValue()).satisfies(emailProperties -> {
      assertThat(emailProperties.getRecipientFullName()).isEqualTo(requesterPerson.getFullName());
      assertThat(emailProperties.getTemplate()).isEqualTo(NotifyTemplate.APPLICATION_UPDATE_RESPONDED);
      assertThat(emailProperties.getEmailPersonalisation()).containsEntry("APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef());
      assertThat(emailProperties.getEmailPersonalisation()).containsEntry("CASE_MANAGEMENT_LINK", CASE_MANAGEMENT_LINK);
    });

  }

  @Test
  void respondToApplicationOpenUpdateRequest_preparerResponseNotOverwritten() {

    // set the response, ensure it is not overwritten later
    var preparerPersonId = new PersonId(99);
    defaultUpdateRequest.setResponseByPersonId(preparerPersonId);
    defaultUpdateRequest.setResponseOtherChanges("my changes");

    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN
    )).thenReturn(Optional.of(defaultUpdateRequest));

    applicationUpdateRequestService.respondToApplicationOpenUpdateRequest(pwaApplicationDetail, responderPerson, null);

    verify(applicationUpdateRequestRepository, times(1)).save(appUpdateArgCapture.capture());

    assertThat(appUpdateArgCapture.getValue()).satisfies(applicationUpdateRequest -> {
      assertThat(applicationUpdateRequest.getResponseTimestamp()).isEqualTo(clock.instant());
      assertThat(applicationUpdateRequest.getResponseOtherChanges()).isEqualTo("my changes");
      assertThat(applicationUpdateRequest.getResponseByPersonId()).isEqualTo(preparerPersonId);
      assertThat(applicationUpdateRequest.getResponsePwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    });

    verify(personService, times(1)).getPersonById(REQUESTER_PERSON_ID);

    verify(emailService, times(1)).sendEmail(emailPropertiesArgumentCaptor.capture(), eq(requesterPerson),
        eq(pwaApplicationDetail.getPwaApplicationRef()));

    assertThat(emailPropertiesArgumentCaptor.getValue()).satisfies(emailProperties -> {
      assertThat(emailProperties.getRecipientFullName()).isEqualTo(requesterPerson.getFullName());
      assertThat(emailProperties.getTemplate()).isEqualTo(NotifyTemplate.APPLICATION_UPDATE_RESPONDED);
      assertThat(emailProperties.getEmailPersonalisation().get("APPLICATION_REFERENCE"))
          .isEqualTo(pwaApplicationDetail.getPwaApplicationRef());
      assertThat(emailProperties.getEmailPersonalisation()).containsEntry("CASE_MANAGEMENT_LINK", CASE_MANAGEMENT_LINK);
    });

  }

  @Test
  void respondToApplicationOpenUpdateRequest_noOpenUpdateRequest() {
    assertThrows(PwaEntityNotFoundException.class, () ->
      applicationUpdateRequestService.respondToApplicationOpenUpdateRequest(pwaApplicationDetail, responderPerson, "RESPONSE"));
  }

  @Test
  void storeResponseWithoutSubmitting_happyPath() {

    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN
    )).thenReturn(Optional.of(defaultUpdateRequest));

    applicationUpdateRequestService.storeResponseWithoutSubmitting(pwaApplicationDetail, responderPerson, "my text");

    verify(applicationUpdateRequestRepository, times(1)).save(appUpdateArgCapture.capture());

    assertThat(appUpdateArgCapture.getValue()).satisfies(applicationUpdateRequest -> {
      assertThat(applicationUpdateRequest.getResponseTimestamp()).isEqualTo(clock.instant());
      assertThat(applicationUpdateRequest.getResponseOtherChanges()).isEqualTo("my text");
      assertThat(applicationUpdateRequest.getResponseByPersonId()).isEqualTo(responderPerson.getId());
      assertThat(applicationUpdateRequest.getResponsePwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
      assertThat(applicationUpdateRequest.getStatus()).isEqualTo(ApplicationUpdateRequestStatus.OPEN);
    });

    verifyNoInteractions(emailService);

  }

  @Test
  void storeResponseWithoutSubmitting_noOpenUpdateRequest() {
    assertThrows(PwaEntityNotFoundException.class, () ->
      applicationUpdateRequestService.storeResponseWithoutSubmitting(pwaApplicationDetail, responderPerson, "RESPONSE"));
  }

  @Test
  void getTaskListEntry_appUpdate_inProgress() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    var updateRequest = new ApplicationUpdateRequest();
    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(any(), any())).thenReturn(Optional.of(updateRequest));

    var taskListEntry = applicationUpdateRequestService.getTaskListEntry(PwaAppProcessingTask.RFI, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.RFI.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.RFI.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isEqualTo(TaskTag.from(TaskStatus.IN_PROGRESS));
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_appUpdate_notInProgress() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(any(), any())).thenReturn(Optional.empty());

    var taskListEntry = applicationUpdateRequestService.getTaskListEntry(PwaAppProcessingTask.RFI, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.RFI.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.RFI.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isNull();
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.EDIT);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void getTaskListEntry_appUpdate_notInProgress_unrespondedOptionApproval() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    var processingContext = new PwaAppProcessingContext(detail, null, Set.of(), null, null, Set.of());

    when(approveOptionsService.getOptionsApprovalStatus(detail)).thenReturn(OptionsApprovalStatus.APPROVED_UNRESPONDED);

    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(any(), any())).thenReturn(Optional.empty());

    var taskListEntry = applicationUpdateRequestService.getTaskListEntry(PwaAppProcessingTask.RFI, processingContext);

    assertThat(taskListEntry.getTaskName()).isEqualTo(PwaAppProcessingTask.RFI.getTaskName());
    assertThat(taskListEntry.getRoute()).isEqualTo(PwaAppProcessingTask.RFI.getRoute(processingContext));
    assertThat(taskListEntry.getTaskTag()).isNull();
    assertThat(taskListEntry.getTaskState()).isEqualTo(TaskState.LOCK);
    assertThat(taskListEntry.getTaskInfoList()).isEmpty();

  }

  @Test
  void endUpdateRequestIfExists_openUpdateRequestExists_requestStatusUpdated() {

    var openUpdateRequest = new ApplicationUpdateRequest();
    openUpdateRequest.setStatus(ApplicationUpdateRequestStatus.OPEN);

    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN)).thenReturn(Optional.of(openUpdateRequest));

    applicationUpdateRequestService.endUpdateRequestIfExists(pwaApplicationDetail);

    verify(applicationUpdateRequestRepository, times(1)).save(appUpdateArgCapture.capture());

    var updateRequest = appUpdateArgCapture.getValue();
    assertThat(updateRequest.getStatus()).isEqualTo(ApplicationUpdateRequestStatus.ENDED);
  }

  @Test
  void endUpdateRequestIfExists_openUpdateRequestDoesNotExists_noUpdateRequestProcessingDone() {

    var openUpdateRequest = new ApplicationUpdateRequest();
    openUpdateRequest.setStatus(ApplicationUpdateRequestStatus.OPEN);

    applicationUpdateRequestService.endUpdateRequestIfExists(pwaApplicationDetail);

    verify(applicationUpdateRequestRepository, never()).save(any());
  }

}