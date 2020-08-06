package uk.co.ogauthority.pwa.service.workarea;

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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.workflow.GenericWorkflowSubject;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.AssignedTaskInstance;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@RunWith(MockitoJUnitRunner.class)
public class WorkAreaServiceTest {

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private ApplicationWorkAreaPageService applicationWorkAreaPageService;

  private WorkAreaService workAreaService;

  private AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(1, new Person()), List.of());

  @Before
  public void setUp() {

    this.workAreaService = new WorkAreaService(camundaWorkflowService, applicationWorkAreaPageService);

  }

  @Test
  public void getWorkAreaResultPage_applicationsTab_resultsExist() {

    var appWorkflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    var appWorkflowSubject2 = new GenericWorkflowSubject(2, WorkflowType.PWA_APPLICATION);
    var consultationWorkflowSubject = new GenericWorkflowSubject(3, WorkflowType.PWA_APPLICATION_CONSULTATION);

    when(camundaWorkflowService.getAssignedTasks(authenticatedUserAccount.getLinkedPerson())).thenReturn(Set.of(
      new AssignedTaskInstance(new WorkflowTaskInstance(appWorkflowSubject, PwaApplicationWorkflowTask.PREPARE_APPLICATION), authenticatedUserAccount.getLinkedPerson()),
      new AssignedTaskInstance(new WorkflowTaskInstance(appWorkflowSubject2, PwaApplicationWorkflowTask.PREPARE_APPLICATION), authenticatedUserAccount.getLinkedPerson()),
      new AssignedTaskInstance(new WorkflowTaskInstance(consultationWorkflowSubject, PwaApplicationConsultationWorkflowTask.ALLOCATION), authenticatedUserAccount.getLinkedPerson())
    ));

    workAreaService.getWorkAreaResultPage(authenticatedUserAccount, WorkAreaTab.OPEN_APPLICATIONS, 0);

    verify(applicationWorkAreaPageService, times(1)).getPageView(eq(authenticatedUserAccount), eq(Set.of(1,2)), eq(0));

  }

  @Test
  public void getWorkAreaResultPage_applicationsTab_noAssignedTasks() {

    when(camundaWorkflowService.getAssignedTasks(authenticatedUserAccount.getLinkedPerson())).thenReturn(Set.of());

    workAreaService.getWorkAreaResultPage(authenticatedUserAccount, WorkAreaTab.OPEN_APPLICATIONS, 1);

    verify(applicationWorkAreaPageService, times(1)).getPageView(eq(authenticatedUserAccount), eq(Set.of()), eq(1));

  }

}
