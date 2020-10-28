package uk.co.ogauthority.pwa.service.workarea.applications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.appprocessing.PwaAppProcessingPermissionService;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationSearchTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class RegulatorWorkAreaPageServiceTest {

  private static final int REQUESTED_PAGE = 0;

  private static final int APP_ID_1 = 1;
  private static final int APP_ID_2 = 2;

  @Mock
  private ApplicationDetailSearcher applicationDetailSearcher;

  @Mock
  private PwaAppProcessingPermissionService appProcessingPermissionService;

  private RegulatorWorkAreaPageService appWorkAreaPageService;

  private Page<ApplicationDetailSearchItem> fakeResultPage;

  private AuthenticatedUserAccount pwaManager = new AuthenticatedUserAccount(
      new WebUserAccount(10),
      EnumSet.of(PwaUserPrivilege.PWA_WORKAREA, PwaUserPrivilege.PWA_MANAGER));

  @Before
  public void setup() {

    appWorkAreaPageService = new RegulatorWorkAreaPageService(
        appProcessingPermissionService,
        applicationDetailSearcher
    );

    fakeResultPage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(List.of(), REQUESTED_PAGE);

    when(appProcessingPermissionService.getGenericProcessingPermissions(pwaManager)).thenReturn(Set.of(
        PwaAppProcessingPermission.ACCEPT_INITIAL_REVIEW));

  }

  @Test
  public void getRequiresAttentionPageView_zeroAssignedCases() {
    when(applicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereAllProcessingWaitFlagsFalse(any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workareaPage = appWorkAreaPageService.getRequiresAttentionPageView(pwaManager, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);

    verify(applicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereAllProcessingWaitFlagsFalse(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of()
    );

  }

  @Test
  public void getRequiresAttentionPageView_hasAssignedApps() {
    fakeResultPage = WorkAreaPageServiceTestUtil.getFakeApplicationSearchResultPage(
        List.of(ApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW)),
        REQUESTED_PAGE);

    when(applicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereAllProcessingWaitFlagsFalse(any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workAreaPage = appWorkAreaPageService.getRequiresAttentionPageView(pwaManager, Set.of(APP_ID_1, APP_ID_2), REQUESTED_PAGE);

    verify(applicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereAllProcessingWaitFlagsFalse(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of(APP_ID_1, APP_ID_2)
    );

    assertThat(workAreaPage.getPageContent()).allSatisfy(pwaApplicationWorkAreaItem ->
        assertThat(pwaApplicationWorkAreaItem.getAccessUrl()).isNotNull()
    );
  }

  @Test
  public void getWaitingOnOthersPageView_zeroAssignedCases() {
    when(applicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereAnyProcessingWaitFlagTrue(any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workareaPage = appWorkAreaPageService.getWaitingOnOthersPageView(pwaManager, Set.of(), REQUESTED_PAGE);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);

    verify(applicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereAnyProcessingWaitFlagTrue(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of()
    );


  }

  @Test
  public void getWaitingOnOthersPageView_hasAssignedApps() {
    when(applicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereAnyProcessingWaitFlagTrue(any(), any(), any()))
        .thenReturn(fakeResultPage);

    var workAreaPage = appWorkAreaPageService.getWaitingOnOthersPageView(pwaManager, Set.of(APP_ID_1, APP_ID_2), REQUESTED_PAGE);

    verify(applicationDetailSearcher, times(1)).searchByStatusOrApplicationIdsAndWhereAnyProcessingWaitFlagTrue(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(REQUESTED_PAGE, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of(APP_ID_1, APP_ID_2)
    );

    assertThat(workAreaPage.getPageContent()).allSatisfy(pwaApplicationWorkAreaItem ->
        assertThat(pwaApplicationWorkAreaItem.getAccessUrl()).isNotNull()
    );

  }


}