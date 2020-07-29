package uk.co.ogauthority.pwa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.featuretest.IdentSummaryView;
import uk.co.ogauthority.pwa.service.diff.DiffService;

/**
 * Test/debug controller for various session related endpoints.
 * Can be removed before production release.
 */
@Controller
public class TestController {

  @GetMapping("/session-info")
  public ModelAndView sessionInfo(AuthenticatedUserAccount userAccount) {
    return new ModelAndView("testTemplates/sessionInfo", "user", userAccount);
  }

  @GetMapping("/requires-auth")
  public ModelAndView requiresAuth(AuthenticatedUserAccount userAccount) {
    return new ModelAndView("testTemplates/requiresAuth", "user", userAccount);
  }

  @GetMapping("/maps-test")
  public ModelAndView maps() {
    return new ModelAndView("testTemplates/mapsTest");
  }

}
