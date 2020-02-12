package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.co.ogauthority.pwa.energyportal.service.SystemAreaAccessService;
import uk.co.ogauthority.pwa.energyportal.service.TopMenuService;
import uk.co.ogauthority.pwa.model.entity.UserSession;
import uk.co.ogauthority.pwa.service.FoxUrlService;
import uk.co.ogauthority.pwa.service.UserSessionService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Import(AbstractControllerTest.AbstractControllerTestConfiguration.class)
public abstract class AbstractControllerTest {

  protected MockMvc mockMvc;

  @Autowired
  protected WebApplicationContext context;

  @MockBean
  protected FoxUrlService foxUrlService;

  @MockBean
  protected UserSessionService userSessionService;

  @MockBean
  protected PwaApplicationDetailService pwaApplicationDetailService;

  @MockBean
  protected PwaApplicationRedirectService pwaApplicationRedirectService;

  @MockBean
  protected TeamService teamService;

  @MockBean
  private TopMenuService topMenuService;

  @Before
  public void abstractControllerTestSetup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(SecurityMockMvcConfigurers.springSecurity())
        .build();

    when(foxUrlService.getFoxLoginUrl()).thenReturn("testLoginUrl");
    when(foxUrlService.getFoxLogoutUrl()).thenReturn("testLogoutUrl");

    when(userSessionService.getAndValidateSession(any(), anyBoolean())).thenReturn(Optional.of(new UserSession()));

    when(pwaApplicationRedirectService.getTaskListRedirect(any())).thenCallRealMethod();

  }


  @TestConfiguration
  public static class AbstractControllerTestConfiguration {
    @Bean
    public SystemAreaAccessService systemAreaAccessService() {
      return new SystemAreaAccessService();
    }
  }

}