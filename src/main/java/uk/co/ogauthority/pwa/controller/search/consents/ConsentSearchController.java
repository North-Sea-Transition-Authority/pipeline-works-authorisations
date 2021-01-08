package uk.co.ogauthority.pwa.controller.search.consents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/consents/search")
public class ConsentSearchController {

  @GetMapping
  public ModelAndView renderSearch() {

    return new ModelAndView("search/consents/consentSearch");

  }

}
