package uk.co.ogauthority.pwa.service.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.co.ogauthority.pwa.model.form.consultation.AssignResponderForm;
import uk.co.ogauthority.pwa.service.appprocessing.consultations.consultees.ConsulteeGroupTeamService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
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


  @Before
  public void setUp() {
    assignResponderService = new AssignResponderService(workflowAssignmentService, validator, consulteeGroupTeamService,
        teamManagementService, camundaWorkflowService, consultationRequestService);
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
  public void assignUserAndCompleteWorkflow() {

    ConsultationRequest consultationRequest = new ConsultationRequest();
    var form = new AssignResponderForm();
    form.setResponderPersonId(1);
    var user = new WebUserAccount(1);

    var person = new Person();
    when(teamManagementService.getPerson(1)).thenReturn(person);

    assignResponderService.assignUserAndCompleteWorkflow(form, consultationRequest, user);

    verify(camundaWorkflowService, times(1)).completeTask(eq(new WorkflowTaskInstance(consultationRequest, PwaApplicationConsultationWorkflowTask.ALLOCATION)));

    verify(workflowAssignmentService, times(1)).assign(
        consultationRequest,
        PwaApplicationConsultationWorkflowTask.RESPONSE,
        person,
        user.getLinkedPerson()
    );

    verify(consultationRequestService, times(1)).saveConsultationRequest(consultationRequest);

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
  public void validate() {
    var form = new AssignResponderForm();
    assignResponderService.validate(form, new BeanPropertyBindingResult(form, "form"), new ConsultationRequest());
    verify(validator, times(1)).validate(any(AssignResponderForm.class), any(BeanPropertyBindingResult.class), any(
        AssignResponderValidationHints.class));
  }


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


}

