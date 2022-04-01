package uk.co.ogauthority.pwa.features.webapp.footer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/cookies")
public class CookiesController {

  @GetMapping
  public ModelAndView getCookiePreferences() {
    return new ModelAndView("footer/cookies");
  }

}
