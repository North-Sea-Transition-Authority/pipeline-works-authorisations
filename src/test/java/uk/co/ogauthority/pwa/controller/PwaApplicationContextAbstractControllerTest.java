package uk.co.ogauthority.pwa.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.co.ogauthority.pwa.config.ServiceProperties;
import uk.co.ogauthority.pwa.config.fileupload.FileUploadProperties;
import uk.co.ogauthority.pwa.energyportal.service.SystemAreaAccessService;
import uk.co.ogauthority.pwa.energyportal.service.TopMenuService;
import uk.co.ogauthority.pwa.model.entity.UserSession;
import uk.co.ogauthority.pwa.service.FoxUrlService;
import uk.co.ogauthority.pwa.service.UserSessionService;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextService;
import uk.co.ogauthority.pwa.service.controllers.ControllerHelperService;
import uk.co.ogauthority.pwa.service.devuk.DevukFieldService;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.footer.FooterService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.service.pwaapplications.context.PwaApplicationPermissionService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.tasklist.CrossingAgreementsTaskListService;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

@Import(PwaApplicationContextAbstractControllerTest.AbstractControllerTestConfiguration.class)
public abstract class PwaApplicationContextAbstractControllerTest {

  protected MockMvc mockMvc;

  @Autowired
  protected WebApplicationContext context;

  @Autowired
  protected PwaApplicationContextService pwaApplicationContextService;

  @MockBean
  protected FoxUrlService foxUrlService;

  @MockBean
  protected UserSessionService userSessionService;

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

  @MockBean
  protected PadPipelineService padPipelineService;

  @SpyBean
  private CrossingAgreementsTaskListService crossingAgreementsTaskListService;

  @MockBean
  protected PadFileService padFileService;

  @MockBean
  protected DevukFieldService devukFieldService;

  @MockBean
  private PwaAppProcessingContextService appProcessingContextService;

  @MockBean
  protected PwaApplicationPermissionService pwaApplicationPermissionService;

  @MockBean
  protected PwaHolderTeamService pwaHolderTeamService;

  @MockBean
  private PwaContextService pwaContextService;

  @SpyBean
  private ControllerHelperService controllerHelperService;

  @MockBean
  protected ServiceProperties serviceProperties;

  @SpyBean
  protected FooterService footerServices;

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
    when(pwaApplicationRedirectService.getTaskListRoute(any())).thenCallRealMethod();
    when(pwaApplicationRedirectService.getTaskListRoute(anyInt(), any())).thenCallRealMethod();

    when(serviceProperties.getCustomerName()).thenReturn("oga");
    when(serviceProperties.getServiceName()).thenReturn("pwa");

  }

  @TestConfiguration
  public static class AbstractControllerTestConfiguration {
    @Bean
    public SystemAreaAccessService systemAreaAccessService() {
      return new SystemAreaAccessService();
    }

    @Bean
    public FileUploadProperties fileUploadProperties() {
      FileUploadProperties fileUploadProperties = new FileUploadProperties();
      fileUploadProperties.setMaxFileSize(1000L);
      fileUploadProperties.setAllowedExtensions(List.of("txt", "xls", "doc"));
      return fileUploadProperties;
    }

    // for controllers using session scoped attributes
    @Bean
    public CustomScopeConfigurer customScopeConfigurer() {
      CustomScopeConfigurer configurer = new CustomScopeConfigurer();
      configurer.addScope("session", new SimpleThreadScope());
      return configurer;
    }

    @Bean("messageSource")
    public MessageSource messageSource() {
      ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
      messageSource.setBasename("messages");
      messageSource.setDefaultEncoding("UTF-8");
      return messageSource;
    }

  }

}