package uk.co.ogauthority.pwa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.model.enums.MarkdownType;

@Controller
@RequestMapping
public class MarkdownController {

  @GetMapping("/markdown-guidance")
  public ModelAndView renderMarkdownGuidance(AuthenticatedUserAccount authenticatedUserAccount) {
    return new ModelAndView("components/markdown/markdownGuidance")
      .addObject("markdownType", MarkdownType.PWA_COMMON_MARKDOWN);
  }

  @GetMapping("/email-markdown-guidance")
  public ModelAndView renderEmailMarkdownGuidance(AuthenticatedUserAccount authenticatedUserAccount) {
    return new ModelAndView("components/markdown/markdownGuidance")
      .addObject("markdownType", MarkdownType.GOVUK_NOTIFY_MARKDOWN);
  }

}
