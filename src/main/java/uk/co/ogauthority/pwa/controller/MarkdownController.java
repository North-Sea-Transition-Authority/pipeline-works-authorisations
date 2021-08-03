package uk.co.ogauthority.pwa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;

@Controller
@RequestMapping("/markdown-guidance")
public class MarkdownController {

  @GetMapping
  public ModelAndView renderMarkdownGuidance(AuthenticatedUserAccount authenticatedUserAccount) {
    return new ModelAndView("components/markdown/markdownGuidance");
  }

}
