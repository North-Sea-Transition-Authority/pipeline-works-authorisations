package uk.co.ogauthority.pwa.service.workarea.applications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.workflow.WorkflowBusinessKey;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.workflow.WorkflowType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaApplicationContactRoleDto;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationSearchTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;
import uk.co.ogauthority.pwa.service.workflow.CamundaWorkflowService;

@RunWith(MockitoJUnitRunner.class)
public class IndustryWorkAreaPageServiceTest {

  private static final int REQUESTED_PAGE = 0;
  private static final int BUSINESS_KEY_INT = 1;

  @Mock
  private PwaContactService pwaContactService;

  @Mock
  private WorkAreaApplicationDetailSearcher workAreaApplicationDetailSearcher;

  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  @Mock
  private CamundaWorkflowService camundaWorkflowService;

  private IndustryWorkAreaPageService industryWorkAreaPageService;

  private AuthenticatedUserAccount workAreaUser = new AuthenticatedUserAccount(
      new WebUserAccount(10),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA));

  private AuthenticatedUserAccount pwaManager = new AuthenticatedUserAccount(
      new WebUserAccount(10),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_MANAGER));

  @Before
  public void setup() {

    industryWorkAreaPageService = new IndustryWorkAreaPageService(
        workAreaApplicationDetailSearcher,
        pwaContactService,
        pwaApplicationRedirectService,
        camundaWorkflowService
    );

    when(pwaContactService.getPwaContactRolesForWebUserAccount(any(), any()))
        .thenReturn(Set.of(new PwaApplicationContactRoleDto(1, BUSINESS_KEY_INT, PwaContactRole.PREPARER)));

    when(camundaWorkflowService.filterBusinessKeysByWorkflowTypeAndActiveTasksContains(
        eq(WorkflowType.PWA_APPLICATION),
        any(),
        any())
    ).thenAnswer(invocation ->
        // fake a filter where every param is returned
        Optional.of(invocation.getArgument(1))
            .map(o -> (Set<WorkflowBusinessKey>) o)
            .orElse(new HashSet<WorkflowBusinessKey>())
    );
  }

  @Test
  public void getOpenApplicationsPageView_zeroResults() {

    var fakePage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(List.of(), REQUESTED_PAGE);
    when(workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(any(), any(), any(), anyBoolean()))
        .thenReturn(fakePage);

    var workareaPage = industryWorkAreaPageService.getOpenApplicationsPageView(workAreaUser, REQUESTED_PAGE);

    assertThat(workareaPage.getTotalElements()).isEqualTo(0);
    verify(pwaContactService, times(1)).getPwaContactRolesForWebUserAccount(
        workAreaUser,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    verify(workAreaApplicationDetailSearcher, times(1)).searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(BUSINESS_KEY_INT),
        ApplicationState.INDUSTRY_EDITABLE.getStatuses(),
        true
    );

    verifyNoInteractions(pwaApplicationRedirectService);
  }

  @Test
  public void getOpenApplicationsPageView_pageableLinksToCorrectTab() {
    var fakePage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(List.of(), REQUESTED_PAGE);
    when(workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(any(), any(), any(), anyBoolean()))
        .thenReturn(fakePage);

    var workareaPage = industryWorkAreaPageService.getOpenApplicationsPageView(workAreaUser, REQUESTED_PAGE);

    assertThat(workareaPage.urlForPage(0))
        .isEqualTo(ReverseRouter.route(on(WorkAreaController.class)
            .renderWorkAreaTab(null, WorkAreaTab.INDUSTRY_OPEN_APPLICATIONS, 0)));
  }

  @Test
  public void getOpenApplicationsPageView_viewUrlWhenApplicationStatusDraft() {

    var searchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.DRAFT);

    var fakePage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(List.of(searchItem), REQUESTED_PAGE);
    when(workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(any(), any(), any(), anyBoolean()))
        .thenReturn(fakePage);

    var workareaPage = industryWorkAreaPageService.getOpenApplicationsPageView(workAreaUser, REQUESTED_PAGE);

    assertThat(workareaPage.getTotalElements()).isEqualTo(1);
    verify(pwaContactService, times(1)).getPwaContactRolesForWebUserAccount(
        workAreaUser,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    verify(workAreaApplicationDetailSearcher, times(1)).searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(BUSINESS_KEY_INT),
        ApplicationState.INDUSTRY_EDITABLE.getStatuses(),
        true
    );

    verify(pwaApplicationRedirectService, times(1))
        .getTaskListRoute(searchItem.getPwaApplicationId(), searchItem.getApplicationType());

  }


  @Test
  public void getOpenApplicationsPageView_viewUrlWhenApplicationStatusInitialSubmission() {

    var searchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    var fakePage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(List.of(searchItem), REQUESTED_PAGE);
    when(workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(any(), any(), any(), anyBoolean()))
        .thenReturn(fakePage);

    var workareaPage = industryWorkAreaPageService.getOpenApplicationsPageView(pwaManager, REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(1);

    verify(workAreaApplicationDetailSearcher, times(1)).searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(BUSINESS_KEY_INT),
        ApplicationState.INDUSTRY_EDITABLE.getStatuses(),
        true
    );

    verifyNoInteractions(pwaApplicationRedirectService);

  }

  @Test
  public void getSubmittedApplicationsPageView_pageableLinksToCorrectTab() {
    var fakePage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(List.of(), REQUESTED_PAGE);
    when(workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInAndOpenUpdateRequest(any(), any(), any(), anyBoolean()))
        .thenReturn(fakePage);

    var workareaPage = industryWorkAreaPageService.getSubmittedApplicationsPageView(workAreaUser, REQUESTED_PAGE);

    assertThat(workareaPage.urlForPage(0))
        .isEqualTo(ReverseRouter.route(on(WorkAreaController.class)
            .renderWorkAreaTab(null, WorkAreaTab.INDUSTRY_SUBMITTED_APPLICATIONS, 0)));
  }

  @Test
  public void getSubmittedApplicationsPageView_zeroResults() {

    var fakePage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(List.of(), REQUESTED_PAGE);
    when(workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInAndOpenUpdateRequest(any(), any(), any(), anyBoolean()))
        .thenReturn(fakePage);

    var workareaPage = industryWorkAreaPageService.getSubmittedApplicationsPageView(workAreaUser, REQUESTED_PAGE);

    assertThat(workareaPage.getTotalElements()).isEqualTo(0);
    verify(pwaContactService, times(1)).getPwaContactRolesForWebUserAccount(
        workAreaUser,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    verify(workAreaApplicationDetailSearcher, times(1)).searchWhereApplicationIdInAndWhereStatusInAndOpenUpdateRequest(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(BUSINESS_KEY_INT),
        Set.of(
            PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW,
            PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT,
            PwaApplicationStatus.CASE_OFFICER_REVIEW,
            PwaApplicationStatus.WITHDRAWN,
            PwaApplicationStatus.DELETED,
            PwaApplicationStatus.COMPLETE
        ),
        false
    );

    verifyNoInteractions(pwaApplicationRedirectService);
  }

  @Test
  public void getSubmittedApplicationsPageView_whenResultsFound() {

    var searchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.CASE_OFFICER_REVIEW);

    var fakePage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(List.of(searchItem), REQUESTED_PAGE);
    when(workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInAndOpenUpdateRequest(any(), any(), any(), anyBoolean()))
        .thenReturn(fakePage);

    var workareaPage = industryWorkAreaPageService.getSubmittedApplicationsPageView(workAreaUser, REQUESTED_PAGE);

    assertThat(workareaPage.getTotalElements()).isEqualTo(1);
    verify(pwaContactService, times(1)).getPwaContactRolesForWebUserAccount(
        workAreaUser,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    verify(workAreaApplicationDetailSearcher, times(1)).searchWhereApplicationIdInAndWhereStatusInAndOpenUpdateRequest(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(BUSINESS_KEY_INT),
        Set.of(
            PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW,
            PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT,
            PwaApplicationStatus.CASE_OFFICER_REVIEW,
            PwaApplicationStatus.WITHDRAWN,
            PwaApplicationStatus.DELETED,
            PwaApplicationStatus.COMPLETE
        ),
        false
    );

    verifyNoInteractions(pwaApplicationRedirectService);

  }


}