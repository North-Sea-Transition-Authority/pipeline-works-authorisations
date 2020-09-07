package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupMemberRole;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroupTeamMember;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.model.notify.emailproperties.ConsultationAssignedToYouEmailProps;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.notify.NotifyService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidationHints;
import uk.co.ogauthority.pwa.validators.consultations.AssignResponderValidator;


@RunWith(MockitoJUnitRunner.class)
public class AssignResponderServiceTest {

  private AssignResponderService assignResponderService;

  @Mock
  private WorkflowAssignmentService workflowAssignmentService;

  @Mock
  private ConsulteeGroupTeamService consulteeGroupTeamService;

  @Mock
  private AssignResponderValidator validator;

  @Mock
  private TeamManagementService teamManagementService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private ConsultationRequestService consultationRequestService;

  @Mock
  private NotifyService notifyService;

  @Captor
  private ArgumentCaptor<ConsultationAssignedToYouEmailProps> emailPropsCaptor;

  @Before
  public void setUp() {
    assignResponderService = new AssignResponderService(workflowAssignmentService, validator, consulteeGroupTeamService,
        teamManagementService, camundaWorkflowService, consultationRequestService, notifyService);
  }

  @Test
  public void getAllRespondersForRequest() {
    ConsultationRequest consultationRequest = new ConsultationRequest();
    var expectedResponder1 = new Person(1, "", "Smith", "", "");
    var expectedResponder2 = new Person(2, "", "Berry", "", "");
    when(workflowAssignmentService.getAssignmentCandidates(consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE)).thenReturn(Set.of(expectedResponder1, expectedResponder2));

    List<Person> responders = assignResponderService.getAllRespondersForRequest(consultationRequest);
    assertThat(responders.get(0)).isEqualTo(expectedResponder2);
    assertThat(responders.get(1)).isEqualTo(expectedResponder1);
  }

  @Test
  public void assignUserAndCompleteWorkflow_assignToDifferentUser_emailSent() {

    ConsultationRequest consultationRequest = new ConsultationRequest();
    var app = new PwaApplication();
    app.setAppReference("PA/2/2");
    consultationRequest.setPwaApplication(app);
    var deadline = Instant.now().plusSeconds(86400);
    consultationRequest.setDeadlineDate(deadline);

    var form = new AssignResponderForm();
    form.setResponderPersonId(2);

    var assigningUser = new WebUserAccount(1, new Person(1, "m", "assign", "assign@assign.com", null));

    var responderPerson = new Person(2, "fore", "sur", "fore@sur.com", null);
    when(teamManagementService.getPerson(2)).thenReturn(responderPerson);

    assignResponderService.assignUserAndCompleteWorkflow(form, consultationRequest, assigningUser);

    verify(camundaWorkflowService, times(1)).completeTask(eq(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION)));

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        responderPerson,
        assigningUser.getLinkedPerson()
    );

    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq(responderPerson.getEmailAddress()));

    var emailProps = emailPropsCaptor.getValue();

    assertThat(emailProps.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("RECIPIENT_FULL_NAME", responderPerson.getFullName()),
            tuple("APPLICATION_REFERENCE", app.getAppReference()),
            tuple("ASSIGNER_FULL_NAME", assigningUser.getLinkedPerson().getFullName()),
            tuple("DUE_DATE", DateUtils.formatDateTime(deadline))
        );

    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.AWAITING_RESPONSE);
  }

  @Test
  public void assignUserAndCompleteWorkflow_assigningToSelf_noEmailSent() {

    ConsultationRequest consultationRequest = new ConsultationRequest();
    var app = new PwaApplication();
    app.setAppReference("PA/2/2");
    consultationRequest.setPwaApplication(app);
    var deadline = Instant.now().plusSeconds(86400);
    consultationRequest.setDeadlineDate(deadline);

    var form = new AssignResponderForm();
    form.setResponderPersonId(1);

    var assigningUser = new WebUserAccount(1, new Person(1, "m", "assign", "assign@assign.com", null));

    when(teamManagementService.getPerson(1)).thenReturn(assigningUser.getLinkedPerson());

    assignResponderService.assignUserAndCompleteWorkflow(form, consultationRequest, assigningUser);

    verify(camundaWorkflowService, times(1)).completeTask(eq(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION)));

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        assigningUser.getLinkedPerson(),
        assigningUser.getLinkedPerson()
    );

    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);

    verifyNoInteractions(notifyService);

    assertThat(consultationRequest.getStatus()).isEqualTo(ConsultationRequestStatus.AWAITING_RESPONSE);

  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void assignUserAndCompleteWorkflow_personNotFound() {

    when(teamManagementService.getPerson(5)).thenThrow(new PwaEntityNotFoundException(""));

    var form = new AssignResponderForm();
    form.setResponderPersonId(5);

    assignResponderService.assignUserAndCompleteWorkflow(form, new ConsultationRequest(), new WebUserAccount());
  }

  @Test
  public void reassignUser_assignToDifferentUser_emailSent() {
    ConsultationRequest consultationRequest = new ConsultationRequest();
    var app = new PwaApplication();
    app.setAppReference("PA/2/2");
    consultationRequest.setPwaApplication(app);
    var deadline = Instant.now().plusSeconds(86400);
    consultationRequest.setDeadlineDate(deadline);
    consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);

    var form = new AssignResponderForm();
    form.setResponderPersonId(2);

    var assigningUser = new WebUserAccount(1, new Person(1, "m", "assign", "assign@assign.com", null));

    var responderPerson = new Person(2, "fore", "sur", "fore@sur.com", null);
    when(teamManagementService.getPerson(2)).thenReturn(responderPerson);

    assignResponderService.reassignUser(form, consultationRequest, assigningUser);

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        responderPerson,
        assigningUser.getLinkedPerson()
    );

    verify(notifyService, times(1)).sendEmail(emailPropsCaptor.capture(), eq(responderPerson.getEmailAddress()));

    var emailProps = emailPropsCaptor.getValue();

    assertThat(emailProps.getEmailPersonalisation().entrySet())
        .extracting(Map.Entry::getKey, Map.Entry::getValue)
        .contains(
            tuple("RECIPIENT_FULL_NAME", responderPerson.getFullName()),
            tuple("APPLICATION_REFERENCE", app.getAppReference()),
            tuple("ASSIGNER_FULL_NAME", assigningUser.getLinkedPerson().getFullName()),
            tuple("DUE_DATE", DateUtils.formatDateTime(deadline))
        );
  }

  @Test
  public void reassignUser_assigningToSelf_noEmailSent() {

    ConsultationRequest consultationRequest = new ConsultationRequest();
    var app = new PwaApplication();
    app.setAppReference("PA/2/2");
    consultationRequest.setPwaApplication(app);
    var deadline = Instant.now().plusSeconds(86400);
    consultationRequest.setDeadlineDate(deadline);
    consultationRequest.setStatus(ConsultationRequestStatus.AWAITING_RESPONSE);

    var form = new AssignResponderForm();
    form.setResponderPersonId(1);

    var assigningUser = new WebUserAccount(1, new Person(1, "m", "assign", "assign@assign.com", null));

    when(teamManagementService.getPerson(1)).thenReturn(assigningUser.getLinkedPerson());

    assignResponderService.reassignUser(form, consultationRequest, assigningUser);

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        assigningUser.getLinkedPerson(),
        assigningUser.getLinkedPerson()
    );

    verifyNoInteractions(notifyService);
  }


  @Test
  public void validate() {
    var form = new AssignResponderForm();
    assignResponderService.validate(form, new BeanPropertyBindingResult(form, "form"), new ConsultationRequest());
    verify(validator, times(1)).validate(any(AssignResponderForm.class), any(BeanPropertyBindingResult.class), any(
        AssignResponderValidationHints.class));
  }

  @Test
  public void isUserMemberOfRequestGroup_valid() {
    var usersGroup = new ConsulteeGroup();
    usersGroup.setId(1);

    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember();
    consulteeGroupTeamMember.setConsulteeGroup(usersGroup);
    consulteeGroupTeamMember.setRoles(Set.of(ConsulteeGroupMemberRole.RESPONDER));

    var user = new WebUserAccount(1);
    var consultationRequest = new ConsultationRequest();
    consultationRequest.setConsulteeGroup(usersGroup);

    when(consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())).thenReturn(List.of(consulteeGroupTeamMember));
    boolean isMemberOfRequestGroup = assignResponderService.isUserMemberOfRequestGroup(user, consultationRequest);

    assertTrue(isMemberOfRequestGroup);
  }

  @Test
  public void isUserMemberOfRequestGroup_invalid() {
    var usersGroup = new ConsulteeGroup();
    usersGroup.setId(1);

    var consulteeGroupTeamMember = new ConsulteeGroupTeamMember();
    consulteeGroupTeamMember.setConsulteeGroup(usersGroup);
    consulteeGroupTeamMember.setRoles(Set.of(ConsulteeGroupMemberRole.ACCESS_MANAGER));

    var user = new WebUserAccount(1);
    var consultationRequest = new ConsultationRequest();
    var requestGroup = new ConsulteeGroup();
    requestGroup.setId(2);
    consultationRequest.setConsulteeGroup(requestGroup);

    when(consulteeGroupTeamService.getTeamMembersByPerson(user.getLinkedPerson())).thenReturn(List.of(consulteeGroupTeamMember));
    boolean isMemberOfRequestGroup = assignResponderService.isUserMemberOfRequestGroup(user, consultationRequest);

    assertFalse(isMemberOfRequestGroup);
  }

  @Test
  public void canShowInTaskList_hasPermission() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(PwaAppProcessingPermission.ASSIGN_RESPONDER), null);

    boolean canShow = assignResponderService.canShowInTaskList(processingContext);

    assertThat(canShow).isTrue();

  }

  @Test
  public void canShowInTaskList_noPermission() {

    var processingContext = new PwaAppProcessingContext(null, null, Set.of(), null);

    boolean canShow = assignResponderService.canShowInTaskList(processingContext);

    assertThat(canShow).isFalse();

  }


}

