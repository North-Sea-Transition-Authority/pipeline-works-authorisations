package uk.co.ogauthority.pwa.service.workflow.assignment;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@RunWith(MockitoJUnitRunner.class)
public class WorkflowAssignmentServiceTest {

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private AssignmentAuditService assignmentAuditService;

  @Mock
  private TeamService teamService;

  private WorkflowAssignmentService workflowAssignmentService;

  private PwaTeamMember caseOfficerTeamMember;
  private PwaTeamMember notCaseOfficer;

  @Before
  public void setUp() {

    var caseOfficerPerson = new Person(1, null, null, null, null);
    caseOfficerTeamMember = new PwaTeamMember(teamService.getRegulatorTeam(), caseOfficerPerson, Set.of(new PwaRole("CASE_OFFICER", null, null, 10)));

    var notCaseOfficerPerson = new Person(2, null, null, null, null);
    notCaseOfficer = new PwaTeamMember(teamService.getRegulatorTeam(), notCaseOfficerPerson, Set.of(new PwaRole("PWA_MANAGER", null, null, 20)));

    when(teamService.getTeamMembers(teamService.getRegulatorTeam())).thenReturn(List.of(caseOfficerTeamMember, notCaseOfficer));

    workflowAssignmentService = new WorkflowAssignmentService(camundaWorkflowService, assignmentAuditService, teamService);

  }

  @Test
  public void getAssignmentCandidates_caseOfficer() {

    assertThat(workflowAssignmentService.getAssignmentCandidates(PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)).containsOnly(caseOfficerTeamMember.getPerson());

  }

  @Test
  public void getAssignmentCandidates_noAssignment() {

    assertThat(workflowAssignmentService.getAssignmentCandidates(PwaApplicationWorkflowTask.PREPARE_APPLICATION)).isEmpty();

  }

  @Test
  public void assign_success() {

    var app = new PwaApplication();

    workflowAssignmentService.assign(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, caseOfficerTeamMember.getPerson(), notCaseOfficer.getPerson());

    verify(camundaWorkflowService, times(1)).assignTaskToUser(eq(new WorkflowTaskInstance(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW)), eq(caseOfficerTeamMember.getPerson()));
    verify(assignmentAuditService, times(1)).auditAssignment(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, caseOfficerTeamMember.getPerson(), notCaseOfficer.getPerson());

  }

  @Test(expected = WorkflowAssignmentException.class)
  public void assign_invalid() {

    var app = new PwaApplication();

    workflowAssignmentService.assign(app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, notCaseOfficer.getPerson(), caseOfficerTeamMember.getPerson());

  }

}
