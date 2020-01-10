package uk.co.ogauthority.pipelines.controller;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.co.ogauthority.pipelines.service.FoxUrlService;

public abstract class AbstractControllerTest {

  protected MockMvc mockMvc;

  @Autowired
  protected WebApplicationContext context;

  @Before
  public void abstractControllerTestSetup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();
  }

  @TestConfiguration
  public static class TestConfig {

    @MockBean
    protected FoxUrlService foxUrlService;

  }

}
