package uk.co.ogauthority.pwa.controller;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.markdown.MarkdownService;
import uk.co.ogauthority.pwa.service.markdown.MarkdownTestForm;

/**
 * Test/debug controller for various session related endpoints.
 * Can be removed before production release.
 */
@Controller
public class TestController {

  private final MarkdownService markdownService;

  @Autowired
  public TestController(MarkdownService markdownService) {
    this.markdownService = markdownService;
  }

  @GetMapping("/session-info")
  public ModelAndView sessionInfo(AuthenticatedUserAccount userAccount) {
    return new ModelAndView("testTemplates/sessionInfo", "user", userAccount);
  }

  @GetMapping("/requires-auth")
  public ModelAndView requiresAuth(AuthenticatedUserAccount userAccount) {
    return new ModelAndView("testTemplates/requiresAuth", "user", userAccount);
  }

  @GetMapping("/maps-test")
  public ModelAndView maps() {
    return new ModelAndView("testTemplates/mapsTest");
  }

  @GetMapping("/markdown")
  public ModelAndView getMarkdownTest() {
    return new ModelAndView("test/markdownTest")
        .addObject("form", new MarkdownTestForm());
  }

  @PostMapping("/markdown")
  public ModelAndView postMarkdownTest(@Valid @ModelAttribute("form") MarkdownTestForm form) {
    var html = markdownService.convertMarkdownToHtml(form.getMarkdown());
    return new ModelAndView("test/markdownTest")
        .addObject("form", form)
        .addObject("html", html);
  }

}
