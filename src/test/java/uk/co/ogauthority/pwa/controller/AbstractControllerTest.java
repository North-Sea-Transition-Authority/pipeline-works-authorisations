package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.co.ogauthority.pwa.auth.HasAnyRoleInterceptor;
import uk.co.ogauthority.pwa.auth.HasTeamRoleService;
import uk.co.ogauthority.pwa.auth.saml.SamlResponseParser;
import uk.co.ogauthority.pwa.config.ExternalApiAuthenticationEntryPoint;
import uk.co.ogauthority.pwa.config.ExternalApiConfiguration;
import uk.co.ogauthority.pwa.config.SamlProperties;
import uk.co.ogauthority.pwa.config.ServiceProperties;
import uk.co.ogauthority.pwa.config.WebSecurityConfig;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsConfig;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsConfigurationProperties;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsProperties;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsService;
import uk.co.ogauthority.pwa.features.application.authorisation.appcontacts.PwaContactService;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.features.webapp.TopMenuService;
import uk.co.ogauthority.pwa.hibernate.HibernateQueryCounter;
import uk.co.ogauthority.pwa.integrations.energyportal.access.EnergyPortalAccessApiConfiguration;
import uk.co.ogauthority.pwa.mvc.PostAuthenticationRequestMdcFilter;
import uk.co.ogauthority.pwa.mvc.RequestLogFilter;
import uk.co.ogauthority.pwa.mvc.error.ErrorService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.footer.FooterService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.teams.TeamService;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;
import uk.co.ogauthority.pwa.teams.management.access.TeamManagementHandlerInterceptor;

@ActiveProfiles("test")
@EnableConfigurationProperties(value = {
    AnalyticsProperties.class,
    AnalyticsConfig.class,
    SamlProperties.class,
    EnergyPortalAccessApiConfiguration.class,
})
@Import({
    AbstractControllerTest.AbstractControllerTestConfiguration.class,
    AnalyticsConfigurationProperties.class,
    WebSecurityConfig.class,
    RequestLogFilter.class,
    PostAuthenticationRequestMdcFilter.class,
    TeamManagementHandlerInterceptor.class,
    HasAnyRoleInterceptor.class
})
public abstract class AbstractControllerTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected WebApplicationContext context;

  @MockBean
  protected PwaApplicationDetailService pwaApplicationDetailService;

  @MockBean
  protected PwaApplicationRedirectService pwaApplicationRedirectService;

  @MockBean
  protected PwaContactService pwaContactService;

  @MockBean
  protected TeamService teamService;

  @MockBean
  private TopMenuService topMenuService;

  @SpyBean
  private ControllerHelperService controllerHelperService;

  @MockBean
  protected ErrorService errorService;

  @SpyBean
  protected FooterService footerServices;

  @MockBean
  protected AnalyticsService analyticsService;

  @MockBean
  protected SamlResponseParser samlResponseParser;

  @MockBean
  protected LogoutSuccessHandler logoutSuccessHandler;

  @MockBean
  protected TeamManagementService teamManagementService;

  @MockBean
  protected TeamQueryService teamQueryService;

  @MockBean
  protected HasTeamRoleService hasTeamRoleService;

  @Autowired
  protected TeamManagementHandlerInterceptor teamManagementHandlerInterceptor;

  @BeforeEach
  public void commonControllerTestSetup() {

    when(pwaApplicationRedirectService.getTaskListRedirect(any())).thenCallRealMethod();
    when(pwaApplicationRedirectService.getTaskListRoute(any())).thenCallRealMethod();
    when(pwaApplicationRedirectService.getTaskListRoute(anyInt(), any())).thenCallRealMethod();

  }

  @TestConfiguration
  @EnableConfigurationProperties(ExternalApiConfiguration.class)
  public static class AbstractControllerTestConfiguration {

    @Bean
    public SystemAreaAccessService systemAreaAccessService(HasTeamRoleService hasTeamRoleService,
                                                           PwaContactService pwaContactService) {
      return new SystemAreaAccessService(true, hasTeamRoleService, pwaContactService);
    }

    @Bean
    public HibernateQueryCounter hibernateQueryInterceptor() {
      return new HibernateQueryCounter();
    }

    @Bean("messageSource")
    public MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasename("messages");
      messageSource.setDefaultEncoding("UTF-8");
      return messageSource;
    }

    // for controllers using session scoped attributes
    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
      CustomScopeConfigurer configurer = new CustomScopeConfigurer();
      configurer.addScope("session", new SimpleThreadScope());
      return configurer;
    }

    @Bean
    public ServiceProperties serviceProperties() {
      return new ServiceProperties(
          "serviceName",
          "fullServiceName",
          "customerMnemonic",
          "customerName",
          "emtMnemonic");
    }

    @Bean
    public ExternalApiAuthenticationEntryPoint externalApiAuthenticationEntryPoint() {
      return new ExternalApiAuthenticationEntryPoint(new ObjectMapper());
    }
  }

}