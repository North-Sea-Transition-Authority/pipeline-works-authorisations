package uk.co.ogauthority.pwa.service.pwaapplications.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.service.appprocessing.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationLandingPageServiceTest {

  private static final int APP_ID = 10;
  private static final PwaApplicationType APP_TYPE = PwaApplicationType.INITIAL;

  private static final String TASK_LIST_ROUTE = "example/task/list/route";

  private static final String URL_BASE = "base/";
  private static final String CONTEXT_PATH = "context";

  @Mock
  private ApplicationInvolvementService applicationInvolvementService;
  @Mock
  private PwaApplicationRedirectService applicationRedirectService;
  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private ApplicationLandingPageService applicationLandingPageService;

  private PwaApplicationDetail detail;
  private AuthenticatedUserAccount authenticatedUserAccount;
  private ApplicationInvolvementDto applicationInvolvementDto;

  @Before
  public void setUp() throws Exception {
    applicationLandingPageService = new ApplicationLandingPageService(
        applicationInvolvementService, applicationRedirectService, pwaApplicationDetailService,
        URL_BASE,
        CONTEXT_PATH);

    authenticatedUserAccount = AuthenticatedUserAccountTestUtil.defaultAllPrivUserAccount();

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID ,20, 1);
    when(pwaApplicationDetailService.getLatestDetailForUser(APP_ID, authenticatedUserAccount))
        .thenReturn(Optional.of(detail));

    when(applicationRedirectService.getTaskListRoute(anyInt(), any())).thenReturn(TASK_LIST_ROUTE);

  }

  @Test(expected = ApplicationLandingPageException.class)
  public void getApplicationLandingPage_cannotLocateDetailForUser() {
    when(pwaApplicationDetailService.getLatestDetailForUser(APP_ID, authenticatedUserAccount))
        .thenReturn(Optional.empty());

    applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);
  }

  @Test
  public void getApplicationLandingPage_whenApplicationContact_nonPreparer_firstVersion_andDraft() {
    detail.setStatus(PwaApplicationStatus.DRAFT);
    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        detail.getPwaApplication(), EnumSet.of(PwaContactRole.VIEWER));
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertCaseManagementLandingPage(landingPageInstance);

  }

  @Test
  public void getApplicationLandingPage_whenApplicationContact_preparer_firstVersion_andDraft() {
    detail.setStatus(PwaApplicationStatus.DRAFT);
    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        detail.getPwaApplication(), EnumSet.of(PwaContactRole.PREPARER));
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertThat(landingPageInstance.getApplicationLandingPage()).isEqualTo(ApplicationLandingPage.TASK_LIST);
    assertThat(landingPageInstance.getUrl()).isEqualTo(URL_BASE + CONTEXT_PATH + TASK_LIST_ROUTE);

    verify(applicationRedirectService, times(1)).getTaskListRoute(APP_ID, APP_TYPE);

  }

  @Test
  public void getApplicationLandingPage_whenApplicationContact_firstVersion_andNotDraft() {

    detail.setStatus(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);
    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        detail.getPwaApplication(), EnumSet.allOf(PwaContactRole.class));
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertCaseManagementLandingPage(landingPageInstance);

  }

  @Test
  public void getApplicationLandingPage_whenApplicationContact_notFirstVersion() {

    detail = PwaApplicationTestUtil.createDefaultApplicationDetail(APP_TYPE, APP_ID ,20, 2);
    when(pwaApplicationDetailService.getLatestDetailForUser(APP_ID, authenticatedUserAccount))
        .thenReturn(Optional.of(detail));

    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        detail.getPwaApplication(), EnumSet.allOf(PwaContactRole.class));

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertCaseManagementLandingPage(landingPageInstance);

  }

  @Test
  public void getApplicationLandingPage_whenConsultationUser() {


    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        detail.getPwaApplication(),
        ConsultationInvolvementDtoTestUtil.emptyConsultationInvolvement()
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertCaseManagementLandingPage(landingPageInstance);
  }

  @Test
  public void getApplicationLandingPage_whenUserWithNoDirectInvolvement() {

    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(detail.getPwaApplication());

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertCaseManagementLandingPage(landingPageInstance);
  }

  @Test
  public void getApplicationLandingPage_whenHolderTeamUser() {

    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
        detail.getPwaApplication(),
        EnumSet.allOf(PwaOrganisationRole.class)
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertCaseManagementLandingPage(landingPageInstance);
  }

  private void assertCaseManagementLandingPage(ApplicationLandingPageInstance applicationLandingPageInstance){
    assertThat(applicationLandingPageInstance.getApplicationLandingPage()).isEqualTo(ApplicationLandingPage.CASE_MANAGEMENT);
    assertThat(applicationLandingPageInstance.getUrl()).isEqualTo( URL_BASE + CONTEXT_PATH + CaseManagementUtils.routeCaseManagement(APP_ID, APP_TYPE));

    verifyNoInteractions(applicationRedirectService);
  }
}