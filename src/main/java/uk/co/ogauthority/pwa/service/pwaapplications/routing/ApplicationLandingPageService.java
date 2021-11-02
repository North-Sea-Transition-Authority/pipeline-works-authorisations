package uk.co.ogauthority.pwa.service.pwaapplications.routing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactRole;
import uk.co.ogauthority.pwa.features.application.authorisation.involvement.ApplicationInvolvementService;
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

  private final String pwaUrlBase;
  private final String contextPath;

  @Autowired
  public ApplicationLandingPageService(ApplicationInvolvementService applicationInvolvementService,
                                       PwaApplicationRedirectService applicationRedirectService,
                                       PwaApplicationDetailService pwaApplicationDetailService,
                                       @Value("${pwa.url.base}") String pwaUrlBase,
                                       @Value("${context-path}") String contextPath) {
    this.applicationInvolvementService = applicationInvolvementService;
    this.applicationRedirectService = applicationRedirectService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.pwaUrlBase = pwaUrlBase;
    this.contextPath = contextPath;
  }


  /**
   * Simply direct users to an appropriate landing page for the application.
   * This is not designed to do extensive authorisation checks, these must be done on the landing pages themselves.
   */
  public ApplicationLandingPageInstance getApplicationLandingPage(AuthenticatedUserAccount user, int applicationId) {

    var detail = pwaApplicationDetailService.getLatestDetailForUser(applicationId, user)
        .orElseThrow(() -> new ApplicationLandingPageException(
            String.format("Did not find latest detail for user wuaId:%s appId:%s", user.getWuaId(), applicationId))
        );

    var appInvolvement = applicationInvolvementService.getApplicationInvolvementDto(detail, user);

    var urlRoot = pwaUrlBase + contextPath;
    if (appInvolvement.hasAnyOfTheseContactRoles(PwaContactRole.PREPARER) && detail.isFirstDraft()) {
      return new ApplicationLandingPageInstance(
          ApplicationLandingPage.TASK_LIST,
          urlRoot + applicationRedirectService.getTaskListRoute(applicationId, detail.getPwaApplicationType())
      );
    }

    return new ApplicationLandingPageInstance(
        ApplicationLandingPage.CASE_MANAGEMENT,
        urlRoot + CaseManagementUtils.routeCaseManagement(applicationId, detail.getPwaApplicationType())
    );

  }
}
