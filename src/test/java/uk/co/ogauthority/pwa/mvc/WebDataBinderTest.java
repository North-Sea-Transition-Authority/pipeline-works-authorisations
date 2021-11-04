package uk.co.ogauthority.pwa.mvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.controller.AbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;

@RunWith(SpringRunner.class)
@WebMvcTest
public class WebDataBinderTest extends AbstractControllerTest {

  @MockBean
  private PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  private PwaAppProcessingContextService pwaAppProcessingContextService;

  @Test
  public void testStringTrimmer() throws Exception {

    var testUser = new AuthenticatedUserAccount(new WebUserAccount(1), List.of());

    mockMvc.perform(post("/data-binder-test")
        .with(authenticatedUserAndSession(testUser))
        .with(csrf())
        .param("leading", " test")
        .param("trailing", "test ")
        .param("both", "  test  "))
        .andExpect(model().attribute("form", allOf(
            hasProperty("leading", is("test")),
            hasProperty("trailing", is("test")),
            hasProperty("both", is("test"))
        )))
        .andExpect(status().isOk());
  }

  @Controller
  @TestConfiguration
  public static class TestController {

    @PostMapping("/data-binder-test")
    public ModelAndView testMethod(@ModelAttribute("form") TestForm form) {
      return new ModelAndView("dummy");
    }

  }

  public static class TestForm {
    private String leading;
    private String trailing;
    private String both;

    public String getLeading() {
      return leading;
    }

    public void setLeading(String leading) {
      this.leading = leading;
    }

    public String getTrailing() {
      return trailing;
    }

    public void setTrailing(String trailing) {
      this.trailing = trailing;
    }

    public String getBoth() {
      return both;
    }

    public void setBoth(String both) {
      this.both = both;
    }
  }
}
