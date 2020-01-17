package uk.co.ogauthority.pwa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.entity.UserAccount;

/**
 * Test/debug controller for various session related endpoints
 * Can be removed before production release
 */
@Controller
public class TestController {

  @GetMapping("/session-info")
  public ModelAndView sessionInfo(UserAccount userAccount) {
    return new ModelAndView("testTemplates/sessionInfo", "user", userAccount);
  }

  @GetMapping("/requires-auth")
  public ModelAndView requiresAuth(UserAccount userAccount) {
    return new ModelAndView("testTemplates/requiresAuth", "user", userAccount);
  }

}
