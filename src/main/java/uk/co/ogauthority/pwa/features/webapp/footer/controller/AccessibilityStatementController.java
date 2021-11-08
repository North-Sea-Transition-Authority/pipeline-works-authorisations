package uk.co.ogauthority.pwa.features.webapp.footer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.enums.ServiceContactDetail;

@Controller
@RequestMapping("/accessibility-statement")
public class AccessibilityStatementController {


  @GetMapping()
  public ModelAndView getAccessibilityStatement(AuthenticatedUserAccount authenticatedUserAccount) {
    return new ModelAndView("footer/accessibilityStatement")
        .addObject("pageHeading", "Accessibility statement")
        .addObject("technicalSupport", ServiceContactDetail.TECHNICAL_SUPPORT);
  }


}
