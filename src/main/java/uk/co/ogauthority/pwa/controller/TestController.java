package uk.co.ogauthority.pwa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.model.entity.UserAccount;

@Controller
public class TestController {

  @GetMapping("/test")
  public ModelAndView test(UserAccount userAccount) {
    return new ModelAndView("test", "user", userAccount);
  }

}
