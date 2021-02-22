package uk.co.ogauthority.pwa.service.workarea.applications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.WorkAreaController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationSearchTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;

@RunWith(MockitoJUnitRunner.class)
public class RegulatorWorkAreaPageServiceTest {

  private static final int REQUESTED_PAGE = 0;

  private static final int APP_ID_1 = 1;
  private static final int APP_ID_2 = 2;

  @Mock
  private WorkAreaApplicationDetailSearcher workAreaApplicationDetailSearcher;

  @Mock
  private PwaAppProcessingPermissionService appProcessingPermissionService;

  private RegulatorWorkAreaPageService appWorkAreaPageService;

  private Page<WorkAreaApplicationDetailSearchItem> fakeResultPage;

  private AuthenticatedUserAccount pwaManager = new AuthenticatedUserAccount(
      new WebUserAccount(10),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_MANAGER));

  @Before
  public void setup() {

    appWorkAreaPageService = new RegulatorWorkAreaPageService(
        appProcessingPermissionService,
        workAreaApplicationDetailSearcher
    );

    fakeResultPage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(List.of(), REQUESTED_PAGE);

    when(appProcessingPermissionService.getGenericProcessingPermissions(pwaManager)).thenReturn(Set.of(
        PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));

  }

  @Test
  public void getRequiresAttentionPageView_zeroAssignedCases() {
    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(any(), any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workareaPage = appWorkAreaPageService.getRequiresAttentionPageView(pwaManager, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);

    verify(workAreaApplicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of(PublicNoticeStatus.MANAGER_APPROVAL),
        Set.of()
    );

  }

  @Test
  public void getRequiresAttentionPageView_zeroAssignedCases_caseOfficerPrivilege() {
    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(any(), any(), any(), any()))
        .thenReturn(fakeResultPage);

    var caseOfficer = new AuthenticatedUserAccount(new WebUserAccount(10),
        EnumSet.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_CASE_OFFICER));

    var workareaPage = appWorkAreaPageService.getRequiresAttentionPageView(caseOfficer, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);

    verify(workAreaApplicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of(PublicNoticeStatus.DRAFT, PublicNoticeStatus.CASE_OFFICER_REVIEW),
        Set.of()
    );

  }

  @Test
  public void getRequiresAttentionPageView_hasAssignedApps() {
    fakeResultPage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(
        List.of(WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)),
        REQUESTED_PAGE);

    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(any(), any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workAreaPage = appWorkAreaPageService.getRequiresAttentionPageView(pwaManager, Set.of(APP_ID_1, APP_ID_2), REQUESTED_PAGE);

    verify(workAreaApplicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of(PublicNoticeStatus.MANAGER_APPROVAL),
        Set.of(APP_ID_1, APP_ID_2)
    );

    assertThat(workAreaPage.getPageContent())
        .isNotEmpty()
        .allSatisfy(pwaApplicationWorkAreaItem -> assertThat(pwaApplicationWorkAreaItem.getAccessUrl()).isNotNull());
  }

  @Test
  public void getRequiresAttentionPageView_pageableLinksToCorrectTab() {
    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(any(), any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workareaPage = appWorkAreaPageService.getRequiresAttentionPageView(pwaManager, Set.of(), REQUESTED_PAGE);

    assertThat(workareaPage.urlForPage(0))
        .isEqualTo(ReverseRouter.route(on(WorkAreaController.class)
            .renderWorkAreaTab(null, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, 0)));
  }

  @Test
  public void getWaitingOnOthersPageView_zeroAssignedCases() {
    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue(any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workareaPage = appWorkAreaPageService.getWaitingOnOthersPageView(pwaManager, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);

    verify(workAreaApplicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of()
    );

  }

  @Test
  public void getWaitingOnOthersPageView_pageableLinksToCorrectTab() {
    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue(any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workareaPage = appWorkAreaPageService.getWaitingOnOthersPageView(pwaManager, Set.of(), REQUESTED_PAGE);

    assertThat(workareaPage.urlForPage(0))
        .isEqualTo(ReverseRouter.route(on(WorkAreaController.class)
            .renderWorkAreaTab(null, WorkAreaTab.REGULATOR_WAITING_ON_OTHERS, 0)));
  }

  @Test
  public void getWaitingOnOthersPageView_hasAssignedApps() {

    fakeResultPage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(
        List.of(WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.CASE_OFFICER_REVIEW)),
        REQUESTED_PAGE);

    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue(any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workAreaPage = appWorkAreaPageService.getWaitingOnOthersPageView(pwaManager, Set.of(APP_ID_1, APP_ID_2), REQUESTED_PAGE);

    verify(workAreaApplicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of(APP_ID_1, APP_ID_2)
    );

    assertThat(workAreaPage.getPageContent())
        .isNotEmpty()
        .allSatisfy(pwaApplicationWorkAreaItem -> assertThat(pwaApplicationWorkAreaItem.getAccessUrl()).isNotNull());

  }


}