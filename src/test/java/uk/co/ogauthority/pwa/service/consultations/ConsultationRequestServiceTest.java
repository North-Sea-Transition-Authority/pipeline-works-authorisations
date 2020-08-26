package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import org.springframework.validation.BeanPropertyBindingResult;
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
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ConsultationWithdrawnEmailProps;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidator;


@RunWith(MockitoJUnitRunner.class)
public class ConsultationRequestServiceTest {

  private ConsultationRequestService consultationRequestService;

  @Mock
  private ConsultationRequestRepository consultationRequestRepository;
  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;
  @Mock
  CamundaWorkflowService camundaWorkflowService;
  @Mock
  private TeamManagementService teamManagementService;
  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;
  @Mock
  private NotifyService notifyService;


  @Captor
  private ArgumentCaptor<ConsultationRequest> consultationRequestArgumentCaptor;


  private ConsultationRequestValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount authenticatedUserAccount;

  private Clock clock;



  @Before
  public void setUp() {
    clock = Clock.fixed(Instant.parse(Instant.now().toString()), ZoneId.of("UTC"));
    var webUserAccount = new WebUserAccount(1, new Person(1, "", "", "", ""));
    authenticatedUserAccount = new AuthenticatedUserAccount(webUserAccount, List.of());
    validator = new ConsultationRequestValidator();
    consultationRequestService = new ConsultationRequestService(consulteeGroupDetailService, consultationRequestRepository, validator, camundaWorkflowService,
        teamManagementService, consulteeGroupTeamService, notifyService, clock);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }


  @Test
  public void saveEntitiesUsingForm_consulteeGroupSelected() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(22);

    var groupDetail = new ConsulteeGroupDetail();
    groupDetail.setName("My Group");
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    groupDetail.setConsulteeGroup(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailById(1)).thenReturn(groupDetail);

    consultationRequestService.saveEntitiesAndStartWorkflow(form, pwaApplicationDetail, authenticatedUserAccount);
    verify(consultationRequestRepository, times(1)).save(consultationRequestArgumentCaptor.capture());

    assertThat(consultationRequestArgumentCaptor.getValue().getConsulteeGroup().getId()).isEqualTo(1);
    var expectedDeadline = Instant.now().plus(Period.ofDays(form.getDaysToRespond()));
    assertThat(consultationRequestArgumentCaptor.getValue().getDeadlineDate().atZone(ZoneOffset.UTC).getDayOfYear()).isEqualTo(expectedDeadline.atZone(ZoneOffset.UTC).getDayOfYear());
    assertThat(consultationRequestArgumentCaptor.getValue().getStatus()).isEqualTo(
        ConsultationRequestStatus.ALLOCATION);
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

    consultationRequestService.withdrawConsultationRequest(consultationRequest, authenticatedUserAccount);

    //Test process termination and marking request as withdrawn
    verify(camundaWorkflowService, times(1)).deleteProcessAndTask(workflowTaskInstance);

    verify(consultationRequestRepository, times(1)).save(consultationRequestArgumentCaptor.capture());
    assertThat(consultationRequestArgumentCaptor.getValue().getStatus()).isEqualTo(ConsultationRequestStatus.WITHDRAWN);
    assertThat(consultationRequestArgumentCaptor.getValue().getEndedByPersonId()).isEqualTo(authenticatedUserAccount.getLinkedPerson().getId().asInt());

    //Test email template and recipient email address
    var expectedEmailProps = new ConsultationWithdrawnEmailProps(
        respondingPerson.getFullName(),
        consultationRequest.getPwaApplication().getAppReference(),
        consulteeGroupDetail.getName(),
        authenticatedUserAccount.getLinkedPerson().getFullName());
    verify(notifyService, times(1)).sendEmail(expectedEmailProps, respondingPerson.getEmailAddress());

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

    consultationRequestService.withdrawConsultationRequest(consultationRequest, authenticatedUserAccount);

    //Test process termination and marking request as withdrawn
    verify(camundaWorkflowService, times(1)).deleteProcessAndTask(
        new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION));

    verify(consultationRequestRepository, times(1)).save(consultationRequestArgumentCaptor.capture());
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
  }




  @Test
  public void validate_valid() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(22);

    var groupDetail = new ConsulteeGroupDetail();
    groupDetail.setName("My Group");
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    groupDetail.setConsulteeGroup(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailById(1)).thenReturn(groupDetail);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    consultationRequestService.validate(form, bindingResult, pwaApplicationDetail.getPwaApplication());
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  public void validate_invalid() {
    var form = new ConsultationRequestForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    consultationRequestService.validate(form, bindingResult, pwaApplicationDetail.getPwaApplication());
    assertTrue(bindingResult.hasErrors());
  }


  @Test
  public void isConsultationRequestOpen() {
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    consultationRequestService.isConsultationRequestOpen(consulteeGroup, pwaApplicationDetail.getPwaApplication());
    verify(consultationRequestRepository, times(1)).findByConsulteeGroupAndPwaApplicationAndStatusNotIn(
        consulteeGroup, pwaApplicationDetail.getPwaApplication(), List.of(ConsultationRequestStatus.RESPONDED, ConsultationRequestStatus.WITHDRAWN));
  }


  @Test
  public void canWithDrawConsultationRequest_allocation() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.ALLOCATION);
    assertTrue(consultationRequestService.canWithDrawConsultationRequest(consultationRequest));
  }

  @Test
  public void canWithDrawConsultationRequest_awaitingResponse() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);
    assertTrue(consultationRequestService.canWithDrawConsultationRequest(consultationRequest));
  }

  @Test
  public void canWithDrawConsultationRequest_withdrawn() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.WITHDRAWN);
    assertFalse(consultationRequestService.canWithDrawConsultationRequest(consultationRequest));
  }

  @Test
  public void canWithDrawConsultationRequest_responded() {
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setStatus(ConsultationRequestStatus.RESPONDED);
    assertFalse(consultationRequestService.canWithDrawConsultationRequest(consultationRequest));
  }


  @Test
  public void getAllRequestsByAppAndGroupRespondedOnly() {
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    consultationRequestService.getAllRequestsByAppAndGroupRespondedOnly(pwaApplicationDetail.getPwaApplication(), consulteeGroup);
    verify(consultationRequestRepository, times(1)).findByConsulteeGroupAndPwaApplicationAndStatus(
        consulteeGroup, pwaApplicationDetail.getPwaApplication(), ConsultationRequestStatus.RESPONDED);
  }







}