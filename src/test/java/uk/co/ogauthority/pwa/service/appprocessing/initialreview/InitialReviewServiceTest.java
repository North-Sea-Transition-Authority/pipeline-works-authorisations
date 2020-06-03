package uk.co.ogauthority.pwa.service.appprocessing.initialreview;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.ActionAlreadyPerformedException;
import uk.co.ogauthority.pwa.exception.WorkflowAssignmentException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.teammanagement.TeamManagementService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.assignment.WorkflowAssignmentService;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@RunWith(MockitoJUnitRunner.class)
public class InitialReviewServiceTest {

  @Mock
  private PwaApplicationDetailService detailService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private WorkflowAssignmentService assignmentService;

  @Mock
  private TeamManagementService teamManagementService;

  private InitialReviewService initialReviewService;

  private PwaApplicationDetail detail;
  private PwaApplication app;
  private WebUserAccount user;

  private Person caseOfficerPerson;

  @Before
  public void setUp() {

    var userPerson = new Person(1, null, null, null, null);
    user = new WebUserAccount(1, userPerson);

    app = new PwaApplication();
    app.setId(1);

    detail = new PwaApplicationDetail();
    detail.setPwaApplication(app);
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    caseOfficerPerson = new Person(555, null, null, null, null);

    when(teamManagementService.getPerson(caseOfficerPerson.getId().asInt())).thenReturn(caseOfficerPerson);

    initialReviewService = new InitialReviewService(detailService, camundaWorkflowService, assignmentService, teamManagementService);

  }

  @Test
  public void acceptApplication_success() {

    initialReviewService.acceptApplication(detail, caseOfficerPerson.getId().asInt(), user);

    verify(detailService, times(1)).setInitialReviewApproved(detail, user);
    verify(camundaWorkflowService, times(1))
        .completeTask(eq(new WorkflowTaskInstance(app, PwaApplicationWorkflowTask.APPLICATION_REVIEW)));

    verify(assignmentService, times(1)).assign(
        app, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW, caseOfficerPerson, user.getLinkedPerson());

  }

  @Test(expected = ActionAlreadyPerformedException.class)
  public void acceptApplication_failed_alreadyAccepted() {

    initialReviewService.acceptApplication(detail, caseOfficerPerson.getId().asInt(), user);

    detail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    initialReviewService.acceptApplication(detail, caseOfficerPerson.getId().asInt(), user);

  }

  @Test(expected = WorkflowAssignmentException.class)
  public void acceptApplication_invalidCaseOfficer() {

    doThrow(new WorkflowAssignmentException("")).when(assignmentService).assign(any(), any(), any(), any());

    initialReviewService.acceptApplication(detail, 999, user);

  }

}
