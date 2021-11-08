package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.model.entity.workflow.assignment.Assignment;
import uk.co.ogauthority.pwa.model.workflow.GenericWorkflowSubject;
import uk.co.ogauthority.pwa.mvc.PageView;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationWorkAreaItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationSearchTestUtil;
import uk.co.ogauthority.pwa.service.workarea.applications.ApplicationWorkAreaSort;
import uk.co.ogauthority.pwa.service.workarea.applications.WorkAreaPageServiceTestUtil;
import uk.co.ogauthority.pwa.service.workarea.asbuilt.AsBuiltWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationRequestWorkAreaItem;
import uk.co.ogauthority.pwa.service.workarea.consultations.ConsultationWorkAreaPageService;
import uk.co.ogauthority.pwa.service.workflow.assignment.AssignmentService;
import uk.co.ogauthority.pwa.testutils.WorkAreaTestUtils;
import uk.co.ogauthority.pwa.util.WorkAreaUtils;

@RunWith(MockitoJUnitRunner.class)
public class WorkAreaServiceTest {

  private static final int DEFAULT_PAGE = 0;

  @Mock
  private AsBuiltWorkAreaPageService asBuiltWorkAreaPageService;

  @Mock
  private ConsultationWorkAreaPageService consultationWorkAreaPageService;

  @Mock
  private AssignmentService assignmentService;

  @Mock
  private ApplicationWorkAreaPageService applicationWorkAreaPageService;

  private WorkAreaService workAreaService;

  private Page<WorkAreaApplicationDetailSearchItem> applicationWorkAreaItemPage;

  private PageView<ConsultationRequestWorkAreaItem> consultationPageView;
  private PageView<AsBuiltNotificationWorkAreaItem> asBuiltNotificationPageView;

  private AuthenticatedUserAccount authenticatedUserAccount = new AuthenticatedUserAccount(new WebUserAccount(1,
      PersonTestUtil.createDefaultPerson()), List.of());

  private WorkAreaContext workAreaContext;
  private Pageable regulatorAppTabPageable = WorkAreaUtils.getWorkAreaPageRequest(DEFAULT_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC);
  private Pageable industryAppTabPageable = WorkAreaUtils.getWorkAreaPageRequest(DEFAULT_PAGE, ApplicationWorkAreaSort.SUBMITTED_APP_START_ASC_THEN_DRAFT_APP_START_ASC);

  @Before
  public void setUp() {

    this.workAreaService = new WorkAreaService(
        asBuiltWorkAreaPageService,
        consultationWorkAreaPageService,
        assignmentService,
        applicationWorkAreaPageService);

    workAreaContext = WorkAreaContextTestUtil.createContextWithAllTabs(authenticatedUserAccount);

    applicationWorkAreaItemPage = WorkAreaApplicationSearchTestUtil.setupFakeApplicationSearchResultPage(
        List.of(),
        PageRequest.of(DEFAULT_PAGE, 10)
    );

    consultationPageView = WorkAreaTestUtils.setUpFakeConsultationPageView(0);
    when(consultationWorkAreaPageService.getPageView(any(), any(), anyInt())).thenReturn(consultationPageView);

    asBuiltNotificationPageView = WorkAreaTestUtils.setUpFakeAsBuiltNotificationPageView(0);
    when(asBuiltWorkAreaPageService.getAsBuiltNotificationsPageView(any(), anyInt()))
        .thenReturn(asBuiltNotificationPageView);
  }

  @Test
  public void getWorkAreaResult_regAttentionTab_noResults() {

    var tab = WorkAreaTab.REGULATOR_REQUIRES_ATTENTION;

    when(applicationWorkAreaPageService.getUsersWorkAreaTabContents(any(), any(), any())).thenReturn(applicationWorkAreaItemPage);

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, tab, DEFAULT_PAGE);

    verify(applicationWorkAreaPageService).getUsersWorkAreaTabContents(workAreaContext, tab.getWorkAreaTabCategory(), regulatorAppTabPageable);

    assertThat(workAreaResult.getApplicationsTabPages().getPageContent()).isEmpty();

    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
    assertThat(workAreaResult.getAsBuiltNotificationTabPages()).isNull();

  }

  @Test
  public void getWorkAreaResult_regBackgroundGroundTab_noResults() {

    var tab = WorkAreaTab.REGULATOR_WAITING_ON_OTHERS;

    when(applicationWorkAreaPageService.getUsersWorkAreaTabContents(any(), any(), any())).thenReturn(applicationWorkAreaItemPage);

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, tab, DEFAULT_PAGE);

    verify(applicationWorkAreaPageService).getUsersWorkAreaTabContents(workAreaContext, tab.getWorkAreaTabCategory(), regulatorAppTabPageable);

    assertThat(workAreaResult.getApplicationsTabPages().getPageContent()).isEmpty();

    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
    assertThat(workAreaResult.getAsBuiltNotificationTabPages()).isNull();

  }


  @Test
  public void getWorkAreaResult_regAttentionTab_resultsExist() {

   var workAreaItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var tab = WorkAreaTab.REGULATOR_REQUIRES_ATTENTION;
    var fakePage = WorkAreaPageServiceTestUtil.getFakeWorkAreaSearchItemPage(List.of(workAreaItem), DEFAULT_PAGE);
    when(applicationWorkAreaPageService.getUsersWorkAreaTabContents(any(), any(), any()))
        .thenReturn(fakePage);

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, tab, DEFAULT_PAGE);

    verify(applicationWorkAreaPageService).getUsersWorkAreaTabContents(workAreaContext, tab.getWorkAreaTabCategory(), regulatorAppTabPageable);

    assertThat(workAreaResult.getApplicationsTabPages().getPageContent())
        .hasOnlyOneElementSatisfying(pwaApplicationWorkAreaItem ->
            assertThat(pwaApplicationWorkAreaItem.getPwaApplicationId()).isEqualTo(workAreaItem.getPwaApplicationId())
        );

    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
    assertThat(workAreaResult.getAsBuiltNotificationTabPages()).isNull();

  }

  @Test
  public void getWorkAreaResult_regBackgroundTab_resultsExist() {

    var workAreaItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var tab = WorkAreaTab.REGULATOR_WAITING_ON_OTHERS;
    var fakePage = WorkAreaPageServiceTestUtil.getFakeWorkAreaSearchItemPage(List.of(workAreaItem), DEFAULT_PAGE);
    when(applicationWorkAreaPageService.getUsersWorkAreaTabContents(any(), any(), any()))
        .thenReturn(fakePage);

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, tab, DEFAULT_PAGE);

    verify(applicationWorkAreaPageService).getUsersWorkAreaTabContents(workAreaContext, tab.getWorkAreaTabCategory(), regulatorAppTabPageable);

    assertThat(workAreaResult.getApplicationsTabPages().getPageContent())
        .hasOnlyOneElementSatisfying(pwaApplicationWorkAreaItem ->
            assertThat(pwaApplicationWorkAreaItem.getPwaApplicationId()).isEqualTo(workAreaItem.getPwaApplicationId())
        );

    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
    assertThat(workAreaResult.getAsBuiltNotificationTabPages()).isNull();

  }

  @Test
  public void getWorkAreaResult_industryAttentionTab_noResults() {

    var tab = WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS;

    when(applicationWorkAreaPageService.getUsersWorkAreaTabContents(any(), any(), any())).thenReturn(applicationWorkAreaItemPage);

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, tab, DEFAULT_PAGE);

    verify(applicationWorkAreaPageService).getUsersWorkAreaTabContents(workAreaContext, tab.getWorkAreaTabCategory(), industryAppTabPageable);

    assertThat(workAreaResult.getApplicationsTabPages().getPageContent()).isEmpty();

    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
    assertThat(workAreaResult.getAsBuiltNotificationTabPages()).isNull();

  }

  @Test
  public void getWorkAreaResult_industryBackgroundGroundTab_noResults() {

    var tab = WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS;

    when(applicationWorkAreaPageService.getUsersWorkAreaTabContents(any(), any(), any())).thenReturn(applicationWorkAreaItemPage);

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, tab, DEFAULT_PAGE);

    verify(applicationWorkAreaPageService).getUsersWorkAreaTabContents(workAreaContext, tab.getWorkAreaTabCategory(), industryAppTabPageable);

    assertThat(workAreaResult.getApplicationsTabPages().getPageContent()).isEmpty();

    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
    assertThat(workAreaResult.getAsBuiltNotificationTabPages()).isNull();

  }


  @Test
  public void getWorkAreaResult_industryAttentionTab_resultsExist() {

    var workAreaItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var tab = WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS;
    var fakePage = WorkAreaPageServiceTestUtil.getFakeWorkAreaSearchItemPage(List.of(workAreaItem), DEFAULT_PAGE);
    when(applicationWorkAreaPageService.getUsersWorkAreaTabContents(any(), any(), any()))
        .thenReturn(fakePage);

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, tab, DEFAULT_PAGE);

    verify(applicationWorkAreaPageService).getUsersWorkAreaTabContents(workAreaContext, tab.getWorkAreaTabCategory(), industryAppTabPageable);

    assertThat(workAreaResult.getApplicationsTabPages().getPageContent())
        .hasOnlyOneElementSatisfying(pwaApplicationWorkAreaItem ->
            assertThat(pwaApplicationWorkAreaItem.getPwaApplicationId()).isEqualTo(workAreaItem.getPwaApplicationId())
        );

    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
    assertThat(workAreaResult.getAsBuiltNotificationTabPages()).isNull();

  }

  @Test
  public void getWorkAreaResult_industryBackgroundTab_resultsExist() {

    var workAreaItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    var tab = WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS;
    var fakePage = WorkAreaPageServiceTestUtil.getFakeWorkAreaSearchItemPage(List.of(workAreaItem), DEFAULT_PAGE);
    when(applicationWorkAreaPageService.getUsersWorkAreaTabContents(any(), any(), any()))
        .thenReturn(fakePage);

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, tab, DEFAULT_PAGE);

    verify(applicationWorkAreaPageService).getUsersWorkAreaTabContents(workAreaContext, tab.getWorkAreaTabCategory(), industryAppTabPageable);

    assertThat(workAreaResult.getApplicationsTabPages().getPageContent())
        .hasOnlyOneElementSatisfying(pwaApplicationWorkAreaItem ->
            assertThat(pwaApplicationWorkAreaItem.getPwaApplicationId()).isEqualTo(workAreaItem.getPwaApplicationId())
        );

    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
    assertThat(workAreaResult.getAsBuiltNotificationTabPages()).isNull();

  }

  @Test
  public void getWorkAreaResult_asBuiltNotificationsTab_resultsExist() {

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, WorkAreaTab.AS_BUILT_NOTIFICATIONS, DEFAULT_PAGE);

    verify(asBuiltWorkAreaPageService).getAsBuiltNotificationsPageView(authenticatedUserAccount, DEFAULT_PAGE);

    assertThat(workAreaResult.getApplicationsTabPages()).isNull();
    assertThat(workAreaResult.getConsultationsTabPages()).isNull();
    assertThat(workAreaResult.getAsBuiltNotificationTabPages()).isEqualTo(asBuiltNotificationPageView);

  }

  @Test
  public void getWorkAreaResult_consultationsTab_resultsExist() {

    var consultationWorkflowSubject = new GenericWorkflowSubject(3, WorkflowType.PWA_APPLICATION_CONSULTATION);
    var consultationWorkflowSubject2 = new GenericWorkflowSubject(4, WorkflowType.PWA_APPLICATION_CONSULTATION);
    var appWorkflowSubject = new GenericWorkflowSubject(2, WorkflowType.PWA_APPLICATION);
    var appWorkflowSubject2 = new GenericWorkflowSubject(3, WorkflowType.PWA_APPLICATION);

    when(assignmentService.getAssignmentsForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Map.of(
        WorkflowType.PWA_APPLICATION, List.of(
            new Assignment(appWorkflowSubject.getBusinessKey(), appWorkflowSubject.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, authenticatedUserAccount.getLinkedPerson().getId()),
            new Assignment(appWorkflowSubject2.getBusinessKey(), appWorkflowSubject2.getWorkflowType(), WorkflowAssignment.CASE_OFFICER, authenticatedUserAccount.getLinkedPerson().getId())
        ),
        WorkflowType.PWA_APPLICATION_CONSULTATION, List.of(
            new Assignment(consultationWorkflowSubject.getBusinessKey(), consultationWorkflowSubject.getWorkflowType(), WorkflowAssignment.CONSULTATION_RESPONDER, authenticatedUserAccount.getLinkedPerson().getId()),
            new Assignment(consultationWorkflowSubject2.getBusinessKey(), consultationWorkflowSubject2.getWorkflowType(), WorkflowAssignment.CONSULTATION_RESPONDER, authenticatedUserAccount.getLinkedPerson().getId())
        )));

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, WorkAreaTab.OPEN_CONSULTATIONS, DEFAULT_PAGE);

    verify(consultationWorkAreaPageService).getPageView(authenticatedUserAccount, Set.of(3,4), DEFAULT_PAGE);

    assertThat(workAreaResult.getApplicationsTabPages()).isNull();
    assertThat(workAreaResult.getConsultationsTabPages()).isEqualTo(consultationPageView);

  }

  @Test
  public void getWorkAreaResult_consultationsTab_noAssignedTasks() {

    when(assignmentService.getAssignmentsForPerson(authenticatedUserAccount.getLinkedPerson())).thenReturn(Map.of());

    var workAreaResult = workAreaService.getWorkAreaResult(workAreaContext, WorkAreaTab.OPEN_CONSULTATIONS, DEFAULT_PAGE);

    verify(consultationWorkAreaPageService).getPageView(authenticatedUserAccount, Set.of(), DEFAULT_PAGE);

    assertThat(workAreaResult.getApplicationsTabPages()).isNull();
    assertThat(workAreaResult.getConsultationsTabPages()).isEqualTo(consultationPageView);

  }

}
