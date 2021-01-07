package uk.co.ogauthority.pwa.controller.search.applicationsearch;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

@Controller
@RequestMapping("/application-search")
public class ApplicationSearchController {

  @GetMapping
  public ModelAndView renderApplicationSearch(AuthenticatedUserAccount authenticatedUserAccount) {
    return new ModelAndView("search/applicationSearch/applicationSearch");

  }

}
