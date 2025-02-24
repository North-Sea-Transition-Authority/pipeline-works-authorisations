package uk.co.ogauthority.pwa.auth;

import org.springframework.stereotype.Controller;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
@ActiveProfiles("test")
class TestAuthenticationController {

  @GetMapping("/secured")
  ModelAndView requiresUserEndpoint(AuthenticatedUserAccount authenticatedUserAccount) {
    return new ModelAndView()
        .addObject("user", authenticatedUserAccount);
  }

}

