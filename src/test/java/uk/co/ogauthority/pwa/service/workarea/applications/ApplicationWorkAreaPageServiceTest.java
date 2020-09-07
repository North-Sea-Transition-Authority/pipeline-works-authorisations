package uk.co.ogauthority.pwa.service.workarea.applications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.model.workflow.GenericWorkflowSubject;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationSearchTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;
import uk.co.ogauthority.pwa.service.workflow.task.AssignedTaskInstance;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationWorkAreaPageServiceTest {

  private static final int REQUESTED_PAGE = 0;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private ApplicationDetailSearcher applicationDetailSearcher;

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  @Mock
  private PwaAppProcessingPermissionService appProcessingPermissionService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  private ApplicationWorkAreaPageService appWorkAreaPageService;

  private AuthenticatedUserAccount workAreaUser = new AuthenticatedUserAccount(
      new WebUserAccount(10),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA));

  private AuthenticatedUserAccount pwaManager = new AuthenticatedUserAccount(
      new WebUserAccount(10),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_MANAGER));

  @Before
  public void setup() {

    appWorkAreaPageService = new ApplicationWorkAreaPageService(
        appProcessingPermissionService,
        applicationDetailSearcher,
        pwaContactService,
        pwaApplicationRedirectService,
        camundaWorkflowService);

    when(appProcessingPermissionService.getProcessingPermissions(pwaManager)).thenReturn(Set.of(
        PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));

  }

  @Test
  public void getPageView_zeroResults_userIsWorkAreaUser() {

    var fakePage = new PageImpl<ApplicationDetailSearchItem>(List.of(), getDefaultWorkAreaViewPageable(REQUESTED_PAGE), 0);
    when(applicationDetailSearcher.searchByPwaContacts(any(), any())).thenReturn(fakePage);

    var workareaPage = appWorkAreaPageService.getPageView(workAreaUser, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);
    verify(pwaContactService, times(1)).getPwaContactRolesForWebUserAccount(
        workAreaUser,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    verify(applicationDetailSearcher, times(1)).searchByPwaContacts(
        getDefaultWorkAreaViewPageable(REQUESTED_PAGE),
        Set.of()
    );

    verifyNoInteractions(pwaApplicationRedirectService);
  }

  @Test
  public void getPageView_zeroResults_userIsAdmin() {
    setupFakeApplicationSearchResultPage(List.of(), REQUESTED_PAGE);

    var workareaPage = appWorkAreaPageService.getPageView(pwaManager, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);

    verify(applicationDetailSearcher, times(1)).searchByStatusOrApplicationIds(
        getAdminWorkAreaViewPageable(REQUESTED_PAGE),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of()
    );

    verifyNoInteractions(pwaApplicationRedirectService);
  }

  @Test
  public void getPageView_assignedApps_userIsAdmin() {

    setupFakeApplicationSearchResultPage(List.of(), REQUESTED_PAGE);
    var assignedTask = new AssignedTaskInstance(getAppWorkflowTaskInstance(999, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW), pwaManager.getLinkedPerson());
    var assignedTask2 = new AssignedTaskInstance(getAppWorkflowTaskInstance(9999, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW), pwaManager.getLinkedPerson());

    var workAreaPage = appWorkAreaPageService.getPageView(pwaManager, Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey()), REQUESTED_PAGE);

    verify(applicationDetailSearcher, times(1)).searchByStatusOrApplicationIds(
        getAdminWorkAreaViewPageable(REQUESTED_PAGE),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey())
    );

  }

  @Test
  public void getPageView_viewUrlWhenApplicationStatusDraft_userIsWorkAreaUser() {

    var searchItem = ApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.DRAFT);

    setupFakeApplicationSearchResultPage(List.of(searchItem), REQUESTED_PAGE);

    var workareaPage = appWorkAreaPageService.getPageView(workAreaUser, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(1);
    verify(pwaContactService, times(1)).getPwaContactRolesForWebUserAccount(
        workAreaUser,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    verify(applicationDetailSearcher, times(1)).searchByPwaContacts(
        getDefaultWorkAreaViewPageable(REQUESTED_PAGE),
        Set.of()
    );

    verify(pwaApplicationRedirectService, times(1))
        .getTaskListRoute(searchItem.getPwaApplicationId(), searchItem.getApplicationType());

  }

  @Test
  public void getPageView_viewUrlWhenApplicationStatusInitialSubmission_userIsAdminUser() {

    var searchItem = ApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    setupFakeApplicationSearchResultPage(List.of(searchItem), REQUESTED_PAGE);

    var workareaPage = appWorkAreaPageService.getPageView(pwaManager, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(1);

    verify(applicationDetailSearcher, times(1)).searchByStatusOrApplicationIds(
        getAdminWorkAreaViewPageable(REQUESTED_PAGE),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of()
        );

    verifyNoInteractions(pwaApplicationRedirectService);

  }

  @Test
  public void getPageView_viewUrlWhenApplicationStatusInitialSubmission_userIsWorkAreaUser() {
    var searchItem = ApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    setupFakeApplicationSearchResultPage(List.of(searchItem), REQUESTED_PAGE);

    var workareaPage = appWorkAreaPageService.getPageView(workAreaUser, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(1);
    verify(pwaContactService, times(1)).getPwaContactRolesForWebUserAccount(
        workAreaUser,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    verify(applicationDetailSearcher, times(1)).searchByPwaContacts(
        getDefaultWorkAreaViewPageable(REQUESTED_PAGE),
        Set.of()
    );

    verifyNoInteractions(pwaApplicationRedirectService);

  }

  private Pageable getDefaultWorkAreaViewPageable(int requestedPage) {
    return PageRequest.of(requestedPage, WorkAreaService.PAGE_SIZE,
        Sort.by(Sort.Direction.DESC, "padCreatedTimestamp"));
  }

  private Pageable getAdminWorkAreaViewPageable(int requestedPage) {
    return PageRequest.of(requestedPage, WorkAreaService.PAGE_SIZE,
        Sort.by(Sort.Direction.ASC, "padProposedStart"));
  }

  private Page<ApplicationDetailSearchItem> setupFakeApplicationSearchResultPage(List<ApplicationDetailSearchItem> results, int page) {

    var fakePage = new PageImpl<>(
        results,
        getDefaultWorkAreaViewPageable(page),
        results.size());

    when(applicationDetailSearcher.searchByPwaContacts(any(), any())).thenReturn(fakePage);
    when(applicationDetailSearcher.searchByStatusOrApplicationIds(any(), any(), any())).thenReturn(fakePage);

    return fakePage;

  }

  private WorkflowTaskInstance getAppWorkflowTaskInstance(Integer businessKey, UserWorkflowTask task) {
    return new WorkflowTaskInstance(new GenericWorkflowSubject(businessKey, WorkflowType.PWA_APPLICATION), task);
  }

}