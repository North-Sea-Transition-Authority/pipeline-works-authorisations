package uk.co.ogauthority.pwa.service.pwaapplications.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDto;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.model.dto.appprocessing.ConsultationInvolvementDtoTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationLandingPageServiceTest {

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

  @BeforeEach
  void setUp() throws Exception {
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

  @Test
  void getApplicationLandingPage_cannotLocateDetailForUser() {
    when(pwaApplicationDetailService.getLatestDetailForUser(APP_ID, authenticatedUserAccount))
          .thenReturn(Optional.empty());
    assertThrows(ApplicationLandingPageException.class, () ->

      applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID));
  }

  @Test
  void getApplicationLandingPage_whenApplicationContact_nonPreparer_firstVersion_andDraft() {
    detail.setStatus(PwaApplicationStatus.DRAFT);
    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        detail.getPwaApplication(), EnumSet.of(PwaContactRole.VIEWER));
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertCaseManagementLandingPage(landingPageInstance);

  }

  @Test
  void getApplicationLandingPage_whenApplicationContact_preparer_firstVersion_andDraft() {
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
  void getApplicationLandingPage_whenApplicationContact_firstVersion_andNotDraft() {

    detail.setStatus(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);
    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.generatePwaContactInvolvement(
        detail.getPwaApplication(), EnumSet.allOf(PwaContactRole.class));
    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertCaseManagementLandingPage(landingPageInstance);

  }

  @Test
  void getApplicationLandingPage_whenApplicationContact_notFirstVersion() {

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
  void getApplicationLandingPage_whenConsultationUser() {


    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.generateConsulteeInvolvement(
        detail.getPwaApplication(),
        ConsultationInvolvementDtoTestUtil.emptyConsultationInvolvement()
    );

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertCaseManagementLandingPage(landingPageInstance);
  }

  @Test
  void getApplicationLandingPage_whenUserWithNoDirectInvolvement() {

    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.noInvolvementAndNoFlags(detail.getPwaApplication());

    when(applicationInvolvementService.getApplicationInvolvementDto(detail, authenticatedUserAccount)).thenReturn(applicationInvolvementDto);

    var landingPageInstance = applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, APP_ID);

    assertCaseManagementLandingPage(landingPageInstance);
  }

  @Test
  void getApplicationLandingPage_whenHolderTeamUser() {

    applicationInvolvementDto = ApplicationInvolvementDtoTestUtil.generatePwaHolderTeamInvolvement(
        detail.getPwaApplication(),
        EnumSet.copyOf(TeamType.ORGANISATION.getAllowedRoles())
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