package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.ogauthority.pwa.teams.TeamType.CONSULTEE;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.workflow.assignments.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.features.email.EmailRecipientWithName;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@ExtendWith(MockitoExtension.class)
class WithdrawConsultationServiceTest {


  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;

  @Mock
  CamundaWorkflowService camundaWorkflowService;

  @Mock
  private PersonService personService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private EmailService emailService;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private Clock clock;

  @Captor
  private ArgumentCaptor<ConsultationRequest> consultationRequestArgumentCaptor;

  @InjectMocks
  private WithdrawConsultationService withdrawConsultationService;

  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount authenticatedUserAccount;


  @BeforeEach
  void setUp() {

    var webUserAccount = new WebUserAccount(1, new Person(1, "", "", "", ""));
    authenticatedUserAccount = new AuthenticatedUserAccount(webUserAccount, List.of());

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);

  }


  @Test
  void withdrawConsultationRequest_emailResponder() {
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);

    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);
    consultationRequest.setConsulteeGroup(consulteeGroup);
    consultationRequest.setPwaApplication(pwaApplicationDetail.getPwaApplication());
    consultationRequest.setEndTimestamp(Instant.now(clock));

    var consultationResponse = new ConsultationResponse();
    consultationResponse.setRespondingPersonId(2);
    consultationResponse.setConsultationRequest(consultationRequest);
    Person respondingPerson = new Person(2, "respFirstName", "respLastName", "respFirstName@live.com", null);
    when(personService.getPersonById(respondingPerson.getId())).thenReturn(respondingPerson);

    var workflowTaskInstance = new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE);
    when(camundaWorkflowService.getAssignedPersonId(workflowTaskInstance)).thenReturn(Optional.of(respondingPerson.getId()));



    var consulteeGroupDetail = new ConsulteeGroupDetail();
    consulteeGroupDetail.setName("my group");
    consulteeGroupDetail.setConsulteeGroup(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consultationRequest.getConsulteeGroup())).thenReturn(consulteeGroupDetail);

    when(clock.instant()).thenReturn(Instant.now());

    withdrawConsultationService.withdrawConsultationRequest(consultationRequest, authenticatedUserAccount);

    //Test process termination and marking request as withdrawn
    verify(camundaWorkflowService).deleteProcessAndTask(workflowTaskInstance);

    verify(consultationRequestService).saveConsultationRequest(consultationRequestArgumentCaptor.capture());
    assertThat(consultationRequestArgumentCaptor.getValue().getStatus()).isEqualTo(ConsultationRequestStatus.WITHDRAWN);
    assertThat(consultationRequestArgumentCaptor.getValue().getEndedByPersonId()).isEqualTo(authenticatedUserAccount.getLinkedPerson().getId().asInt());

    //Test email template and recipient email address
    var expectedEmailProps = new ConsultationWithdrawnEmailProps(
        respondingPerson.getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupDetail.getName(),
        authenticatedUserAccount.getLinkedPerson().getFullName());
    verify(emailService).sendEmail(expectedEmailProps, EmailRecipientWithName.from(respondingPerson), consultationRequest.getPwaApplication().getAppReference());

    verify(workflowAssignmentService).clearAssignments(consultationRequest);
  }


  @Test
  void withdrawConsultationRequest_emailTeamMembers() {
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);

    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.ALLOCATION);
    consultationRequest.setConsulteeGroup(consulteeGroup);
    consultationRequest.setPwaApplication(pwaApplicationDetail.getPwaApplication());
    consultationRequest.setEndTimestamp(Instant.now(clock));

    var teamMember1 = new TeamMemberView(1L, "Mr.", "test", "surname", "myEmail1@mail.com", null, null, List.of(Role.RECIPIENT));
    var teamMember2 = new TeamMemberView(2L, "Mr.", "test", "surname", "myEmail3@mail.com", null, null, List.of(Role.RECIPIENT));

    when(teamQueryService.getMembersOfScopedTeamWithRoleIn(eq(CONSULTEE), any(), eq(Set.of(Role.RECIPIENT)))).thenReturn(List.of(teamMember1, teamMember2));

    var consulteeGroupDetail = new ConsulteeGroupDetail();
    consulteeGroupDetail.setName("my group");
    consulteeGroupDetail.setConsulteeGroup(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consultationRequest.getConsulteeGroup())).thenReturn(consulteeGroupDetail);

    when(clock.instant()).thenReturn(Instant.now());

    withdrawConsultationService.withdrawConsultationRequest(consultationRequest, authenticatedUserAccount);

    //Test process termination and marking request as withdrawn
    verify(camundaWorkflowService).deleteProcessAndTask(
        new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION));

    verify(consultationRequestService).saveConsultationRequest(consultationRequestArgumentCaptor.capture());
    assertThat(consultationRequestArgumentCaptor.getValue().getStatus()).isEqualTo(ConsultationRequestStatus.WITHDRAWN);
    assertThat(consultationRequestArgumentCaptor.getValue().getEndedByPersonId()).isEqualTo(authenticatedUserAccount.getLinkedPerson().getId().asInt());

    //Test email templates and recipient email addresses
    ArgumentCaptor<ConsultationWithdrawnEmailProps> expectedEmailProps = ArgumentCaptor.forClass(ConsultationWithdrawnEmailProps.class);
    ArgumentCaptor<EmailRecipient> expectedRecipient = ArgumentCaptor.forClass(EmailRecipient.class);
    verify(emailService, times(2)).sendEmail(expectedEmailProps.capture(), expectedRecipient.capture(),
        eq(consultationRequest.getPwaApplication().getAppReference()));

    List<ConsultationWithdrawnEmailProps> expectedEmailPropsValues = expectedEmailProps.getAllValues();
    assertTrue(expectedEmailPropsValues.contains(new ConsultationWithdrawnEmailProps(
        teamMember1.getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupDetail.getName(),
        authenticatedUserAccount.getLinkedPerson().getFullName())));

    assertTrue(expectedEmailPropsValues.contains(new ConsultationWithdrawnEmailProps(
        teamMember2.getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupDetail.getName(),
        authenticatedUserAccount.getLinkedPerson().getFullName())));

    List<EmailRecipient> expectedEmailRecipients = expectedRecipient.getAllValues();
    assertTrue(expectedEmailRecipients.contains(EmailRecipientWithName.from(teamMember1)));
    assertTrue(expectedEmailRecipients.contains(EmailRecipientWithName.from(teamMember2)));

    verify(workflowAssignmentService).clearAssignments(consultationRequest);
  }


  @Test
  void canWithDrawConsultationRequest_allocation() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.ALLOCATION);
    assertTrue(withdrawConsultationService.canWithDrawConsultationRequest(consultationRequest));
  }

  @Test
  void canWithDrawConsultationRequest_awaitingResponse() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);
    assertTrue(withdrawConsultationService.canWithDrawConsultationRequest(consultationRequest));
  }

  @Test
  void canWithDrawConsultationRequest_withdrawn() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.WITHDRAWN);
    assertFalse(withdrawConsultationService.canWithDrawConsultationRequest(consultationRequest));
  }

  @Test
  void canWithDrawConsultationRequest_responded() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.RESPONDED);
    assertFalse(withdrawConsultationService.canWithDrawConsultationRequest(consultationRequest));
  }



}