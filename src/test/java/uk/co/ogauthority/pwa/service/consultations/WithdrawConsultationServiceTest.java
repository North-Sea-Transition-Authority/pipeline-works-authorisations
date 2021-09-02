package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationResponse;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.notify.emailproperties.consultations.ConsultationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class WithdrawConsultationServiceTest {

  private WithdrawConsultationService withdrawConsultationService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;

  @Mock
  CamundaWorkflowService camundaWorkflowService;

  @Mock
  private TeamManagementService teamManagementService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private NotifyService notifyService;

  @Captor
  private ArgumentCaptor<ConsultationRequest> consultationRequestArgumentCaptor;


  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount authenticatedUserAccount;

  private Clock clock;

  @Before
  public void setUp() {

    clock = Clock.fixed(Instant.parse(Instant.now().toString()), ZoneId.of("UTC"));
    var webUserAccount = new WebUserAccount(1, new Person(1, "", "", "", ""));
    authenticatedUserAccount = new AuthenticatedUserAccount(webUserAccount, List.of());

    withdrawConsultationService = new WithdrawConsultationService(
        consulteeGroupDetailService,
        consultationRequestService,
        camundaWorkflowService,
        teamManagementService,
        consulteeGroupTeamService,
        workflowAssignmentService,
        notifyService,
        clock);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);

  }


  @Test
  public void withdrawConsultationRequest_emailResponder() {
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
    when(teamManagementService.getPerson(2)).thenReturn(respondingPerson);

    var workflowTaskInstance = new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE);
    when(camundaWorkflowService.getAssignedPersonId(workflowTaskInstance)).thenReturn(Optional.of(respondingPerson.getId()));



    var consulteeGroupDetail = new ConsulteeGroupDetail();
    consulteeGroupDetail.setName("my group");
    consulteeGroupDetail.setConsulteeGroup(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consultationRequest.getConsulteeGroup())).thenReturn(consulteeGroupDetail);

    withdrawConsultationService.withdrawConsultationRequest(consultationRequest, authenticatedUserAccount);

    //Test process termination and marking request as withdrawn
    verify(camundaWorkflowService, times(1)).deleteProcessAndTask(workflowTaskInstance);

    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequestArgumentCaptor.capture());
    assertThat(consultationRequestArgumentCaptor.getValue().getStatus()).isEqualTo(ConsultationRequestStatus.WITHDRAWN);
    assertThat(consultationRequestArgumentCaptor.getValue().getEndedByPersonId()).isEqualTo(authenticatedUserAccount.getLinkedPerson().getId().asInt());

    //Test email template and recipient email address
    var expectedEmailProps = new ConsultationWithdrawnEmailProps(
        respondingPerson.getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupDetail.getName(),
        authenticatedUserAccount.getLinkedPerson().getFullName());
    verify(notifyService, times(1)).sendEmail(expectedEmailProps, respondingPerson.getEmailAddress());

    verify(workflowAssignmentService).clearAssignments(consultationRequest);
  }


  @Test
  public void withdrawConsultationRequest_emailTeamMembers() {
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);

    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.ALLOCATION);
    consultationRequest.setConsulteeGroup(consulteeGroup);
    consultationRequest.setPwaApplication(pwaApplicationDetail.getPwaApplication());
    consultationRequest.setEndTimestamp(Instant.now(clock));

    ConsulteeGroupTeamMember teamMember1 = new ConsulteeGroupTeamMember(consultationRequest.getConsulteeGroup(),
        new Person(2, "memberFirst1", "memberLast1", "member1@live.com", null),
        Set.of(ConsulteeGroupMemberRole.RECIPIENT));
    ConsulteeGroupTeamMember teamMember2 = new ConsulteeGroupTeamMember(consultationRequest.getConsulteeGroup(),
        new Person(3, "memberFirst2", "memberLast2", "member2@live.com", null),
        Set.of(ConsulteeGroupMemberRole.RECIPIENT));
    ConsulteeGroupTeamMember teamMember3 = new ConsulteeGroupTeamMember(consultationRequest.getConsulteeGroup(),
        new Person(3, "memberFirst3", "memberLast3", "member3@live.com", null),
        Set.of());
    when(consulteeGroupTeamService.getTeamMembersForGroup(consultationRequest.getConsulteeGroup())).thenReturn(List.of(teamMember1, teamMember2, teamMember3));

    var consulteeGroupDetail = new ConsulteeGroupDetail();
    consulteeGroupDetail.setName("my group");
    consulteeGroupDetail.setConsulteeGroup(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consultationRequest.getConsulteeGroup())).thenReturn(consulteeGroupDetail);

    withdrawConsultationService.withdrawConsultationRequest(consultationRequest, authenticatedUserAccount);

    //Test process termination and marking request as withdrawn
    verify(camundaWorkflowService, times(1)).deleteProcessAndTask(
        new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION));

    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequestArgumentCaptor.capture());
    assertThat(consultationRequestArgumentCaptor.getValue().getStatus()).isEqualTo(ConsultationRequestStatus.WITHDRAWN);
    assertThat(consultationRequestArgumentCaptor.getValue().getEndedByPersonId()).isEqualTo(authenticatedUserAccount.getLinkedPerson().getId().asInt());

    //Test email templates and recipient email addresses
    ArgumentCaptor<ConsultationWithdrawnEmailProps> expectedEmailProps = ArgumentCaptor.forClass(ConsultationWithdrawnEmailProps.class);
    ArgumentCaptor<String> expectedToEmailAddress = ArgumentCaptor.forClass(String.class);
    verify(notifyService, times(2)).sendEmail(expectedEmailProps.capture(), expectedToEmailAddress.capture());

    List<ConsultationWithdrawnEmailProps> expectedEmailPropsValues = expectedEmailProps.getAllValues();
    assertTrue(expectedEmailPropsValues.contains(new ConsultationWithdrawnEmailProps(
        teamMember1.getPerson().getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupDetail.getName(),
        authenticatedUserAccount.getLinkedPerson().getFullName())));

    assertTrue(expectedEmailPropsValues.contains(new ConsultationWithdrawnEmailProps(
        teamMember2.getPerson().getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupDetail.getName(),
        authenticatedUserAccount.getLinkedPerson().getFullName())));

    List<String> expectedToEmailValues = expectedToEmailAddress.getAllValues();
    assertTrue(expectedToEmailValues.contains(teamMember1.getPerson().getEmailAddress()));
    assertTrue(expectedToEmailValues.contains(teamMember2.getPerson().getEmailAddress()));
    assertFalse(expectedToEmailValues.contains(teamMember3.getPerson().getEmailAddress()));

    verify(workflowAssignmentService).clearAssignments(consultationRequest);
  }


  @Test
  public void canWithDrawConsultationRequest_allocation() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.ALLOCATION);
    assertTrue(withdrawConsultationService.canWithDrawConsultationRequest(consultationRequest));
  }

  @Test
  public void canWithDrawConsultationRequest_awaitingResponse() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);
    assertTrue(withdrawConsultationService.canWithDrawConsultationRequest(consultationRequest));
  }

  @Test
  public void canWithDrawConsultationRequest_withdrawn() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.WITHDRAWN);
    assertFalse(withdrawConsultationService.canWithDrawConsultationRequest(consultationRequest));
  }

  @Test
  public void canWithDrawConsultationRequest_responded() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.RESPONDED);
    assertFalse(withdrawConsultationService.canWithDrawConsultationRequest(consultationRequest));
  }



}