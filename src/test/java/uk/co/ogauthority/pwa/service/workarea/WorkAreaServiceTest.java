package uk.co.ogauthority.pwa.service.workarea;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.applications.PwaApplicationWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.asbuilt.AsBuiltWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationRequestWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;
import uk.co.ogauthority.pwa.testutils.WorkAreaTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class WorkAreaServiceTest {

  @Mock
  private AsBuiltWorkAreaPageService asBuiltWorkAreaPageService;

  @Mock
  private ConsultationWorkAreaPageService consultationWorkAreaPageService;

  @Mock
  private AssignmentService assignmentService;

  @Mock
  private ApplicationWorkAreaPageService applicationWorkAreaPageService;

  private WorkAreaService workAreaService;

  private PageView<PwaApplicationWorkAreaItem> appPageView;
  private PageView<ConsultationRequestWorkAreaItem> consultationPageView;
  private PageView<AsBuiltNotificationWorkAreaItem> asBuiltNotificationPageView;

  private AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(1,
      PersonTestUtil.createDefaultPerson()), List.of());

  @Before
  public void setUp() {

    this.workAreaService = new WorkAreaService(
        asBuiltWorkAreaPageService,
        consultationWorkAreaPageService,
        assignmentService,
        applicationWorkAreaPageService);

    appPageView = WorkAreaTestUtils.setUpFakeAppPageView(0);

    consultationPageView = WorkAreaTestUtils.setUpFakeConsultationPageView(0);
    when(consultationWorkAreaPageService.getPageView(any(), any(), anyInt())).thenReturn(consultationPageView);

    asBuiltNotificationPageView = WorkAreaTestUtils.setUpFakeAsBuiltNotificationPageView(0);
    when(asBuiltWorkAreaPageService.getAsBuiltNotificationsPageView(any(), anyInt()))
        .thenReturn(asBuiltNotificationPageView);
  }
// TODO PWA-1172 work out which of these commented out tests can stay
//  @Test
//  public void getWorkAreaResult_regAttentionTab_resultsExist() {
//
//    var appWorkflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
//    var appWorkflowSubject2 = new GenericWorkflowSubject(2, WorkflowType.PWA_APPLICATION);
//    var consultationWorkflowSubject = new GenericWorkflowSubject(3, WorkflowType.PWA_APPLICATION_CONSULTATION);
//
//    when(assignmentService.getAssignmentsForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Map.of(
//        WorkflowType.PWA_APPLICATION, List.of(
//            new Assignment(appWorkflowSubject.getBusinessKey(), appWorkflowSubject.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, authenticatedUserAccount.getLinkedPerson().getId()),
//            new Assignment(appWorkflowSubject2.getBusinessKey(), appWorkflowSubject2.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, authenticatedUserAccount.getLinkedPerson().getId())
//        ),
//        WorkflowType.PWA_APPLICATION_CONSULTATION, List.of(
//            new Assignment(consultationWorkflowSubject.getBusinessKey(), consultationWorkflowSubject.getWorkflowType(), WorkflowAssignment.CONSULTATION_RESPONDER, authenticatedUserAccount.getLinkedPerson().getId())
//        )));
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, 0);
//
//    verify(regulatorWorkAreaPageService, times(1)).getRequiresAttentionPageView(eq(authenticatedUserAccount), eq(Set.of(1,2)), eq(0));
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//
//  }
//
//  @Test
//  public void getWorkAreaResult_regAttentionTab_pwaManagerPrivilege_resultsExist() {
//
//    authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of(
//        PwaUserPrivilege.PWA_MANAGER));
//
//    var pwaApplication = new PwaApplication();
//    pwaApplication.setId(1);
//    var publicNotice = PublicNoticeTestUtil.createInitialPublicNotice(pwaApplication);
//    when(publicNoticeService.getOpenPublicNotices()).thenReturn(List.of(publicNotice));
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, 0);
//
//    verify(regulatorWorkAreaPageService, times(1)).getRequiresAttentionPageView(
//        authenticatedUserAccount, Set.of(publicNotice.getPwaApplication().getId()), 0);
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//
//  }
//
//  @Test
//  public void getWorkAreaResult_regAttentionTab_pwaIndustryPrivilege_resultsExist() {
//
//    authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), List.of(
//        PwaUserPrivilege.PWA_INDUSTRY));
//
//    var appContactAppId = 999;
//    when(industryWorkAreaPageService.getBusinessKeysWhereUserIsAppPreparerAndTaskActive(any(), any()))
//        .thenReturn(Set.of(appContactAppId));
//
//    var pwaApplication = new PwaApplication();
//    pwaApplication.setId(1);
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, 0);
//
//    verify(regulatorWorkAreaPageService, times(1)).getRequiresAttentionPageView(
//        eq(authenticatedUserAccount), eq(Set.of(appContactAppId)), eq(0));
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//
//  }
//
//
//  @Test
//  public void getWorkAreaResult_regAttentionTab_noAssignedTasks() {
//
//    when(assignmentService.getAssignmentsForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Map.of());
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, 1);
//
//    verify(regulatorWorkAreaPageService, times(1)).getRequiresAttentionPageView(eq(authenticatedUserAccount), eq(Set.of()), eq(1));
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//
//  }
//
//  @Test
//  public void getWorkAreaResult_regWaitingTab_resultsExist() {
//
//    var appWorkflowSubject = new GenericWorkflowSubject(1, WorkflowType.PWA_APPLICATION);
//    var appWorkflowSubject2 = new GenericWorkflowSubject(2, WorkflowType.PWA_APPLICATION);
//    var consultationWorkflowSubject = new GenericWorkflowSubject(3, WorkflowType.PWA_APPLICATION_CONSULTATION);
//
//    when(assignmentService.getAssignmentsForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Map.of(
//        WorkflowType.PWA_APPLICATION, List.of(
//            new Assignment(appWorkflowSubject.getBusinessKey(), appWorkflowSubject.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, authenticatedUserAccount.getLinkedPerson().getId()),
//            new Assignment(appWorkflowSubject2.getBusinessKey(), appWorkflowSubject2.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, authenticatedUserAccount.getLinkedPerson().getId())
//        ),
//        WorkflowType.PWA_APPLICATION_CONSULTATION, List.of(
//            new Assignment(consultationWorkflowSubject.getBusinessKey(), consultationWorkflowSubject.getWorkflowType(), WorkflowAssignment.CONSULTATION_RESPONDER, authenticatedUserAccount.getLinkedPerson().getId())
//        )));
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS, 0);
//
//    verify(regulatorWorkAreaPageService, times(1)).getWaitingOnOthersPageView(authenticatedUserAccount, Set.of(1,2), 0);
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//
//  }
//
//  @Test
//  public void getWorkAreaResult_regWaitingTab_noAssignedTasks() {
//
//    when(assignmentService.getAssignmentsForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Map.of());
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS, 1);
//
//    verify(regulatorWorkAreaPageService, times(1)).getWaitingOnOthersPageView(authenticatedUserAccount, Set.of(), 1);
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//
//  }
//
//  @Test
//  public void getWorkAreaResult_openIndustryApplicationsTab_resultsExist() {
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS, 0);
//
//    verify(industryWorkAreaPageService, times(1)).getOpenApplicationsPageView(eq(authenticatedUserAccount), eq(0));
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//
//  }
//
//  @Test
//  public void getWorkAreaResult_openIndustryApplicationsTab_noAssignedTasks() {
//
//    when(assignmentService.getAssignmentsForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Map.of());
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS, 1);
//
//    verify(industryWorkAreaPageService, times(1)).getOpenApplicationsPageView(eq(authenticatedUserAccount), eq(1));
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//
//  }
//
//  @Test
//  public void getWorkAreaResult_industrySubmittedApplicationsTab_resultsExist() {
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS, 0);
//
//    verify(industryWorkAreaPageService, times(1)).getSubmittedApplicationsPageView(eq(authenticatedUserAccount), eq(0));
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//
//  }
//
//  @Test
//  public void getWorkAreaResult_industrySubmittedApplicationsTab_noAssignedTasks() {
//
//    when(assignmentService.getAssignmentsForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Map.of());
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS, 1);
//
//    verify(industryWorkAreaPageService, times(1)).getSubmittedApplicationsPageView(eq(authenticatedUserAccount), eq(1));
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isEqualTo(appPageView);
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//
//  }
//
//  @Test
//  public void getWorkAreaResult_asBuiltNotificationsTab_resultsExist() {
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.AS_BUILT_NOTIFICATIONS, 0);
//
//    verify(asBuiltWorkAreaPageService).getAsBuiltNotificationsPageView(authenticatedUserAccount, 0);
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isNull();
//    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
//    assertThat(workAreaResult.getAsBuiltNotificationTabPages()).isEqualTo(asBuiltNotificationPageView);
//
//  }
//
//  @Test
//  public void getWorkAreaResult_consultationsTab_resultsExist() {
//
//    var consultationWorkflowSubject = new GenericWorkflowSubject(3, WorkflowType.PWA_APPLICATION_CONSULTATION);
//    var consultationWorkflowSubject2 = new GenericWorkflowSubject(4, WorkflowType.PWA_APPLICATION_CONSULTATION);
//    var appWorkflowSubject = new GenericWorkflowSubject(2, WorkflowType.PWA_APPLICATION);
//    var appWorkflowSubject2 = new GenericWorkflowSubject(3, WorkflowType.PWA_APPLICATION);
//
//    when(assignmentService.getAssignmentsForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Map.of(
//        WorkflowType.PWA_APPLICATION, List.of(
//            new Assignment(appWorkflowSubject.getBusinessKey(), appWorkflowSubject.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, authenticatedUserAccount.getLinkedPerson().getId()),
//            new Assignment(appWorkflowSubject2.getBusinessKey(), appWorkflowSubject2.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, authenticatedUserAccount.getLinkedPerson().getId())
//        ),
//        WorkflowType.PWA_APPLICATION_CONSULTATION, List.of(
//            new Assignment(consultationWorkflowSubject.getBusinessKey(), consultationWorkflowSubject.getWorkflowType(), WorkflowAssignment.CONSULTATION_RESPONDER, authenticatedUserAccount.getLinkedPerson().getId()),
//            new Assignment(consultationWorkflowSubject2.getBusinessKey(), consultationWorkflowSubject2.getWorkflowType(), WorkflowAssignment.CONSULTATION_RESPONDER, authenticatedUserAccount.getLinkedPerson().getId())
//        )));
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.OPEN_CONSULTATIONS, 0);
//
//    verify(consultationWorkAreaPageService, times(1)).getPageView(authenticatedUserAccount, Set.of(3,4), 0);
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isNull();
//    assertThat(workAreaResult.getConsultationsTabPages()).isEqualTo(consultationPageView);
//
//  }
//
//  @Test
//  public void getWorkAreaResult_consultationsTab_noAssignedTasks() {
//
//    when(assignmentService.getAssignmentsForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Map.of());
//
//    var workAreaResult = workAreaService.getWorkAreaResult(authenticatedUserAccount, WorkAreaTab.OPEN_CONSULTATIONS, 1);
//
//    verify(consultationWorkAreaPageService, times(1)).getPageView(authenticatedUserAccount, Set.of(), 1);
//
//    assertThat(workAreaResult.getApplicationsTabPages()).isNull();
//    assertThat(workAreaResult.getConsultationsTabPages()).isEqualTo(consultationPageView);
//
//  }

}
