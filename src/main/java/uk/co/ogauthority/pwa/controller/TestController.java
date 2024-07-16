package uk.co.ogauthority.pwa.controller;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.service.markdown.MailMergeContainer;
import uk.co.ogauthority.pwa.service.markdown.MarkdownService;
import uk.co.ogauthority.pwa.service.markdown.MarkdownTestForm;

/**
 * Test/debug controller for various session related endpoints.
 * Not for production.
 */
@Controller
@Profile("development")
public class TestController {

  private final MarkdownService markdownService;

  @Autowired
  public TestController(MarkdownService markdownService) {
    this.markdownService = markdownService;
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
    var container = new MailMergeContainer();
    container.setMailMergeFields(Map.of("FORENAME", "Joe", "SURNAME", "Bloggs"));
    var html = markdownService.convertMarkdownToHtml(form.getMarkdown(), container);
    return new ModelAndView("test/markdownTest")
        .addObject("form", form)
        .addObject("html", html);
  }

}
