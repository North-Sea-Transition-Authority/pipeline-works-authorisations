package uk.co.ogauthority.pwa.service.pwaapplications.routing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.appprocessing.ApplicationInvolvementService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.util.CaseManagementUtils;

/**
 * Based on a users application involvement, do the logic to correctly route a user when they try to access an application.
 */
@Service
public class ApplicationLandingPageService {

  private final ApplicationInvolvementService applicationInvolvementService;
  private final PwaApplicationRedirectService applicationRedirectService;
  private final PwaApplicationDetailService pwaApplicationDetailService;

  @Autowired
  public ApplicationLandingPageService(ApplicationInvolvementService applicationInvolvementService,
                                       PwaApplicationRedirectService applicationRedirectService,
                                       PwaApplicationDetailService pwaApplicationDetailService) {
    this.applicationInvolvementService = applicationInvolvementService;
    this.applicationRedirectService = applicationRedirectService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
  }


  /**
   * Simply direct users to an appropriate landing page for the application.
   * This is not designed to do extensive authorisation checks, these must be done on the landing pages themselves.
   */
  @Transactional(readOnly = true)
  public ApplicationLandingPageInstance getApplicationLandingPage(AuthenticatedUserAccount user, int applicationId) {

    var detail = pwaApplicationDetailService.getLatestDetailForUser(applicationId, user)
        .orElseThrow(() -> new ApplicationLandingPageException(
            String.format("Did not find latest detail for user wuaId:%s appId:%s", user.getWuaId(), applicationId))
        );

    var appInvolvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    if (appInvolvement.isUserInAppContactTeam() && detail.isFirstVersion()) {
      return new ApplicationLandingPageInstance(
          ApplicationLandingPage.TASK_LIST,
          applicationRedirectService.getTaskListRoute(applicationId, detail.getPwaApplicationType())
      );
    }

    return new ApplicationLandingPageInstance(
        ApplicationLandingPage.CASE_MANAGEMENT,
        CaseManagementUtils.routeCaseManagement(applicationId, detail.getPwaApplicationType())
    );

  }
}
