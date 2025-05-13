package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneOffset;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.email.CaseLinkService;
import uk.co.ogauthority.pwa.features.email.EmailRecipientWithName;
import uk.co.ogauthority.pwa.features.email.emailproperties.consultations.ConsultationRequestReceivedEmailProps;
import uk.co.ogauthority.pwa.integrations.camunda.external.CamundaWorkflowService;
import uk.co.ogauthority.pwa.integrations.camunda.external.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.govuknotify.EmailService;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupDetail;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationRequestForm;
import uk.co.ogauthority.pwa.repository.consultations.ConsultationRequestRepository;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupDetailService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.consultation.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.teammanagement.OldTeamManagementService;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.view.TeamMemberView;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.consultations.ConsultationRequestValidator;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsultationRequestServiceTest {


  @Mock
  private ConsultationRequestRepository consultationRequestRepository;

  @Mock
  private ConsulteeGroupDetailService consulteeGroupDetailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private OldTeamManagementService teamManagementService;

  @Mock
  private CaseLinkService caseLinkService;

  @Mock
  private ConsultationsStatusViewFactory consultationsStatusViewFactory;

  @Mock
  private EmailService emailService;

  @Mock
  private TeamQueryService teamQueryService;

  @Mock
  private ConsultationRequestValidator consultationRequestValidator;

  @InjectMocks
  private ConsultationRequestService underTest;

  @Captor
  private ArgumentCaptor<ConsultationRequest> consultationRequestArgumentCaptor;


  private PwaApplicationDetail pwaApplicationDetail;

  private AuthenticatedUserAccount authenticatedUserAccount;
  private TeamMemberView teamMember1;
  private TeamMemberView teamMember2;

  @BeforeEach
  void setUp() {

    var webUserAccount = new WebUserAccount(1, new Person(1, "", "", "", ""));
    authenticatedUserAccount = new AuthenticatedUserAccount(webUserAccount, List.of());

    // return the object being saved upon saving
    when(consultationRequestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);

    teamMember1 = new TeamMemberView(1L, "Mr.", "test", "surname", "myEmail1@mail.com", null, null, List.of(Role.RECIPIENT));
    teamMember2 = new TeamMemberView(2L, "Mr.", "test", "surname", "myEmail3@mail.com", null, null, List.of(Role.RECIPIENT));

  }


  @Test
  void saveEntitiesUsingForm_consulteeGroupSelected() {
    var form = new ConsultationRequestForm();
    form.getConsulteeGroupSelection().put("1", "true");
    form.setDaysToRespond(22);

    var groupDetail = new ConsulteeGroupDetail();
    groupDetail.setName("My Group");
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    groupDetail.setConsulteeGroup(consulteeGroup);
    when(consulteeGroupDetailService.getConsulteeGroupDetailById(1)).thenReturn(groupDetail);

    when(consulteeGroupDetailService.getConsulteeGroupDetailByGroupAndTipFlagIsTrue(consulteeGroup)).thenReturn(groupDetail);
    when(teamQueryService.getMembersOfScopedTeamWithRoleIn(eq(TeamType.CONSULTEE), any(), eq(Set.of(Role.RECIPIENT))))
        .thenReturn(List.of(teamMember1, teamMember2));

    //consultation request assertions
    underTest.saveEntitiesAndStartWorkflow(form, pwaApplicationDetail, authenticatedUserAccount);

    verify(consultationRequestRepository).save(consultationRequestArgumentCaptor.capture());

    assertThat(consultationRequestArgumentCaptor.getValue().getConsulteeGroup().getId()).isEqualTo(1);
    var expectedDeadline = Instant.now().plus(Period.ofDays(form.getDaysToRespond()));
    assertThat(consultationRequestArgumentCaptor.getValue().getDeadlineDate().atZone(ZoneOffset.UTC).getDayOfYear()).isEqualTo(expectedDeadline.atZone(ZoneOffset.UTC).getDayOfYear());
    assertThat(consultationRequestArgumentCaptor.getValue().getStatus()).isEqualTo(
        ConsultationRequestStatus.ALLOCATION);

    var dueDate = consultationRequestArgumentCaptor.getValue().getDeadlineDate();

    //email assertions
    ArgumentCaptor<ConsultationRequestReceivedEmailProps> expectedEmailProps = ArgumentCaptor.forClass(ConsultationRequestReceivedEmailProps.class);
    ArgumentCaptor<EmailRecipient> expectedRecipient = ArgumentCaptor.forClass(EmailRecipient.class);
    verify(emailService, times(2)).sendEmail(expectedEmailProps.capture(), expectedRecipient.capture(),
        eq(pwaApplicationDetail.getPwaApplication().getAppReference()));

    var caseManagementLink = caseLinkService.generateCaseManagementLink(pwaApplicationDetail.getPwaApplication());
    List<ConsultationRequestReceivedEmailProps> expectedEmailPropsValues = expectedEmailProps.getAllValues();
    assertTrue(expectedEmailPropsValues.contains(new ConsultationRequestReceivedEmailProps(
        teamMember1.getFullName(),
        pwaApplicationDetail.getPwaApplication().getAppReference(),
        groupDetail.getName(),
        DateUtils.formatDate(dueDate),
        caseManagementLink)));

    assertTrue(expectedEmailPropsValues.contains(new ConsultationRequestReceivedEmailProps(
        teamMember2.getFullName(),
        pwaApplicationDetail.getPwaApplication().getAppReference(),
        groupDetail.getName(),
        DateUtils.formatDate(dueDate),
        caseManagementLink)));

    List<EmailRecipient> expectedToEmailRecipients = expectedRecipient.getAllValues();
    assertTrue(expectedToEmailRecipients.contains(EmailRecipientWithName.from(teamMember1)));
    assertTrue(expectedToEmailRecipients.contains(EmailRecipientWithName.from(teamMember2)));

  }


  @Test
  void getConsultationRecipients() {

    var consultationRequest = new ConsultationRequest();
    var consulteeGroup = new ConsulteeGroup();
    consultationRequest.setConsulteeGroup(consulteeGroup);

    when(teamQueryService.getMembersOfScopedTeamWithRoleIn(eq(TeamType.CONSULTEE), any(), eq(Set.of(Role.RECIPIENT)))).thenReturn(List.of(
        teamMember1, teamMember2));

    List<EmailRecipientWithName> recipients = underTest.getConsultationRecipients(consultationRequest);
    assertThat(recipients).hasSize(2);
    assertThat(recipients.get(0)).isEqualTo(EmailRecipientWithName.from(teamMember1));
    assertThat(recipients.get(1)).isEqualTo(EmailRecipientWithName.from(teamMember2));
  }

  @Test
  void getAssignedResponderForConsultation_responderExists() {

    var assignedResponderPersonId = new PersonId(1);
        var consultationRequest = new ConsultationRequest();
    when(camundaWorkflowService.getAssignedPersonId(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.RESPONSE)))
        .thenReturn(Optional.of(assignedResponderPersonId));

    var responderPerson = PersonTestUtil.createPersonFrom(assignedResponderPersonId, "email");
    when(teamManagementService.getPerson(assignedResponderPersonId.asInt())).thenReturn(responderPerson);

    var responder = underTest.getAssignedResponderForConsultation(consultationRequest);
    assertThat(responder).isEqualTo(responderPerson);
  }

  @Test
  void getAssignedResponderForConsultation_noAssignedResponder() {
    var responder = underTest.getAssignedResponderForConsultation(new ConsultationRequest());
    assertThat(responder).isNull();
  }


  @Test
  void validate_valid() {
    doCallRealMethod().when(consultationRequestValidator).validate(any(), any(), any());

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
    underTest.validate(form, bindingResult, pwaApplicationDetail.getPwaApplication());
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_invalid() {
    doCallRealMethod().when(consultationRequestValidator).validate(any(), any(), any());

    var form = new ConsultationRequestForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    underTest.validate(form, bindingResult, pwaApplicationDetail.getPwaApplication());
    assertTrue(bindingResult.hasErrors());
  }


  @Test
  void isConsultationRequestOpen() {
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    underTest.isConsultationRequestOpen(consulteeGroup, pwaApplicationDetail.getPwaApplication());
    verify(consultationRequestRepository).findByConsulteeGroupAndPwaApplicationAndStatusNotIn(
        consulteeGroup, pwaApplicationDetail.getPwaApplication(), Set.of(ConsultationRequestStatus.RESPONDED, ConsultationRequestStatus.WITHDRAWN));
  }

  @Test
  void getGroupDetailsForConsulteeGroups() {

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

    var groupDetailMap = underTest.getGroupDetailsForConsulteeGroups(List.of(consultationRequest1, consultationRequest2));
    assertThat(groupDetailMap.get(consulteeGroup1)).isEqualTo(consulteeGroupDetail1);
    assertThat(groupDetailMap.get(consulteeGroup2)).isEqualTo(consulteeGroupDetail2);
  }

  @Test
  void getAllRequestsByAppAndGroupRespondedOnly() {
    var consulteeGroup = new ConsulteeGroup();
    consulteeGroup.setId(1);
    underTest.getAllRequestsByAppAndGroupRespondedOnly(pwaApplicationDetail.getPwaApplication(), consulteeGroup);
    verify(consultationRequestRepository).findByConsulteeGroupAndPwaApplicationAndStatus(
        consulteeGroup, pwaApplicationDetail.getPwaApplication(), ConsultationRequestStatus.RESPONDED);
  }

}