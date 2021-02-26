package uk.co.ogauthority.pwa.service.workarea.applications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import uk.co.ogauthority.pwa.service.enums.workarea.WorkAreaFlag;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationSearchTestUtil;
import uk.co.ogauthority.pwa.service.workarea.WorkAreaTab;

@RunWith(MockitoJUnitRunner.class)
public class RegulatorWorkAreaPageServiceTest {

  private static final int REQUESTED_PAGE = 0;

  private static final int APP_ID_1 = 1;
  private static final int APP_ID_2 = 2;

  private final Set<PwaApplicationStatus> pwaManagerStatuses = Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW, PwaApplicationStatus.CONSENT_REVIEW);

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
        PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW, PwaAppProcessingPermission.CONSENT_REVIEW));

  }

  @Test
  public void getRequiresAttentionPageView_zeroAssignedCases() {
    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(any(), any(), any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workAreaPage = appWorkAreaPageService.getRequiresAttentionPageView(pwaManager, Set.of(), REQUESTED_PAGE);
    assertThat(workAreaPage.getTotalElements()).isEqualTo(0);

    var flagMap = getFlagMapWithDefaultValue(false);
    flagMap.put(WorkAreaFlag.OPEN_CONSENT_REVIEW_FOREGROUND_FLAG, true);

    verify(workAreaApplicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        pwaManagerStatuses,
        Set.of(PublicNoticeStatus.MANAGER_APPROVAL),
        Set.of(),
        flagMap
    );

  }

  @Test
  public void getRequiresAttentionPageView_hasAssignedApps() {

    fakeResultPage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(
        List.of(WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)),
        REQUESTED_PAGE);

    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(any(), any(), any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workAreaPage = appWorkAreaPageService.getRequiresAttentionPageView(pwaManager, Set.of(APP_ID_1, APP_ID_2), REQUESTED_PAGE);

    var flagMap = getFlagMapWithDefaultValue(false);
    flagMap.put(WorkAreaFlag.OPEN_CONSENT_REVIEW_FOREGROUND_FLAG, true);

    verify(workAreaApplicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        pwaManagerStatuses,
        Set.of(PublicNoticeStatus.MANAGER_APPROVAL),
        Set.of(APP_ID_1, APP_ID_2),
        flagMap
    );

    assertThat(workAreaPage.getPageContent())
        .isNotEmpty()
        .allSatisfy(pwaApplicationWorkAreaItem -> assertThat(pwaApplicationWorkAreaItem.getAccessUrl()).isNotNull());
  }

  @Test
  public void getRequiresAttentionPageView_pageableLinksToCorrectTab() {
    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(any(), any(), any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workareaPage = appWorkAreaPageService.getRequiresAttentionPageView(pwaManager, Set.of(), REQUESTED_PAGE);

    assertThat(workareaPage.urlForPage(0))
        .isEqualTo(ReverseRouter.route(on(WorkAreaController.class)
            .renderWorkAreaTab(null, WorkAreaTab.REGULATOR_REQUIRES_ATTENTION, 0)));
  }

  @Test
  public void getWaitingOnOthersPageView_zeroAssignedCases() {
    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(any(), any(), any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workareaPage = appWorkAreaPageService.getWaitingOnOthersPageView(pwaManager, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);

    var flagMap = getFlagMapWithDefaultValue(true);

    verify(workAreaApplicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        pwaManagerStatuses,
        Set.of(PublicNoticeStatus.MANAGER_APPROVAL),
        Set.of(),
        flagMap
    );

  }

  @Test
  public void getWaitingOnOthersPageView_pageableLinksToCorrectTab() {
    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(any(), any(), any(), any(), any()))
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

    when(workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(any(), any(), any(), any(), any()))
        .thenReturn(fakeResultPage);

    var flagMap = getFlagMapWithDefaultValue(true);

    var workAreaPage = appWorkAreaPageService.getWaitingOnOthersPageView(pwaManager, Set.of(APP_ID_1, APP_ID_2), REQUESTED_PAGE);

    verify(workAreaApplicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        pwaManagerStatuses,
        Set.of(PublicNoticeStatus.MANAGER_APPROVAL),
        Set.of(APP_ID_1, APP_ID_2),
        flagMap
    );

    assertThat(workAreaPage.getPageContent())
        .isNotEmpty()
        .allSatisfy(pwaApplicationWorkAreaItem -> assertThat(pwaApplicationWorkAreaItem.getAccessUrl()).isNotNull());

  }

  private Map<WorkAreaFlag, Boolean> getFlagMapWithDefaultValue(Boolean value) {

    return WorkAreaFlag.stream()
        .collect(Collectors.toMap(Function.identity(), val -> value));

  }

}