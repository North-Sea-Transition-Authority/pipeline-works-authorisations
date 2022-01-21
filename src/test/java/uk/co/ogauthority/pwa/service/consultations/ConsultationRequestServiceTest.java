package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.Period;
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
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationRequestReceivedEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.NotifyService;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidator;


@RunWith(MockitoJUnitRunner.class)
public class ConsultationRequestServiceTest {

  private ConsultationRequestService consultationRequestService;

  @Mock
  private ConsultationRequestRepository consultationRequestRepository;

  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private TeamManagementService teamManagementService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @Mock
  private NotifyService notifyService;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private ConsultationsStatusViewFactory consultationsStatusViewFactory;

  @Captor
  private ArgumentCaptor<ConsultationRequest> consultationRequestArgumentCaptor;

  private ConsultationRequestValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount authenticatedUserAccount;

  @Before
  public void setUp() {

    var webUserAccount = new WebUserAccount(1, new Person(1, "", "", "", ""));
    authenticatedUserAccount = new AuthenticatedUserAccount(webUserAccount, List.of());
    validator = new ConsultationRequestValidator();

    // return the object being saved upon saving
    when(consultationRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    consultationRequestService = new ConsultationRequestService(
        consulteeGroupDetailService,
        consultationRequestRepository,
        validator,
        camundaWorkflowService,
        teamManagementService,
        consulteeGroupTeamService,
        consultationsStatusViewFactory,
        notifyService,
        caseLinkService);

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

    ConsulteeGroupTeamMember teamMember1 = new ConsulteeGroupTeamMember(consulteeGroup,
        new Person(1, "memberFirst1", "memberLast1", "member1@live.com", null),
        Set.of(ConsulteeGroupMemberRole.RECIPIENT));
    ConsulteeGroupTeamMember teamMember2 = new ConsulteeGroupTeamMember(consulteeGroup,
        new Person(2, "memberFirst2", "memberLast2", "member2@live.com", null),
        Set.of(ConsulteeGroupMemberRole.RECIPIENT));
    when(consulteeGroupTeamService.getTeamMembersForGroup(consulteeGroup)).thenReturn(List.of(teamMember1, teamMember2));

    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consulteeGroup)).thenReturn(groupDetail);

    //consultation request assertions
    consultationRequestService.saveEntitiesAndStartWorkflow(form, pwaApplicationDetail, authenticatedUserAccount);
    verify(consultationRequestRepository, times(1)).save(consultationRequestArgumentCaptor.capture());

    assertThat(consultationRequestArgumentCaptor.getValue().getConsulteeGroup().getId()).isEqualTo(1);
    var expectedDeadline = Instant.now().plus(Period.ofDays(form.getDaysToRespond()));
    assertThat(consultationRequestArgumentCaptor.getValue().getDeadlineDate().atZone(ZoneOffset.UTC).getDayOfYear()).isEqualTo(expectedDeadline.atZone(ZoneOffset.UTC).getDayOfYear());
    assertThat(consultationRequestArgumentCaptor.getValue().getStatus()).isEqualTo(
        ConsultationRequestStatus.ALLOCATION);

    var dueDate = consultationRequestArgumentCaptor.getValue().getDeadlineDate();

    //email assertions
    ArgumentCaptor<ConsultationRequestReceivedEmailProps> expectedEmailProps = ArgumentCaptor.forClass(ConsultationRequestReceivedEmailProps.class);
    ArgumentCaptor<String> expectedToEmailAddress = ArgumentCaptor.forClass(String.class);
    verify(notifyService, times(2)).sendEmail(expectedEmailProps.capture(), expectedToEmailAddress.capture());

    var caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());
    List<ConsultationRequestReceivedEmailProps> expectedEmailPropsValues = expectedEmailProps.getAllValues();
    assertTrue(expectedEmailPropsValues.contains(new ConsultationRequestReceivedEmailProps(
        teamMember1.getPerson().getFullName(),
        pwaApplicationDetail.getPwaApplication().getAppReference(),
        groupDetail.getName(),
        DateUtils.formatDate(dueDate),
        caseManagementLink)));

    assertTrue(expectedEmailPropsValues.contains(new ConsultationRequestReceivedEmailProps(
        teamMember2.getPerson().getFullName(),
        pwaApplicationDetail.getPwaApplication().getAppReference(),
        groupDetail.getName(),
        DateUtils.formatDate(dueDate),
        caseManagementLink)));

    List<String> expectedToEmailValues = expectedToEmailAddress.getAllValues();
    assertTrue(expectedToEmailValues.contains(teamMember1.getPerson().getEmailAddress()));
    assertTrue(expectedToEmailValues.contains(teamMember2.getPerson().getEmailAddress()));

  }


  @Test
  public void getConsultationRecipients() {

    var consultationRequest = new ConsultationRequest();
    var consulteeGroup = new ConsulteeGroup();
    consultationRequest.setConsulteeGroup(consulteeGroup);
    var teamMember1 = new ConsulteeGroupTeamMember(
        consulteeGroup, PersonTestUtil.createPersonFrom(new PersonId(1), "myEmail1@mail.com"), Set.of(ConsulteeGroupMemberRole.RECIPIENT));
    var teamMember2 = new ConsulteeGroupTeamMember(
        consulteeGroup, PersonTestUtil.createPersonFrom(new PersonId(2), "myEmail2@mail.com"), Set.of(ConsulteeGroupMemberRole.RESPONDER));
    var teamMember3 = new ConsulteeGroupTeamMember(
        consulteeGroup, PersonTestUtil.createPersonFrom(new PersonId(3), "myEmail3@mail.com"), Set.of(ConsulteeGroupMemberRole.RECIPIENT));
    when(consulteeGroupTeamService.getTeamMembersForGroup(consultationRequest.getConsulteeGroup()))
        .thenReturn(List.of(teamMember1, teamMember2, teamMember3));

    List<Person> recipients = consultationRequestService.getConsultationRecipients(consultationRequest);
    assertThat(recipients).hasSize(2);
    assertThat(recipients.get(0)).isEqualTo(teamMember1.getPerson());
    assertThat(recipients.get(1)).isEqualTo(teamMember3.getPerson());
  }

  @Test
  public void getAssignedResponderForConsultation_responderExists() {

    var assignedResponderPersonId = new PersonId(1);
        var consultationRequest = new ConsultationRequest();
    when(camundaWorkflowService.getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE)))
        .thenReturn(Optional.of(assignedResponderPersonId));

    var responderPerson = PersonTestUtil.createPersonFrom(assignedResponderPersonId, "email");
    when(teamManagementService.getPerson(assignedResponderPersonId.asInt())).thenReturn(responderPerson);

    var responder = consultationRequestService.getAssignedResponderForConsultation(consultationRequest);
    assertThat(responder).isEqualTo(responderPerson);
  }

  @Test
  public void getAssignedResponderForConsultation_noAssignedResponder() {
    var responder = consultationRequestService.getAssignedResponderForConsultation(new ConsultationRequest());
    assertThat(responder).isNull();
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
        consulteeGroup, pwaApplicationDetail.getPwaApplication(), Set.of(ConsultationRequestStatus.RESPONDED, ConsultationRequestStatus.WITHDRAWN));
  }

  @Test
  public void getGroupDetailsForConsulteeGroups() {

    var consulteeGroup1 = new ConsulteeGroup();
    consulteeGroup1.setId(1);
    var consulteeGroupDetail1 = new ConsulteeGroupDetail();
    consulteeGroupDetail1.setConsulteeGroup(consulteeGroup1);
    var consultationRequest1 = new ConsultationRequest();
    consultationRequest1.setConsulteeGroup(consulteeGroup1);

    var consulteeGroup2 = new ConsulteeGroup();
    consulteeGroup2.setId(2);
    var consulteeGroupDetail2 = new ConsulteeGroupDetail();
    consulteeGroupDetail2.setConsulteeGroup(consulteeGroup2);
    var consultationRequest2 = new ConsultationRequest();
    consultationRequest2.setConsulteeGroup(consulteeGroup2);

    when(consulteeGroupDetailService.getAllConsulteeGroupDetailsByGroup(List.of(consulteeGroup1, consulteeGroup2)))
        .thenReturn(List.of(consulteeGroupDetail1, consulteeGroupDetail2));

    var groupDetailMap = consultationRequestService.getGroupDetailsForConsulteeGroups(List.of(consultationRequest1, consultationRequest2));
    assertThat(groupDetailMap.get(consulteeGroup1)).isEqualTo(consulteeGroupDetail1);
    assertThat(groupDetailMap.get(consulteeGroup2)).isEqualTo(consulteeGroupDetail2);
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