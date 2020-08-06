package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationConsultationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.workarea.applications.ApplicationWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationRequestWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.AssignedTaskInstance;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;
import uk.co.ogauthority.pwa.testutils.WorkAreaTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class WorkAreaServiceTest {

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  @Mock
  private ApplicationWorkAreaPageService applicationWorkAreaPageService;

  @Mock
  private ConsultationWorkAreaPageService consultationWorkAreaPageService;

  private WorkAreaService workAreaService;

  private PageView<PwaApplicationWorkAreaItem> appPageView;
  private PageView<ConsultationRequestWorkAreaItem> consultationPageView;

  private AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(1, new Person()), List.of());

  @Before
  public void setUp() {

    this.workAreaService = new WorkAreaService(camundaWorkflowService, applicationWorkAreaPageService, consultationWorkAreaPageService);

    appPageView = WorkAreaTestUtils.setUpFakeAppPageView(0);
    when(applicationWorkAreaPageService.getPageView(any(), any(), anyInt())).thenReturn(appPageView);

    consultationPageView = WorkAreaTestUtils.setUpFakeConsultationPageView(0);
    when(consultationWorkAreaPageService.getPageView(any(), any(), anyInt())).thenReturn(consultationPageView);

  }

  @Test
  public void getWorkAreaResult_applicationsTab_resultsExist() {

    var appWorkflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
    var appWorkflowSubject2 = new GenericWorkflowSubject(2, WorkflowType.PWA_APPLICATION);
    var consultationWorkflowSubject = new GenericWorkflowSubject(3, WorkflowType.PWA_APPLICATION_CONSULTATION);

    when(camundaWorkflowService.getAssignedTasks(authenticatedUserAccount.getLinkedPerson())).thenReturn(Set.of(
      new AssignedTaskInstance(new WorkflowTaskInstance(appWorkflowSubject, PwaApplicationWorkflowTask.PREPARE_APPLICATION), authenticatedUserAccount.getLinkedPerson()),
      new AssignedTaskInstance(new WorkflowTaskInstance(appWorkflowSubject2, PwaApplicationWorkflowTask.PREPARE_APPLICATION), authenticatedUserAccount.getLinkedPerson()),
      new AssignedTaskInstance(new WorkflowTaskInstance(consultationWorkflowSubject, PwaApplicationConsultationWorkflowTask.ALLOCATION), authenticatedUserAccount.getLinkedPerson())
    ));

    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.OPEN_APPLICATIONS, 0);

    verify(applicationWorkAreaPageService, times(1)).getPageView(eq(authenticatedUserAccount), eq(Set.of(1,2)), eq(0));

    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
    assertThat(workAreaResult.getConsultationsTabPages()).isNull();

  }

  @Test
  public void getWorkAreaResult_applicationsTab_noAssignedTasks() {

    when(camundaWorkflowService.getAssignedTasks(authenticatedUserAccount.getLinkedPerson())).thenReturn(Set.of());

    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.OPEN_APPLICATIONS, 1);

    verify(applicationWorkAreaPageService, times(1)).getPageView(eq(authenticatedUserAccount), eq(Set.of()), eq(1));

    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
    assertThat(workAreaResult.getConsultationsTabPages()).isNull();

  }

  @Test
  public void getWorkAreaResult_consultationsTab_resultsExist() {

    var consultationWorkflowSubject = new GenericWorkflowSubject(3, WorkflowType.PWA_APPLICATION_CONSULTATION);
    var consultationWorkflowSubject2 = new GenericWorkflowSubject(4, WorkflowType.PWA_APPLICATION_CONSULTATION);
    var appWorkflowSubject = new GenericWorkflowSubject(2, WorkflowType.PWA_APPLICATION);
    var appWorkflowSubject2 = new GenericWorkflowSubject(3, WorkflowType.PWA_APPLICATION);

    when(camundaWorkflowService.getAssignedTasks(authenticatedUserAccount.getLinkedPerson())).thenReturn(Set.of(
        new AssignedTaskInstance(new WorkflowTaskInstance(consultationWorkflowSubject, PwaApplicationConsultationWorkflowTask.ALLOCATION), authenticatedUserAccount.getLinkedPerson()),
        new AssignedTaskInstance(new WorkflowTaskInstance(consultationWorkflowSubject2, PwaApplicationConsultationWorkflowTask.ALLOCATION), authenticatedUserAccount.getLinkedPerson()),
        new AssignedTaskInstance(new WorkflowTaskInstance(appWorkflowSubject, PwaApplicationWorkflowTask.PREPARE_APPLICATION), authenticatedUserAccount.getLinkedPerson()),
        new AssignedTaskInstance(new WorkflowTaskInstance(appWorkflowSubject2, PwaApplicationWorkflowTask.PREPARE_APPLICATION), authenticatedUserAccount.getLinkedPerson())
    ));

    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.OPEN_CONSULTATIONS, 0);

    verify(consultationWorkAreaPageService, times(1)).getPageView(eq(authenticatedUserAccount), eq(Set.of(3,4)), eq(0));

    assertThat(workAreaResult.getApplicationsTabPages()).isNull();
    assertThat(workAreaResult.getConsultationsTabPages()).isEqualTo(consultationPageView);

  }

  @Test
  public void getWorkAreaResult_consultationsTab_noAssignedTasks() {

    when(camundaWorkflowService.getAssignedTasks(authenticatedUserAccount.getLinkedPerson())).thenReturn(Set.of());

    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.OPEN_CONSULTATIONS, 1);

    verify(consultationWorkAreaPageService, times(1)).getPageView(eq(authenticatedUserAccount), eq(Set.of()), eq(1));

    assertThat(workAreaResult.getApplicationsTabPages()).isNull();
    assertThat(workAreaResult.getConsultationsTabPages()).isEqualTo(consultationPageView);

  }

}
