package uk.co.ogauthority.pwa.service.appprocessing.applicationupdate;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.applicationupdates.ApplicationUpdateRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.enums.appprocessing.applicationupdates.ApplicationUpdateRequestStatus;
import uk.co.ogauthority.pwa.model.enums.notify.NotifyTemplate;
import uk.co.ogauthority.pwa.model.notify.emailproperties.EmailProperties;
import uk.co.ogauthority.pwa.repository.appprocessing.applicationupdates.ApplicationUpdateRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowMessageEvents;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowMessageEvent;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.person.PersonService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.PwaApplicationDetailVersioningService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class ApplicationUpdateRequestServiceTest {
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

  private static final PersonId REQUESTER_PERSON_ID = new PersonId(10);
  private static final PersonId RESPONDER_PERSON_ID = new PersonId(20);

  @Mock
  private ApplicationUpdateRequestRepository applicationUpdateRequestRepository;

  @Mock
  private NotifyService notifyService;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private PwaApplicationDetailVersioningService pwaApplicationDetailVersioningService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private PersonService personService;

  private Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ApplicationUpdateRequestService applicationUpdateRequestService;

  @Captor
  private ArgumentCaptor<ApplicationUpdateRequest> appUpdateArgCapture;

  @Captor
  private ArgumentCaptor<String> stringArgCaptor;

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

  @Before
  public void setUp() throws Exception {
    responderPerson = new Person(RESPONDER_PERSON_ID.asInt(), "test", "person", "email", TELEPHONE);
    requesterPerson = new Person(REQUESTER_PERSON_ID.asInt(), "test1", "person1", "email1", TELEPHONE);
    user = new WebUserAccount(99, responderPerson);
    preparer1 = new Person(PREPARER_1_ID, PREPARER_FORENAME, PREPARER_1_SURNAME, PREPARER_1_EMAIL, TELEPHONE);
    preparer2 = new Person(PREPARER_2_ID, PREPARER_FORENAME, PREPARER_2_SURNAME, PREPARER_2_EMAIL, TELEPHONE);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    applicationUpdateRequestService = new ApplicationUpdateRequestService(
        applicationUpdateRequestRepository,
        clock,
        notifyService,
        pwaContactService,
        pwaApplicationDetailVersioningService,
        workflowAssignmentService,
        personService
    );

    defaultUpdateRequest = new ApplicationUpdateRequest();
    defaultUpdateRequest.setRequestedByPersonId(REQUESTER_PERSON_ID);

    when(personService.getPersonById(REQUESTER_PERSON_ID)).thenReturn(requesterPerson);
  }

  @Test
  public void createApplicationUpdateRequest_savedRequestHasExpectedAttributes() {

    applicationUpdateRequestService.createApplicationUpdateRequest(pwaApplicationDetail, responderPerson, REASON);

    verify(applicationUpdateRequestRepository, times(1)).save(appUpdateArgCapture.capture());

    var updateRequest = appUpdateArgCapture.getValue();

    assertThat(updateRequest.getRequestReason()).isEqualTo(REASON);
    assertThat(updateRequest.getRequestedByPersonId()).isEqualTo(RESPONDER_PERSON_ID);
    assertThat(updateRequest.getRequestedTimestamp()).isEqualTo(clock.instant());
    assertThat(updateRequest.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(updateRequest.getStatus()).isEqualTo(ApplicationUpdateRequestStatus.OPEN);

  }

  @Test
  public void applicationDetailHasOpenUpdateRequest_whenOpenUpdateRequest() {
    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN
    ))
        .thenReturn(Optional.of(defaultUpdateRequest));
    assertThat(applicationUpdateRequestService.applicationDetailHasOpenUpdateRequest(pwaApplicationDetail)).isTrue();

  }

  @Test
  public void applicationDetailHasOpenUpdateRequest_whenNoOpenUpdateRequest() {
    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN
    ))
        .thenReturn(Optional.empty());
    assertThat(applicationUpdateRequestService.applicationDetailHasOpenUpdateRequest(pwaApplicationDetail)).isFalse();

  }

  @Test
  public void sendApplicationUpdateRequestedEmail_whenMultiplePreparers() {

    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER))
        .thenReturn(List.of(preparer1, preparer2));

    applicationUpdateRequestService.sendApplicationUpdateRequestedEmail(pwaApplicationDetail, responderPerson);

    verify(notifyService, times(2)).sendEmail(emailPropertiesArgumentCaptor.capture(), stringArgCaptor.capture());

    var email1Properties = emailPropertiesArgumentCaptor.getAllValues().get(0);
    var email1Address = stringArgCaptor.getAllValues().get(0);

    var email2Properties = emailPropertiesArgumentCaptor.getAllValues().get(1);
    var email2Address = stringArgCaptor.getAllValues().get(1);

    assertThat(emailPropertiesArgumentCaptor.getAllValues()).allSatisfy(emailProperties ->
        assertThat(emailProperties.getTemplate().equals(NotifyTemplate.APPLICATION_UPDATE_REQUESTED))
    );

    assertThat(email1Address).isEqualTo(PREPARER_1_EMAIL);
    assertEmailPropertiesAsExpected(email1Properties, PREPARER_1_FULL_NAME);

    assertThat(email2Address).isEqualTo(PREPARER_2_EMAIL);
    assertEmailPropertiesAsExpected(email2Properties, PREPARER_2_FULL_NAME);
  }

  @Test
  public void sendApplicationUpdateRequestedEmail_wheZeroPreparers() {

    applicationUpdateRequestService.sendApplicationUpdateRequestedEmail(pwaApplicationDetail, responderPerson);

    verify(notifyService, times(0)).sendEmail(any(), any());
  }


  @Test
  public void submitApplicationUpdateRequest_serviceInteractions() {
    when(pwaContactService.getPeopleInRoleForPwaApplication(pwaApplicationDetail.getPwaApplication(),
        PwaContactRole.PREPARER))
        .thenReturn(List.of(preparer1));

    // dont bother creating new tip app detail. just use existing one.
    when(pwaApplicationDetailVersioningService.createNewApplicationVersion(pwaApplicationDetail, user))
        .thenReturn(pwaApplicationDetail);

    applicationUpdateRequestService.submitApplicationUpdateRequest(pwaApplicationDetail, user, REASON);
    verify(applicationUpdateRequestRepository, times(1)).save(any());
    verify(notifyService, times(1)).sendEmail(any(), eq(PREPARER_1_EMAIL));
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
        entry("CASE_OFFICER_NAME", responderPerson.getFullName()),
        entry("APPLICATION_REFERENCE", pwaApplicationDetail.getPwaApplicationRef())
    );
    assertThat(emailProperties.getRecipientFullName()).isEqualTo(recipientFullName);

  }

  @Test
  public void canShowInTaskList_hasPermission() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.REQUEST_APPLICATION_UPDATE), null);

    boolean canShow = applicationUpdateRequestService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermission() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null);

    boolean canShow = applicationUpdateRequestService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }

  @Test
  public void respondToApplicationOpenUpdateRequest_happyPath() {

    when(applicationUpdateRequestRepository.findByPwaApplicationDetail_pwaApplicationAndStatus(
        pwaApplicationDetail.getPwaApplication(), ApplicationUpdateRequestStatus.OPEN
    ))
        .thenReturn(Optional.of(defaultUpdateRequest));

    applicationUpdateRequestService.respondToApplicationOpenUpdateRequest(pwaApplicationDetail, responderPerson, "RESPONSE");

    verify(applicationUpdateRequestRepository, times(1)).save(appUpdateArgCapture.capture());


    assertThat(appUpdateArgCapture.getValue()).satisfies(applicationUpdateRequest -> {
      assertThat(applicationUpdateRequest.getResponseTimestamp()).isEqualTo(clock.instant());
      assertThat(applicationUpdateRequest.getResponseOtherChanges()).isEqualTo("RESPONSE");
      assertThat(applicationUpdateRequest.getResponseByPersonId()).isEqualTo(RESPONDER_PERSON_ID);
      assertThat(applicationUpdateRequest.getResponsePwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    });

    verify(personService, times(1)).getPersonById(REQUESTER_PERSON_ID);

    verify(notifyService, times(1)).sendEmail(emailPropertiesArgumentCaptor.capture(), eq(requesterPerson.getEmailAddress()));

    assertThat(emailPropertiesArgumentCaptor.getValue()).satisfies(emailProperties -> {
      assertThat(emailProperties.getRecipientFullName()).isEqualTo(requesterPerson.getFullName());
      assertThat(emailProperties.getTemplate()).isEqualTo(NotifyTemplate.APPLICATION_UPDATE_RESPONDED);
      assertThat(emailProperties.getEmailPersonalisation().get("APPLICATION_REFERENCE"))
          .isEqualTo(pwaApplicationDetail.getPwaApplicationRef());
    });

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void respondToApplicationOpenUpdateRequest_noOpenUpdateRequest() {
    applicationUpdateRequestService.respondToApplicationOpenUpdateRequest(pwaApplicationDetail, responderPerson, "RESPONSE");

  }
}