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
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.PwaApplicationWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.UserWorkflowTask;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationSearchTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaService;
import uk.co.ogauthority.pwa.service.workflow.task.AssignedTaskInstance;
import uk.co.ogauthority.pwa.service.workflow.task.WorkflowTaskInstance;

@RunWith(MockitoJUnitRunner.class)
public class RegulatorWorkAreaPageServiceTest {

  private static final int REQUESTED_PAGE = 0;

  @Mock
  private ApplicationDetailSearcher applicationDetailSearcher;

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  @Mock
  private PwaAppProcessingPermissionService appProcessingPermissionService;

  private RegulatorWorkAreaPageService appWorkAreaPageService;

  private AuthenticatedUserAccount pwaManager = new AuthenticatedUserAccount(
      new WebUserAccount(10),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_MANAGER));

  @Before
  public void setup() {

    appWorkAreaPageService = new RegulatorWorkAreaPageService(
        appProcessingPermissionService,
        applicationDetailSearcher
    );

    when(appProcessingPermissionService.getGenericProcessingPermissions(pwaManager)).thenReturn(Set.of(
        PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));

  }

  @Test
  public void getPageView_zeroAssignedCases() {
    setupFakeApplicationSearchResultPage(List.of(), REQUESTED_PAGE);

    var workareaPage = appWorkAreaPageService.getPageView(pwaManager, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);

    verify(applicationDetailSearcher, times(1)).searchByStatusOrApplicationIds(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of()
    );

    verifyNoInteractions(pwaApplicationRedirectService);
  }

  @Test
  public void getPageView_hasAssignedApps() {

    setupFakeApplicationSearchResultPage(List.of(), REQUESTED_PAGE);
    var assignedTask = new AssignedTaskInstance(getAppWorkflowTaskInstance(999, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW), pwaManager.getLinkedPerson());
    var assignedTask2 = new AssignedTaskInstance(getAppWorkflowTaskInstance(9999, PwaApplicationWorkflowTask.CASE_OFFICER_REVIEW), pwaManager.getLinkedPerson());

    var workAreaPage = appWorkAreaPageService.getPageView(pwaManager, Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey()), REQUESTED_PAGE);

    verify(applicationDetailSearcher, times(1)).searchByStatusOrApplicationIds(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of(assignedTask.getBusinessKey(), assignedTask2.getBusinessKey())
    );

  }

  @Test
  public void getPageView_viewUrlWhenApplicationStatusInitialSubmission() {

    var searchItem = ApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    setupFakeApplicationSearchResultPage(List.of(searchItem), REQUESTED_PAGE);

    var workareaPage = appWorkAreaPageService.getPageView(pwaManager, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(1);

    verify(applicationDetailSearcher, times(1)).searchByStatusOrApplicationIds(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of()
    );

    verifyNoInteractions(pwaApplicationRedirectService);

  }

  private Pageable getDefaultWorkAreaViewPageable(int requestedPage) {
    return PageRequest.of(requestedPage, WorkAreaService.PAGE_SIZE,
        Sort.by(Sort.Direction.DESC, "padCreatedTimestamp"));
  }


  private Page<ApplicationDetailSearchItem> setupFakeApplicationSearchResultPage(List<ApplicationDetailSearchItem> results, int page) {

    var fakePage = new PageImpl<>(
        results,
        getDefaultWorkAreaViewPageable(page),
        results.size());

    when(applicationDetailSearcher.searchByStatusOrApplicationIds(any(), any(), any())).thenReturn(fakePage);

    return fakePage;

  }

  private WorkflowTaskInstance getAppWorkflowTaskInstance(Integer businessKey, UserWorkflowTask task) {
    return new WorkflowTaskInstance(new GenericWorkflowSubject(businessKey, WorkflowType.PWA_APPLICATION), task);
  }

}