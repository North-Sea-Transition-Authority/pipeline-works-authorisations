package uk.co.ogauthority.pipelines.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pipelines.model.entity.UserAccount;

@Controller
public class TestController {

  @GetMapping("/test")
  public ModelAndView test(UserAccount userAccount) {
    return new ModelAndView("test", "user", userAccount);
  }

}
