package uk.co.ogauthority.pwa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.pwaapplications.routing.ApplicationLandingPageService;

@Controller
@RequestMapping("/pwa-application/{applicationId}/route")
public class ApplicationLandingPageRouterController {

  private final ApplicationLandingPageService applicationLandingPageService;

  @Autowired
  public ApplicationLandingPageRouterController(ApplicationLandingPageService applicationLandingPageService) {
    this.applicationLandingPageService = applicationLandingPageService;
  }

  @GetMapping
  public RedirectView route(@PathVariable("applicationId") int appId,
                            AuthenticatedUserAccount authenticatedUserAccount) {
    return new RedirectView(
        applicationLandingPageService.getApplicationLandingPage(authenticatedUserAccount, appId).getUrl()
    );
  }

}
