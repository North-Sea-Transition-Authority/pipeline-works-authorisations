package uk.co.ogauthority.pipelines.mvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pipelines.util.TestUserProvider.authenticatedUserAndSession;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pipelines.controller.AbstractControllerTest;
import uk.co.ogauthority.pipelines.model.entity.UserAccount;

@RunWith(SpringRunner.class)
@WebMvcTest
@Import({AbstractControllerTest.TestConfig.class})
public class WebDataBinderTest {

  @Autowired
  protected WebApplicationContext context;

  @Test
  public void testStringTrimmer() throws Exception {

    MockMvc mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();

    UserAccount testUser = new UserAccount("1");

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
